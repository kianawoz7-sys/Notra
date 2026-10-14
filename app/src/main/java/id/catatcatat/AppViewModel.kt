package id.catatcatat

import android.app.Application
import android.net.Uri
import android.util.AtomicFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class Page { HOME, REPORT, SETTINGS, VOICE, DRAFT, RESTORE }
enum class VoicePhase { RECORDING, PREPARING, PROCESSING, FAILED }
data class AppState(
    val records: List<Expense> = emptyList(),
    val page: Page = Page.HOME,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val drafts: List<Draft> = emptyList(),
    val date: LocalDate = LocalDate.now(),
    val editing: Expense? = null,
    val returnPage: Page = Page.HOME,
    val transcript: String = "",
    val voicePhase: VoicePhase = VoicePhase.RECORDING,
    val level: Float = 0f,
    val seconds: Int = 0,
    val processingSeconds: Int = 0,
    val voiceProgress: Int = 0,
    val voiceError: String? = null,
    val message: String? = null,
    val backupAt: Long = 0,
    val changedSinceBackup: Boolean = false,
    val restore: Backup? = null,
    val merge: MergePlan? = null,
    val restoreChoices: Set<String> = emptySet(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val db = ExpenseDatabase.create(app)
    private val dao = db.expenses()
    private val prefs = app.getSharedPreferences("settings", 0)
    private val recorder = VoiceRecorder()
    private val voice = VoiceEngine(app)
    private var discardRecording = false
    private var recordingJob: Job? = null
    private var voiceTask: VoiceTask? = null
    @Volatile private var voiceSession = 0
    private val mutable = MutableStateFlow(AppState(backupAt = prefs.getLong("backupAt", 0), themeMode = ThemeMode.fromPreference(prefs.getString("themeMode", null))))
    val state = mutable.asStateFlow()
    init {
        viewModelScope.launch {
            dao.observe().collect { rows ->
                val signature = withContext(Dispatchers.Default) { BackupCodec.signature(rows) }
                mutable.update { it.copy(records = rows, loading = false, changedSinceBackup = if (prefs.getLong("backupAt", 0) == 0L) rows.isNotEmpty() else signature != prefs.getString("backupSignature", null)) }
            }
        }
    }
    fun message(text: String) { mutable.update { it.copy(message = text) } }
    fun clearMessage() { mutable.update { it.copy(message = null) } }
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("themeMode", mode.name).apply()
        mutable.update { it.copy(themeMode = mode) }
        applyApplicationTheme(getApplication(), mode)
    }
    fun navigate(page: Page) { if (!state.value.busy && state.value.page != Page.VOICE) mutable.update { it.copy(page = page) } }
    fun back() {
        if (state.value.page == Page.VOICE) { cancelRecording(); return }
        if (state.value.busy) return
        mutable.update { it.copy(page = if (it.page == Page.DRAFT) it.returnPage else Page.HOME, drafts = emptyList(), editing = null, restore = null, merge = null) }
    }
    fun newManual() {
        if (state.value.busy) return
        mutable.update { it.copy(page = Page.DRAFT, returnPage = it.page, editing = null, drafts = listOf(Draft()), transcript = "", date = LocalDate.now()) }
    }
    fun edit(row: Expense) { mutable.update { it.copy(page = Page.DRAFT, returnPage = it.page, editing = row, drafts = listOf(Draft(name = row.name, amountText = row.amount.toString())), date = LocalDate.parse(row.date), transcript = "") } }
    fun updateDraft(key: String, name: String? = null, amount: String? = null) {
        if (state.value.busy) return
        mutable.update { it.copy(drafts = it.drafts.map { row -> if (row.key == key) row.copy(name = name ?: row.name, amountText = amount ?: row.amountText, warning = null) else row }) }
    }
    fun removeDraft(key: String) { if (!state.value.busy) mutable.update { it.copy(drafts = it.drafts.filterNot { row -> row.key == key }) } }
    fun addDraft() { if (!state.value.busy && state.value.editing == null) mutable.update { it.copy(drafts = it.drafts + Draft()) } }
    fun setDate(date: LocalDate) { mutable.update { it.copy(date = date) } }
    fun save() {
        val s = state.value
        if (s.busy || s.drafts.isEmpty() || s.drafts.any { !it.valid }) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val rows = s.drafts.map { draft ->
                    Expense(s.editing?.id ?: UUID.randomUUID().toString(), draft.name.trim(), draft.amount!!, s.date.toString(), s.editing?.createdAt ?: now, maxOf(now, (s.editing?.updatedAt ?: 0) + 1))
                }
                db.withTransaction { dao.upsert(rows) }
                mutable.update { it.copy(busy = false, page = s.returnPage, drafts = emptyList(), editing = null, message = if (s.editing != null) "Pengeluaran diperbarui." else "${rows.size} pengeluaran tersimpan.") }
            } catch (e: Exception) { mutable.update { it.copy(busy = false, message = "Belum tersimpan. ${e.message.orEmpty()}") } }
        }
    }
    fun delete(row: Expense) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try { dao.upsert(listOf(row.copy(deleted = true, updatedAt = maxOf(System.currentTimeMillis(), row.updatedAt + 1)))); message("Pengeluaran dihapus.") }
            catch (e: Exception) { message("Belum dapat menghapus pengeluaran.") }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
    fun startRecording() {
        if (state.value.busy || (state.value.page == Page.VOICE && state.value.voicePhase != VoicePhase.FAILED)) return
        if (recordingJob?.isActive == true) { message("Proses sebelumnya sedang berhenti. Coba lagi sebentar."); return }
        val session = ++voiceSession
        discardRecording = false
        recorder.begin()
        mutable.update { it.copy(page = Page.VOICE, voicePhase = VoicePhase.RECORDING, level = 0f, seconds = 0, processingSeconds = 0, voiceProgress = 0, voiceError = null, returnPage = Page.HOME, editing = null, transcript = "", drafts = emptyList()) }
        recordingJob = viewModelScope.launch {
            var ticker: Job? = null
            try {
                val audio = recorder.capture { level, seconds -> mutable.update { if (session == voiceSession) it.copy(level = level, seconds = seconds) else it } }
                if (discardRecording || session != voiceSession) return@launch
                mutable.update { it.copy(voicePhase = VoicePhase.PREPARING, level = 0f, busy = true) }
                val task = VoiceTask()
                voiceTask = task
                val started = android.os.SystemClock.elapsedRealtime()
                ticker = launch {
                    while (isActive && session == voiceSession) {
                        delay(250)
                        val elapsed = ((android.os.SystemClock.elapsedRealtime() - started) / 1000).toInt()
                        if (elapsed >= 45) {
                            task.cancel()
                            voiceSession++
                            mutable.update { it.copy(busy = false, voicePhase = VoicePhase.FAILED, processingSeconds = elapsed, voiceError = "Pemrosesan melewati 45 detik. Coba ucapan lebih pendek atau isi manual.") }
                            break
                        }
                        mutable.update { if (session == voiceSession) it.copy(processingSeconds = elapsed) else it }
                    }
                }
                val transcript = voice.transcribe(audio, task, { stage, percent ->
                    mutable.update { if (session == voiceSession) it.copy(voicePhase = if (stage == 0) VoicePhase.PREPARING else VoicePhase.PROCESSING, voiceProgress = percent.coerceIn(0, 100)) else it }
                })
                if (session != voiceSession) return@launch
                check(transcript.isNotBlank()) { "Suara belum terbaca. Coba rekam lagi." }
                VoiceTranscript.validate(transcript, audio.size / 16000f)
                val drafts = ExpenseParser.parse(transcript)
                mutable.update { it.copy(page = Page.DRAFT, busy = false, drafts = drafts, transcript = transcript, date = LocalDate.now(), editing = null, returnPage = Page.HOME) }
            } catch (e: CancellationException) {
                if (session == voiceSession) mutable.update { it.copy(busy = false, voicePhase = VoicePhase.FAILED, voiceError = "Pemrosesan dibatalkan. Coba lagi atau isi manual.") }
                throw e
            } catch (e: Throwable) {
                android.util.Log.e("CatatVoice", "Voice processing failed; session=$session", e)
                if (session == voiceSession) mutable.update { it.copy(busy = false, voicePhase = VoicePhase.FAILED, voiceError = e.message ?: "Suara belum dapat diproses. Coba lagi atau isi manual.") }
            } finally { ticker?.cancel(); if (session == voiceSession) voiceTask = null }
        }
    }
    fun stopRecording() { recorder.stop() }
    fun cancelRecording() { voiceSession++; discardRecording = true; voiceTask?.cancel(); recorder.stop(); mutable.update { it.copy(page = Page.HOME, busy = false) } }
    fun voiceToManual() { cancelRecording(); newManual() }
    fun recordingBackgrounded() { if (state.value.page == Page.VOICE && state.value.voicePhase == VoicePhase.RECORDING) cancelRecording() }

    fun backupName() = "notra-cadangan-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))}.json"
    fun export(uri: Uri) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val snapshot = dao.snapshot()
                val now = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    val text = BackupCodec.encode(snapshot, now)
                    val stream = getApplication<Application>().contentResolver.openOutputStream(uri, "wt") ?: error("Lokasi backup belum dapat dibuka.")
                    stream.use { it.write(text.toByteArray(Charsets.UTF_8)); it.flush() }
                    prefs.edit().putLong("backupAt", now).putString("backupSignature", BackupCodec.signature(snapshot)).commit()
                }
                val changed = BackupCodec.signature(dao.snapshot()) != BackupCodec.signature(snapshot)
                mutable.update { it.copy(backupAt = now, changedSinceBackup = changed, message = "Backup berhasil disimpan. Salin ke luar HP agar aman saat HP hilang.") }
            } catch (e: Exception) { message("Backup belum berhasil: ${e.message.orEmpty()}") }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
    suspend fun sharedBackup(): File = withContext(Dispatchers.IO) {
        val folder = File(getApplication<Application>().cacheDir, "shared-backups").apply { mkdirs() }
        val file = File(folder, backupName())
        file.writeText(BackupCodec.encode(dao.snapshot()), Charsets.UTF_8)
        file
    }
    fun import(uri: Uri) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                val backup = withContext(Dispatchers.IO) {
                    val stream = getApplication<Application>().contentResolver.openInputStream(uri) ?: error("File belum dapat dibuka.")
                    val bytes = stream.use { input ->
                        val out = ByteArrayOutputStream()
                        val block = ByteArray(8192)
                        var n: Int
                        while (input.read(block).also { n = it } != -1) { require(out.size() + n <= BackupCodec.MAX_BYTES) { "File backup terlalu besar." }; out.write(block, 0, n) }
                        out.toByteArray()
                    }
                    BackupCodec.decode(bytes.toString(Charsets.UTF_8))
                }
                val plan = BackupCodec.plan(dao.snapshot(), backup.records)
                mutable.update { it.copy(page = Page.RESTORE, restore = backup, merge = plan, restoreChoices = emptySet()) }
            } catch (e: Exception) { message("Backup tidak dapat dibaca. ${e.message.orEmpty()}") }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
    fun chooseConflict(id: String, useBackup: Boolean) { mutable.update { it.copy(restoreChoices = if (useBackup) it.restoreChoices + id else it.restoreChoices - id) } }
    fun restore() {
        val s = state.value
        val backup = s.restore ?: return
        if (s.busy) return
        mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                db.withTransaction {
                    val current = dao.snapshot()
                    val plan = BackupCodec.plan(current, backup.records)
                    check(plan == s.merge) { "Data berubah. Buka kembali file backup untuk memeriksa hasilnya." }
                    withContext(Dispatchers.IO) {
                        val safe = AtomicFile(File(getApplication<Application>().filesDir, "before-restore.json"))
                        val stream = safe.startWrite()
                        try { stream.write(BackupCodec.encode(current).toByteArray(Charsets.UTF_8)); safe.finishWrite(stream) }
                        catch (e: Exception) { safe.failWrite(stream); throw e }
                    }
                    dao.upsert(plan.additions + plan.conflicts.filter { it.second.id in s.restoreChoices }.map { it.second })
                }
                mutable.update { it.copy(page = Page.SETTINGS, restore = null, merge = null, message = "Pemulihan selesai. Catatan yang sama tidak digandakan.") }
            } catch (e: Exception) { message("Pemulihan dibatalkan: ${e.message.orEmpty()}") }
            finally { mutable.update { it.copy(busy = false) } }
        }
    }
    override fun onCleared() { voiceSession++; voiceTask?.cancel(); recorder.stop(); super.onCleared() }
}
