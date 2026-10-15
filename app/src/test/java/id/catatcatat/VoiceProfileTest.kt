package id.catatcatat

import org.junit.Assert.*
import org.junit.Test

class VoiceProfileTest {
    @Test fun qualityKeepsTrainedContextAndBeam() {
        assertEquals(0, VoiceProfile.QUALITY.audioContext(16000 * 2))
        assertEquals(3, VoiceProfile.QUALITY.beamSize)
    }

    @Test fun shortUtterancesUseTheSelectedMinimum() {
        assertEquals(512, VoiceProfile.CONTEXT_512.audioContext(16000 * 2))
        assertEquals(384, VoiceProfile.CONTEXT_384.audioContext(16000 * 2))
        assertEquals(384, VoiceProfile.FAST_384.audioContext(16000 * 2))
        assertEquals(3, VoiceProfile.CONTEXT_384.beamSize)
        assertEquals(1, VoiceProfile.FAST_384.beamSize)
    }

    @Test fun longerUtteranceGrowsWindowInsteadOfTruncatingTheEnd() {
        val sevenSeconds = VoiceProfile.CONTEXT_384.audioContext(16000 * 7)
        val fifteenSeconds = VoiceProfile.CONTEXT_384.audioContext(16000 * 15)
        assertTrue(sevenSeconds > 384)
        assertTrue(fifteenSeconds > sevenSeconds)
        assertTrue(sevenSeconds / 50.0 >= 9)
        assertTrue(fifteenSeconds / 50.0 >= 17)
    }

    @Test fun nearThirtySecondsAndLongClipsUseTheFullWindow() {
        for (profile in VoiceProfile.entries) {
            assertEquals(0, profile.audioContext(16000 * 28))
            assertEquals(0, profile.audioContext(16000 * 60))
        }
    }

    @Test fun windowsCoverEntireRecordingAndPaddingAcrossDurations() {
        for (profile in VoiceProfile.entries) for (samples in 4800..(16000 * 27) step 7919) {
            val context = profile.audioContext(samples)
            if (context != 0) assertTrue(context * 320L >= samples + 32000L)
        }
    }

    @Test fun invalidLengthAndUnknownProfileAreRejected() {
        assertTrue(runCatching { VoiceProfile.FAST_384.audioContext(100) }.isFailure)
        assertTrue(runCatching { VoiceProfile.FAST_384.audioContext(960001) }.isFailure)
        assertTrue(runCatching { VoiceProfile.fromKey("typo") }.isFailure)
    }
}
