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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

object WhisperNative {
    init {
        val features = runCatching { File("/proc/cpuinfo").readText().lowercase().split(Regex("\\s+")) }.getOrDefault(emptyList())
        val accelerated = when (Build.SUPPORTED_ABIS.firstOrNull()) {
            "arm64-v8a" -> "asimdhp" in features && "asimddp" in features
            "x86_64" -> listOf("sse4_2", "avx", "avx2", "fma", "f16c").all { it in features }
            else -> false
        }
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
        // Use the phone's normal recording path. Vendor processing varies by device;
        // bounded software gain below also handles quieter PCM without assuming AGC.
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuffer * 2, 6400))
        try {
            check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Mikrofon tidak dapat dibuka. Coba tutup aplikasi lain yang memakai mikrofon." }
            val output = FloatArray(16000 * 60)
            val buffer = ShortArray(3200)
            var count = 0
            recorder.startRecording()
            Log.i("CatatVoice", "Recording source=MIC; inputType=${recorder.routedDevice?.type}; rate=${recorder.sampleRate}")
            while (running.get() && count < output.size) {
                val n = recorder.read(buffer, 0, minOf(buffer.size, output.size - count))
                check(n >= 0) { "Rekaman terputus. Coba lagi." }
                if (n == 0) continue
                var squared = 0.0
                for (i in 0 until n) { val sample = buffer[i] / 32768f; output[count++] = sample; squared += sample * sample }
                val rms = sqrt(squared / n).toFloat()
                level((rms * 12).coerceIn(0f, 1f), count / 16000)
            }
            require(count >= 4800) { "Rekaman terlalu singkat. Coba rekam ucapan lengkap." }
            output.copyOf(count)
        } finally {
            running.set(false)
            runCatching { recorder.stop() }
            recorder.release()
        }
    }
}

class VoiceEngine(private val context: Context) {
    companion object { private val modelFilesMutex = Mutex() }

    suspend fun transcribe(audio: FloatArray, task: VoiceTask = VoiceTask(), progress: (Int, Int) -> Unit = { _, _ -> }, modelName: String = VoiceModels.defaultName): String {
        try { return withContext(Dispatchers.Default) {
            val started = android.os.SystemClock.elapsedRealtime()
            task.checkActive()
            val prepared = AudioPreprocessor.prepare(audio)
            progress(0, 0)
            val model = prepareModel(modelName, task)
            task.checkActive()
            Log.i("CatatVoice", "Recorded=${audio.size / 16000f}s; prepared=${prepared.samples.size / 16000f}s; model=$modelName; gain=${prepared.gain}; rms=${prepared.inputRms}; peak=${prepared.inputPeak}; decode=beam3")
            val text = WhisperNative.transcribe(model.absolutePath, prepared.samples, task.id, VoiceProgress(progress)).trim()
            if (modelName == VoiceModels.SMALL_NAME) {
                // Weight files are expendable; saved expenses and settings are never touched.
                File(context.filesDir, VoiceModels.BASE_NAME).delete()
                File(context.filesDir, "ggml-tiny-q5_1.bin").delete()
            }
            Log.i("CatatVoice", "Voice finished in ${android.os.SystemClock.elapsedRealtime() - started}ms; characters=${text.length}")
            text
        } } finally { task.close() }
    }

    private suspend fun prepareModel(name: String, task: VoiceTask): File = withContext(Dispatchers.IO) {
        modelFilesMutex.withLock {
            task.checkActive()
            val spec = VoiceModels.supported[name] ?: error("Model suara tidak dikenal.")
            val model = File(context.filesDir, name)
            val prefs = context.getSharedPreferences("verified-voice-models", Context.MODE_PRIVATE)
            fun marker() = "${spec.sha256}:${model.length()}:${model.lastModified()}"
            if (model.length() == spec.bytes && prefs.getString(name, null) == marker()) return@withLock model
            fun checksum(file: File): String {
                val digest = MessageDigest.getInstance("SHA-256")
                file.inputStream().use { input ->
                    val block = ByteArray(64 * 1024)
                    while (true) {
                        task.checkActive()
                        val n = input.read(block)
                        if (n < 0) break
                        digest.update(block, 0, n)
                    }
                }
                return digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
            }
            if (model.length() != spec.bytes || checksum(model) != spec.sha256) {
                val temp = File(context.filesDir, "$name.tmp")
                try {
                    val digest = MessageDigest.getInstance("SHA-256")
                    context.assets.open(name).use { input -> temp.outputStream().use { output ->
                        val block = ByteArray(64 * 1024)
                        while (true) {
                            task.checkActive()
                            val n = input.read(block)
                            if (n < 0) break
                            output.write(block, 0, n)
                            digest.update(block, 0, n)
                        }
                    } }
                    val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
                    require(temp.length() == spec.bytes && hash == spec.sha256) { "Model suara tidak lengkap. Pasang ulang aplikasi." }
                    task.checkActive()
                    check(temp.renameTo(model)) { "Penyimpanan HP belum cukup untuk menyiapkan suara." }
                } finally { temp.delete() }
            }
            prefs.edit().putString(name, marker()).apply()
            model
        }
    }

    internal fun trimAudio(audio: FloatArray): FloatArray = AudioPreprocessor.trim(audio)
}
