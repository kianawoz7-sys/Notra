#include <jni.h>
#include <whisper.h>
#include <string>
#include <thread>
#include <algorithm>
#include <memory>
#include <atomic>
#include <chrono>
#include <mutex>
#include <unordered_map>

using Clock = std::chrono::steady_clock;
struct VoiceTask {
    std::atomic<bool> cancelled{false};
    Clock::time_point deadline;
    bool expired() const { return Clock::now() >= deadline; }
    bool aborted() const { return cancelled.load() || expired(); }
};
static std::atomic<jlong> next_task{1};
static std::mutex tasks_mutex;
static std::unordered_map<jlong, std::shared_ptr<VoiceTask>> tasks;
static std::timed_mutex model_mutex;
static std::unique_ptr<whisper_context, decltype(&whisper_free)> cached_context{nullptr, whisper_free};
static std::string cached_path;
static std::shared_ptr<VoiceTask> find_task(jlong id) {
    std::lock_guard<std::mutex> lock(tasks_mutex);
    auto it = tasks.find(id);
    return it == tasks.end() ? nullptr : it->second;
}
static void fail(JNIEnv *env, const char *type, const char *message) { env->ThrowNew(env->FindClass(type), message); }
static bool stop_if_aborted(JNIEnv *env, const VoiceTask &task) {
    if (!task.aborted()) return false;
    if (task.cancelled.load()) fail(env, "java/util/concurrent/CancellationException", "Pemrosesan dibatalkan.");
    else fail(env, "java/util/concurrent/TimeoutException", "Pemrosesan terlalu lama. Coba ucapan lebih pendek atau isi manual.");
    return true;
}
extern "C" JNIEXPORT jlong JNICALL
Java_id_catatcatat_WhisperNative_createTask(JNIEnv *, jobject, jlong timeout) {
    const auto id = next_task.fetch_add(1);
    auto task = std::make_shared<VoiceTask>();
    task->deadline = Clock::now() + std::chrono::milliseconds(std::max<jlong>(1, timeout));
    std::lock_guard<std::mutex> lock(tasks_mutex);
    tasks.emplace(id, task);
    return id;
}
extern "C" JNIEXPORT void JNICALL
Java_id_catatcatat_WhisperNative_cancelTask(JNIEnv *, jobject, jlong id) { auto task = find_task(id); if (task) task->cancelled.store(true); }
extern "C" JNIEXPORT void JNICALL
Java_id_catatcatat_WhisperNative_releaseTask(JNIEnv *, jobject, jlong id) { std::lock_guard<std::mutex> lock(tasks_mutex); tasks.erase(id); }
struct Progress {
    JNIEnv *env;
    jobject listener;
    jmethodID update;
    VoiceTask *task;
    void report(int stage, int percent) { if (!env->ExceptionCheck() && !task->aborted()) env->CallVoidMethod(listener, update, stage, percent); }
};
extern "C" JNIEXPORT jstring JNICALL
Java_id_catatcatat_WhisperNative_transcribe(JNIEnv *env, jobject, jstring path, jfloatArray audio, jlong id, jobject listener) {
    auto task = find_task(id);
    if (!task) { fail(env, "java/lang/IllegalStateException", "Proses suara sudah ditutup."); return nullptr; }
    if (stop_if_aborted(env, *task)) return nullptr;
    std::unique_lock<std::timed_mutex> lock(model_mutex, std::defer_lock);
    while (!lock.try_lock_for(std::chrono::milliseconds(50))) if (stop_if_aborted(env, *task)) return nullptr;
    const char *raw_path = env->GetStringUTFChars(path, nullptr);
    if (!raw_path) return nullptr;
    const std::string model(raw_path);
    env->ReleaseStringUTFChars(path, raw_path);
    auto update = env->GetMethodID(env->GetObjectClass(listener), "update", "(II)V");
    if (!update) return nullptr;
    Progress progress{env, listener, update, task.get()};
    progress.report(0, 0);
    if (cached_path != model || !cached_context) {
        cached_context.reset();
        auto options = whisper_context_default_params();
        options.use_gpu = false;
        cached_context.reset(whisper_init_from_file_with_params(model.c_str(), options));
        cached_path = model;
    }
    if (stop_if_aborted(env, *task)) return nullptr;
    if (!cached_context) { fail(env, "java/lang/IllegalStateException", "Model suara tidak dapat dibuka."); return nullptr; }
    auto *ctx = cached_context.get();
    jfloat *samples = env->GetFloatArrayElements(audio, nullptr);
    if (!samples) return nullptr;
    auto params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.n_threads = std::max(1, std::min(4, static_cast<int>(std::thread::hardware_concurrency())));
    params.language = "id";
    params.translate = false;
    params.no_context = true;
    params.no_timestamps = true;
    params.single_segment = true;
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = false;
    params.print_special = false;
    params.suppress_blank = true;
    params.suppress_nst = true;
    params.temperature_inc = 0.0f;
    const int sample_count = env->GetArrayLength(audio);
    // Keep at least ten seconds of attention for short purchases, plus padding.
    // The earlier 256-frame minimum was too aggressive for a real short clip.
    const int required_context = (sample_count * 50 + 15999) / 16000 + 100;
    params.audio_ctx = std::min(whisper_n_audio_ctx(ctx), std::max(512, ((required_context + 127) / 128) * 128));
    params.max_tokens = std::min(192, std::max(48, sample_count * 8 / 16000 + 24));
    params.initial_prompt = "Catatan pengeluaran dalam rupiah: nasi ayam lima belas ribu, es teh lima ribu, matcha dua belas ribu, bayar kost delapan ratus ribu, paket data lima puluh ribu, ngeprint lima ribu, bensin tiga puluh ribu.";
    params.abort_callback = [](void *data) { return static_cast<VoiceTask *>(data)->aborted(); };
    params.abort_callback_user_data = task.get();
    params.encoder_begin_callback = [](whisper_context *, whisper_state *, void *data) { return !static_cast<VoiceTask *>(data)->aborted(); };
    params.encoder_begin_callback_user_data = task.get();
    params.progress_callback = [](whisper_context *, whisper_state *, int percent, void *data) { static_cast<Progress *>(data)->report(1, percent); };
    params.progress_callback_user_data = &progress;
    progress.report(1, 0);
    const int result = whisper_full(ctx, params, samples, sample_count);
    env->ReleaseFloatArrayElements(audio, samples, JNI_ABORT);
    if (env->ExceptionCheck() || stop_if_aborted(env, *task)) return nullptr;
    if (result != 0) { fail(env, "java/lang/IllegalStateException", "Suara belum berhasil diproses. Coba rekam ulang."); return nullptr; }
    for (int i = 0; i < whisper_full_n_segments(ctx); ++i) {
        if (whisper_full_n_tokens(ctx, i) >= params.max_tokens) {
            fail(env, "java/lang/IllegalStateException", "Hasil suara terlalu panjang atau berulang. Coba rekam ulang; belum ada pengeluaran yang disimpan.");
            return nullptr;
        }
    }
    std::string text;
    for (int i = 0; i < whisper_full_n_segments(ctx); ++i) text += whisper_full_get_segment_text(ctx, i);
    return env->NewStringUTF(text.c_str());
}
