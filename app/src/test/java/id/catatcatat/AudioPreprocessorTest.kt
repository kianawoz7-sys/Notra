package id.catatcatat

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class AudioPreprocessorTest {
    private fun tone(seconds: Int = 1, amplitude: Float = .02f) =
        FloatArray(16000 * seconds) { (sin(2 * PI * 220 * it / 16000) * amplitude).toFloat() }

    @Test fun silenceIsRejectedBeforeRecognition() {
        assertTrue(runCatching { AudioPreprocessor.prepare(FloatArray(16000)) }.isFailure)
    }
    @Test fun AlmostInaudibleNoiseIsNotBoosted() {
        assertTrue(runCatching { AudioPreprocessor.prepare(tone(amplitude = .00001f)) }.isFailure)
    }
    @Test fun quietContinuousVoiceCanBeAdjusted() {
        val audio = tone(amplitude = .0015f)
        val prepared = AudioPreprocessor.prepare(audio)
        assertTrue(prepared.gain > 1f)
        assertTrue(prepared.gain <= 8f)
        assertTrue(prepared.samples.maxOf { abs(it) } > audio.maxOf { abs(it) })
    }
    @Test fun amplificationDoesNotCreateClipping() {
        val prepared = AudioPreprocessor.prepare(tone(amplitude = 1.4f))
        assertTrue(prepared.samples.all { abs(it) <= .95001f })
        assertTrue(prepared.gain < 1f)
    }
    @Test fun healthyRecordingIsNotBoosted() {
        val audio = tone(amplitude = .08f)
        val prepared = AudioPreprocessor.prepare(audio)
        assertEquals(1f, prepared.gain)
        assertArrayEquals(audio, prepared.samples, 0f)
    }
    @Test fun pausesBetweenPurchasesArePreserved() {
        val audio = FloatArray(16000 * 5)
        tone().copyInto(audio, 16000)
        tone().copyInto(audio, 48000)
        val prepared = AudioPreprocessor.prepare(audio)
        val startOfPause = 6400 + 16000
        assertTrue(prepared.samples.sliceArray(startOfPause until startOfPause + 16000).all { it == 0f })
        assertEquals(60800, prepared.samples.size)
    }
    @Test fun weakBeginningAndEndingAreKeptNearLouderSpeech() {
        val audio = FloatArray(16000 * 3)
        tone(amplitude = .03f).copyInto(audio, 16000)
        audio[11000] = .0001f
        audio[37000] = .0001f
        val trimmed = AudioPreprocessor.trim(audio)
        assertEquals(2, trimmed.count { it == .0001f })
    }
    @Test fun isolatedMicrophoneClickIsRejected() {
        val audio = FloatArray(16000)
        audio[8000] = .9f
        assertTrue(runCatching { AudioPreprocessor.prepare(audio) }.isFailure)
    }
    @Test fun invalidSamplesAreRejected() {
        val audio = tone()
        audio[500] = Float.NaN
        assertTrue(runCatching { AudioPreprocessor.prepare(audio) }.isFailure)
    }
    @Test fun VeryShortRecordingIsRejected() {
        assertTrue(runCatching { AudioPreprocessor.prepare(tone().copyOf(1600)) }.isFailure)
    }
    @Test fun originalRecordingIsNotMutated() {
        val audio = tone(amplitude = .0015f)
        val original = audio.copyOf()
        AudioPreprocessor.prepare(audio)
        assertArrayEquals(original, audio, 0f)
    }
}
