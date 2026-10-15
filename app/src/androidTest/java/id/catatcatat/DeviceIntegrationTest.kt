package id.catatcatat

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import androidx.room.withTransaction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.os.SystemClock
import android.util.Log

@RunWith(AndroidJUnit4::class)
class DeviceIntegrationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun row() = Expense(UUID.randomUUID().toString(), "Kost", 800000, "2026-10-14", 100, 100)

    @Test fun savedRecordsSurviveDatabaseReopen() = runBlocking {
        val name = "integration-${UUID.randomUUID()}.db"
        val expense = row()
        var db = Room.databaseBuilder(context, ExpenseDatabase::class.java, name).build()
        try {
            db.expenses().upsert(listOf(expense))
            db.close()
            db = Room.databaseBuilder(context, ExpenseDatabase::class.java, name).build()
            assertEquals(listOf(expense), db.expenses().snapshot())
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun transactionFailureKeepsExistingData() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, ExpenseDatabase::class.java).build()
        try {
            val original = row()
            db.expenses().upsert(listOf(original))
            runCatching { db.withTransaction { db.expenses().upsert(listOf(row())); error("Simulated failure") } }
            assertEquals(listOf(original), db.expenses().snapshot())
        } finally { db.close() }
    }

    @Test fun backupRestoresIntoFreshDatabaseWithoutDuplicates() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, ExpenseDatabase::class.java).build()
        try {
            val original = listOf(row(), row().copy(deleted = true))
            val backup = BackupCodec.decode(BackupCodec.encode(original))
            db.withTransaction { db.expenses().upsert(BackupCodec.plan(db.expenses().snapshot(), backup.records).additions) }
            db.withTransaction { db.expenses().upsert(BackupCodec.plan(db.expenses().snapshot(), backup.records).additions) }
            assertEquals(original.sortedBy { it.id }, db.expenses().snapshot())
        } finally { db.close() }
    }

    @Test fun silentAudioDoesNotProduceInventedExpenses() = runBlocking {
        val failure = runCatching { VoiceEngine(context).transcribe(FloatArray(16000)) }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
        assertTrue(failure?.message.orEmpty().contains("Suara belum terdengar"))
    }

    @Test fun indonesianSpeechBecomesTwoExpensesOffline() = runBlocking {
        assertSpeech("expenses", listOf(15000L, 5000L))
    }
    @Test fun singleSpokenExpenseWorksOffline() = runBlocking { assertSpeech("single", listOf(12000L)) }
    @Test fun sixSecondMakanFifteenThousandDoesNotRepeat() = runBlocking { assertSpeech("reported-six", listOf(15000L)) }
    @Test fun colloquialRebuSpeechWorksOffline() = runBlocking { assertSpeech("colloquial", listOf(12000L, 5000L)) }
    @Test fun fourSpokenExpenseTypesWorkOffline() = runBlocking { assertSpeech("four", listOf(800000L, 50000L, 5000L, 30000L)) }

    @Test fun nativeProcessingCanBeCancelledAndReused() = runBlocking {
        val engine = VoiceEngine(context)
        val audio = speechAudio("four")
        // Warm the context first, then cancel while the encoder is actually running.
        engine.transcribe(audio)
        val task = VoiceTask()
        val started = SystemClock.elapsedRealtime()
        val work = async { engine.transcribe(audio, task) }
        delay(700)
        task.cancel()
        val failure = runCatching { withTimeout(10_000) { work.await() } }.exceptionOrNull()
        assertTrue("Expected cancellation, got $failure", failure is CancellationException)
        assertTrue("Native work did not stop promptly", SystemClock.elapsedRealtime() - started < 10_000)
        assertSpeech("single", listOf(12000L))
    }

    @Test fun nativeDeadlineInterruptsProcessing() = runBlocking {
        val engine = VoiceEngine(context)
        val audio = speechAudio("four")
        engine.transcribe(audio)
        val started = SystemClock.elapsedRealtime()
        val failure = runCatching { engine.transcribe(audio, VoiceTask(700)) }.exceptionOrNull()
        assertTrue("Expected timeout, got $failure", failure is TimeoutException)
        assertTrue("Deadline did not interrupt native work", SystemClock.elapsedRealtime() - started < 10_000)
        assertSpeech("single", listOf(12000L))
    }

    @Test fun silenceTrimmingKeepsQuietSpeechAndInternalPauses() {
        val audio = FloatArray(16000 * 6)
        for (i in 16000 until 24000) audio[i] = .003f
        for (i in 56000 until 64000) audio[i] = .003f
        val trimmed = VoiceEngine(context).trimAudio(audio)
        assertEquals(60800, trimmed.size)
        assertEquals(16000, trimmed.count { it == .003f })
        assertEquals(.003f, trimmed[6400])
        assertEquals(.003f, trimmed[40000 + 6400])
    }

    private fun speechAudio(name: String): FloatArray {
        val pcm = InstrumentationRegistry.getInstrumentation().context.assets.open("$name.pcm").use { it.readBytes() }
        val buffer = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(pcm.size / 2) { buffer.short / 32768f }
    }

    private suspend fun assertSpeech(name: String, expected: List<Long>) {
        val audio = speechAudio(name)
        val started = SystemClock.elapsedRealtime()
        val transcript = VoiceEngine(context).transcribe(audio)
        VoiceTranscript.validate(transcript, audio.size / 16000f)
        Log.i("CatatVoiceTest", "Synthetic Indonesian: $transcript; ${SystemClock.elapsedRealtime() - started} ms")
        val expenses = ExpenseParser.parse(transcript)
        assertEquals(transcript, expected, expenses.map { it.amount })
        assertTrue(expenses.all { it.valid })
    }
}
