package id.catatcatat

import android.os.SystemClock
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Runs only in the separate experiment app. Recognition failures remain in the report. */
@RunWith(AndroidJUnit4::class)
class VoiceProfileBenchmarkTest {
    private data class Case(val file: String, val names: List<String>, val amounts: List<Long>)

    @Test fun compareProfilesOnIdenticalAudio() = runBlocking {
        assertTrue("Never run this benchmark against the user's main app", BuildConfig.VOICE_EXPERIMENT)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val engine = VoiceEngine(instrumentation.targetContext)
        // Keep the experiment activity foreground, as in real use. Background-only
        // instrumentation can get a different CPU scheduling policy on phones.
        val activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        lateinit var status: TextView
        instrumentation.runOnMainSync {
            status = TextView(activity).apply {
                text = "Pengujian otomatis sedang berjalan\nAudio sintetis • tunggu sampai selesai"
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(android.graphics.Color.BLACK)
                setBackgroundColor(0xEEFFFFFF.toInt())
                setPadding(24, 32, 24, 32)
            }
            // Keep the real orb UI rendering behind the panel, but consume test
            // touches so a manual microphone session cannot queue with fixtures.
            val panel = FrameLayout(activity).apply {
                isClickable = true
                isFocusable = true
                addView(status, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP).apply { topMargin = (80 * resources.displayMetrics.density).toInt() })
            }
            activity.addContentView(panel, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        try {
        val cases = listOf(
            Case("cuci-normal", listOf("cuci"), listOf(12000L)),
            Case("cuci-fast", listOf("cuci"), listOf(12000L)),
            Case("cuci-quiet", listOf("cuci"), listOf(12000L)),
            Case("parkir-normal", listOf("parkir"), listOf(2000L)),
            Case("chatgpt-normal", listOf("langganan chatgpt"), listOf(165000L)),
            Case("devices-normal", listOf("charger", "speaker"), listOf(30000L, 120000L)),
            Case("four", listOf("kos", "paket data", "ngeprint", "bensin"), listOf(800000L, 50000L, 5000L, 30000L)),
            Case("reported-six", listOf("makan"), listOf(15000L))
        )
        val requested = InstrumentationRegistry.getArguments().getString("profiles")
        val profiles = requested?.split(',')?.map(VoiceProfile::fromKey) ?: VoiceProfile.entries.toList()
        require(profiles.isNotEmpty() && profiles.distinct().size == profiles.size)
        fun audio(name: String): FloatArray {
            val raw = instrumentation.context.assets.open("$name.pcm").use { it.readBytes() }
            val buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN)
            return FloatArray(raw.size / 2) { buffer.short / 32768f }
        }
        // Exclude copying/verification/initial model preparation from steady-state timings.
        // Warm kernels/weights with a short window first; the full-window cold
        // encoder can hit the same 45-second guard before measurements begin.
        engine.transcribe(audio("cuci-normal"), profile = VoiceProfile.CONTEXT_384)
        Log.i("NotraProfileBench", "Warm-up context384 complete; synthetic audio, not microphone input")
        var completed = 0
        for ((index, case) in cases.withIndex()) {
            val samples = audio(case.file)
            // Rotate order to reduce a systematic cold/thermal advantage for one profile.
            val offset = index % profiles.size
            for (profile in profiles.drop(offset) + profiles.take(offset)) {
                instrumentation.runOnMainSync { status.text = "Uji ${case.file} • ${profile.key}\nAudio sintetis • tunggu sampai selesai" }
                val started = SystemClock.elapsedRealtime()
                val result = JSONObject().put("case", case.file).put("profile", profile.key)
                    .put("model", VoiceModels.defaultName).put("audioSeconds", samples.size / 16000f)
                    .put("expectedNames", JSONArray(case.names)).put("expectedAmounts", JSONArray(case.amounts))
                try {
                    val text = engine.transcribe(samples, profile = profile)
                    VoiceTranscript.validate(text, samples.size / 16000f)
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
                Log.i("NotraProfileBench", result.toString())
            }
        }
        assertEquals("Pipeline errors occurred; inspect the report", cases.size * profiles.size, completed)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }
}
