package id.catatcatat

import kotlin.math.abs
import kotlin.math.sqrt

/** Local level adjustment, not a denoiser or a speech classifier. */
object AudioPreprocessor {
    const val SAMPLE_RATE = 16000
    private const val FRAME_SAMPLES = 320
    private const val EDGE_PADDING = 6400 // 400 ms: preserve weak consonants around louder vowels.
    private const val MIN_PEAK_RMS = .0006f
    private const val MIN_ACTIVE_RMS = .00035f
    private const val MAX_GAIN = 8f
    private const val TARGET_RMS = .04f
    private const val MAX_OUTPUT_PEAK = .95f

    data class Prepared(
        val samples: FloatArray,
        val gain: Float,
        val inputRms: Float,
        val inputPeak: Float
    )

    fun prepare(audio: FloatArray): Prepared {
        require(audio.size >= 4800 && audio.all(Float::isFinite)) { "Rekaman belum lengkap. Coba rekam lagi." }
        val clean = FloatArray(audio.size) { audio[it].coerceIn(-1f, 1f) }
        val trimmed = trim(clean)
        var sum = 0.0
        var peak = 0f
        for (sample in trimmed) {
            sum += sample.toDouble() * sample
            peak = maxOf(peak, abs(sample))
        }
        val rms = sqrt(sum / trimmed.size).toFloat()
        val gain = (TARGET_RMS / rms).coerceIn(1f, MAX_GAIN)
            .coerceAtMost(MAX_OUTPUT_PEAK / peak)
        return Prepared(FloatArray(trimmed.size) { trimmed[it] * gain }, gain, rms, peak)
    }

    fun trim(audio: FloatArray): FloatArray {
        require(audio.isNotEmpty()) { "Suara belum terdengar. Coba rekam lagi." }
        val frameCount = (audio.size + FRAME_SAMPLES - 1) / FRAME_SAMPLES
        val levels = FloatArray(frameCount)
        for (frame in levels.indices) {
            val start = frame * FRAME_SAMPLES
            val end = minOf(start + FRAME_SAMPLES, audio.size)
            var sum = 0.0
            for (i in start until end) sum += audio[i].toDouble() * audio[i]
            levels[frame] = sqrt(sum / (end - start)).toFloat()
        }
        val loudest = levels.maxOrNull() ?: 0f
        require(loudest >= MIN_PEAK_RMS) { "Suara belum terdengar. Dekatkan HP dan coba rekam lagi." }
        val threshold = maxOf(MIN_ACTIVE_RMS, minOf(.0015f, loudest * .08f))
        // A single click must not be amplified into a recording of speech.
        require(levels.count { it >= threshold } >= 3) { "Suara terlalu singkat. Coba rekam ucapan lengkap." }
        val first = levels.indexOfFirst { it >= threshold } * FRAME_SAMPLES
        val last = minOf((levels.indexOfLast { it >= threshold } + 1) * FRAME_SAMPLES, audio.size)
        return audio.copyOfRange(maxOf(0, first - EDGE_PADDING), minOf(audio.size, last + EDGE_PADDING))
    }
}
