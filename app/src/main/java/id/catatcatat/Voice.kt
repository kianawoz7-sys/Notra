package id.catatcatat

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt
import kotlin.math.abs

object WhisperNative {
    init {
        val features = runCatching { File("/proc/cpuinfo").readText().lowercase().split(Regex("\\s+")) }.getOrDefault(emptyList())
        val accelerated = Build.SUPPORTED_ABIS.firstOrNull() == "arm64-v8a" && "asimdhp" in features && "asimddp" in features
        val library = if (accelerated) "catatvoice_fast" else "catatvoice"
        System.loadLibrary(library)
        Log.i("CatatVoice", "Native library: $library; ABI=${Build.SUPPORTED_ABIS.firstOrNull()}")
    }
    external fun createTask(timeoutMs: Long): Long
    external fun cancelTask(task: Long)
    external fun releaseTask(task: Long)
    external fun transcribe(modelPath: String, audio: FloatArray, task: Long, progress: VoiceProgress): String
}

@Keep
class VoiceProgress(private val report: (Int, Int) -> Unit) {
    @Keep fun update(stage: Int, percent: Int) = report(stage, percent)
}

class VoiceTask(timeoutMs: Long = 45_000) {
    internal val id = WhisperNative.createTask(timeoutMs)
    private val cancelled = AtomicBoolean(false)
    fun cancel() { cancelled.set(true); WhisperNative.cancelTask(id) }
    internal fun checkActive() { if (cancelled.get()) throw java.util.concurrent.CancellationException("Pemrosesan dibatalkan.") }
    internal fun close() = WhisperNative.releaseTask(id)
}

class VoiceRecorder {
    private val running = AtomicBoolean(false)
    fun begin() { running.set(true) }
    fun stop() { running.set(false) }
    @Suppress("MissingPermission")
    suspend fun capture(level: (Float, Int) -> Unit): FloatArray = withContext(Dispatchers.IO) {
        val minBuffer = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        require(minBuffer > 0) { "Mikrofon belum tersedia." }
        val recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuffer * 2, 6400))
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Mikrofon tidak dapat dibuka. Coba tutup aplikasi lain yang memakai mikrofon." }
            val output = FloatArray(16000 * 60)
            val buffer = ShortArray(3200)
            var count = 0
            var loudest = 0f
            recorder.startRecording()
            while (running.get() && count < output.size) {
                val n = recorder.read(buffer, 0, minOf(buffer.size, output.size - count))
                check(n >= 0) { "Rekaman terputus. Coba lagi." }
                if (n == 0) continue
                var squared = 0.0
                for (i in 0 until n) { val sample = buffer[i] / 32768f; output[count++] = sample; squared += sample * sample }
                val rms = sqrt(squared / n).toFloat()
                loudest = maxOf(loudest, rms)
                level((rms * 12).coerceIn(0f, 1f), count / 16000)
            }
            require(count >= 4800 && loudest >= 0.002f) { "Suara belum terdengar. Dekatkan HP dan coba rekam lagi." }
            output.copyOf(count)
        } finally {
            running.set(false)
            runCatching { recorder.stop() }
            recorder.release()
        }
    }
}

class VoiceEngine(private val context: Context) {
    suspend fun transcribe(audio: FloatArray, task: VoiceTask = VoiceTask(), progress: (Int, Int) -> Unit = { _, _ -> }, modelName: String = "ggml-base-q5_1.bin"): String {
        try { return withContext(Dispatchers.Default) {
            val started = android.os.SystemClock.elapsedRealtime()
            task.checkActive()
            require(audio.any { abs(it) >= .002f }) { "Suara belum terdengar. Dekatkan HP dan coba rekam lagi." }
            progress(0, 0)
            val expectedSize = when (modelName) {
                "ggml-tiny-q5_1.bin" -> 32152673L
                "ggml-base-q5_1.bin" -> 59707625L
                else -> error("Model suara tidak dikenal.")
            }
            val model = File(context.filesDir, modelName)
            if (model.length() != expectedSize) {
                val temp = File(context.filesDir, "$modelName.tmp")
                try {
                    context.assets.open(modelName).use { input -> temp.outputStream().use { output ->
                        val block = ByteArray(64 * 1024)
                        while (true) {
                            task.checkActive()
                            val n = input.read(block)
                            if (n < 0) break
                            output.write(block, 0, n)
                        }
                    } }
                    require(temp.length() == expectedSize) { "Model suara tidak lengkap. Pasang ulang aplikasi." }
                    check(temp.renameTo(model)) { "Penyimpanan HP belum cukup untuk menyiapkan suara." }
                } finally { temp.delete() }
            }
            task.checkActive()
            val trimmed = trimAudio(audio)
            Log.i("CatatVoice", "Recorded=${audio.size / 16000f}s; trimmed=${trimmed.size / 16000f}s; model=$modelName")
            val text = WhisperNative.transcribe(model.absolutePath, trimmed, task.id, VoiceProgress(progress)).trim()
            Log.i("CatatVoice", "Voice finished in ${android.os.SystemClock.elapsedRealtime() - started}ms; characters=${text.length}")
            text
        } } finally { task.close() }
    }

    // Remove silence at the edges, keeping 250 ms around audible samples.
    // Pauses between purchases remain intact.
    internal fun trimAudio(audio: FloatArray): FloatArray {
        val block = 320
        var first = -1
        var last = 0
        for (start in audio.indices step block) {
            val end = minOf(start + block, audio.size)
            var sum = 0.0
            for (i in start until end) sum += audio[i] * audio[i]
            if (sqrt(sum / (end - start)) >= .002) {
                if (first < 0) first = start
                last = end
            }
        }
        if (first < 0) return audio
        return audio.copyOfRange(maxOf(0, first - 4000), minOf(audio.size, last + 4000))
    }
}
