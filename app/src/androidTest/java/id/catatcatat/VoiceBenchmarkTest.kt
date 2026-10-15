package id.catatcatat

import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Measurements, including recognition errors, are recorded rather than hidden.
 * Fixtures are synthetic; they cannot validate the physical phone microphone.
 */
@RunWith(AndroidJUnit4::class)
class VoiceBenchmarkTest {
    private data class Case(val file: String, val names: List<String>, val amounts: List<Long>)

    @Test fun measureNamesAmountsAndLatency() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val engine = VoiceEngine(instrumentation.targetContext)
        val cases = listOf(
            Case("cuci-normal", listOf("cuci"), listOf(12000L)),
            Case("cuci-fast", listOf("cuci"), listOf(12000L)),
            Case("cuci-quiet", listOf("cuci"), listOf(12000L)),
            Case("parkir-normal", listOf("parkir"), listOf(2000L)),
            Case("chatgpt-normal", listOf("langganan chatgpt"), listOf(165000L)),
            Case("devices-normal", listOf("charger", "speaker"), listOf(30000L, 120000L))
        )
        var completed = 0
        for (case in cases) {
            val raw = instrumentation.context.assets.open("${case.file}.pcm").use { it.readBytes() }
            val buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN)
            val audio = FloatArray(raw.size / 2) { buffer.short / 32768f }
            val started = SystemClock.elapsedRealtime()
            val result = JSONObject().put("case", case.file).put("model", VoiceModels.defaultName)
                .put("audioSeconds", audio.size / 16000f)
                .put("expectedNames", JSONArray(case.names)).put("expectedAmounts", JSONArray(case.amounts))
            try {
                val text = engine.transcribe(audio)
                VoiceTranscript.validate(text, audio.size / 16000f)
                val rows = ExpenseParser.parse(text)
                val amounts = rows.map { it.amount }
                val names = rows.map { it.name.lowercase() }
                result.put("text", text).put("actualNames", JSONArray(names)).put("actualAmounts", JSONArray(amounts))
                    .put("amountsCorrect", amounts == case.amounts)
                    .put("namesCorrect", names.size == case.names.size && names.zip(case.names).all { (actual, expected) -> actual.contains(expected) })
                completed++
            } catch (failure: Exception) {
                result.put("error", "${failure.javaClass.simpleName}: ${failure.message}")
            }
            result.put("elapsedMs", SystemClock.elapsedRealtime() - started)
            Log.i("NotraBenchmark", result.toString())
        }
        // This asserts the pipeline completed, not that every recognition was correct.
        assertEquals("Some fixtures could not finish; inspect NotraBenchmark output", cases.size, completed)
    }
}
