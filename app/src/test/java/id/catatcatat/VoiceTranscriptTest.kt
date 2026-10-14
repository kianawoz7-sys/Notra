package id.catatcatat

import org.junit.Assert.*
import org.junit.Test

class VoiceTranscriptTest {
    @Test fun reportedTwentyEightRepeatedExpensesAreRejected() {
        val failure = runCatching { VoiceTranscript.validate("makan lima belas ribu, ".repeat(28), 6f) }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
    }
    @Test fun repeatedLoopInLongRecordingIsRejected() {
        assertTrue(runCatching { VoiceTranscript.validate("makan lima belas ribu, ".repeat(8), 60f) }.isFailure)
    }
    @Test fun genuinelyRepeatedPurchasesAreKeptForReview() {
        VoiceTranscript.validate("makan lima belas ribu, ".repeat(4), 12f)
        assertEquals(4, ExpenseParser.parse("makan lima belas ribu, ".repeat(4)).size)
    }
    @Test fun fourDifferentPurchasesAreAllowed() {
        VoiceTranscript.validate("bayar kost delapan ratus ribu paket data lima puluh ribu ngeprint lima ribu bensin tiga puluh ribu", 12f)
    }
}
