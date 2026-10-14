@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package id.catatcatat

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Paper: Color @Composable get() = MaterialTheme.colorScheme.background
private val Line: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val Green: Color @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
private val Panel: Color @Composable get() = MaterialTheme.colorScheme.surface
private val SoftPanel: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val TonedPanel: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
private val Indonesian = Locale("id", "ID")
private fun dayLabel(date: LocalDate) = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Indonesian))
private fun timeLabel(time: Long) = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH.mm"))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyApplicationTheme(this, ThemeMode.fromPreference(getSharedPreferences("settings", 0).getString("themeMode", null)))
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AppViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            var launching by rememberSaveable { mutableStateOf(savedInstanceState == null) }
            NotraTheme(state.themeMode) {
                if (launching) NotraSplash { launching = false } else App(vm, this)
            }
        }
    }
}

@Composable
private fun App(vm: AppViewModel, activity: MainActivity) {
    val s by vm.state.collectAsStateWithLifecycle()
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(today) {
        val nextDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault())
        delay(Duration.between(ZonedDateTime.now(), nextDay).toMillis().coerceAtLeast(1000))
        today = LocalDate.now()
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deleting by remember { mutableStateOf<Expense?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.startRecording() else vm.message("Izinkan mikrofon untuk mencatat lewat suara. Input manual tetap bisa digunakan.")
    }
    val record = {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) vm.startRecording()
        else permission.launch(Manifest.permission.RECORD_AUDIO)
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) vm.export(uri) }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.import(uri) }
    val share: () -> Unit = {
        scope.launch {
            try {
                val file = vm.sharedBackup()
                val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.files", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri("Cadangan Notra", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(intent, "Simpan salinan backup di luar HP"))
            } catch (e: Exception) { vm.message("Belum dapat membagikan backup. ${e.message.orEmpty()}") }
        }
    }
    LaunchedEffect(s.message) { s.message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) vm.recordingBackgrounded()
            if (event == Lifecycle.Event.ON_RESUME) today = LocalDate.now()
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    BackHandler(enabled = s.page != Page.HOME) { vm.back() }
    Scaffold(containerColor = Paper, snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            when (s.page) {
                Page.HOME -> RoundIcon(UiIcons.Menu, "Laporan", enabled = !s.busy) { vm.navigate(Page.REPORT) }
                Page.VOICE -> RoundIcon(UiIcons.Close, "Batal", action = vm::cancelRecording)
                else -> RoundIcon(UiIcons.ArrowBack, "Kembali", enabled = !s.busy) { vm.back() }
            }
            if (s.page in listOf(Page.HOME, Page.VOICE)) Spacer(Modifier.weight(1f))
            else Text(when (s.page) { Page.SETTINGS -> "Pengaturan"; Page.DRAFT -> "Periksa dulu"; Page.RESTORE -> "Pulihkan data"; else -> "Laporan" }, Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = (-.7).sp)
            if (s.page in listOf(Page.HOME, Page.REPORT)) RoundIcon(UiIcons.Tune, "Pengaturan", enabled = !s.busy) { vm.navigate(Page.SETTINGS) }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (s.page) {
                Page.HOME, Page.VOICE -> CaptureScreen(s, today, record, vm)
                Page.REPORT -> Report(s, vm::edit) { deleting = it }
                Page.SETTINGS -> Settings(s, vm::setThemeMode, { export.launch(vm.backupName()) }, { import.launch(arrayOf("*/*")) }, share)
                Page.DRAFT -> DraftEditor(s, vm, record)
                Page.RESTORE -> RestoreScreen(s, vm)
            }
            if (s.busy && s.page != Page.VOICE) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter), color = Green)
        }
    }
    deleting?.let { row ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Hapus pengeluaran?") }, text = { Text("${row.name} · ${rupiah(row.amount)} akan dihapus dari laporan.") }, confirmButton = { TextButton(onClick = { vm.delete(row); deleting = null }) { Text("Hapus", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Batal") } })
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, enabled: Boolean = true, action: () -> Unit) {
    FilledTonalIconButton(onClick = action, enabled = enabled, modifier = Modifier.size(46.dp), shape = CircleShape, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Panel, contentColor = Ink)) { Icon(icon, label, Modifier.size(22.dp)) }
}
@Composable
private fun EmptyState(title: String, body: String) { Surface(color = Panel, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) { Icon(UiIcons.ReceiptLong, null, tint = Muted, modifier = Modifier.size(28.dp)); Text(title, fontWeight = FontWeight.Medium); Text(body, fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center, lineHeight = 20.sp) } } }

@Composable
private fun ExpenseRow(row: Expense, enabled: Boolean, edit: (Expense) -> Unit, delete: (Expense) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { edit(row) }) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(SoftPanel, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text(row.name.take(1).uppercase(Indonesian), color = Green, fontWeight = FontWeight.Medium) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(row.name, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text(timeLabel(row.createdAt), fontSize = 11.sp, color = Muted, modifier = Modifier.padding(top = 3.dp)) }
            Text(rupiah(row.amount), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Box { IconButton(onClick = { menu = true }, enabled = enabled, modifier = Modifier.size(38.dp)) { Icon(UiIcons.MoreVert, "Pilihan ${row.name}", Modifier.size(18.dp), tint = Muted) }; DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { DropdownMenuItem(text = { Text("Ubah") }, onClick = { menu = false; edit(row) }); DropdownMenuItem(text = { Text("Hapus") }, onClick = { menu = false; delete(row) }) } }
        }
    }
}

@Composable
private fun DateChooser(initial: LocalDate, close: () -> Unit, selected: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    DatePickerDialog(onDismissRequest = close, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { selected(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }; close() }) { Text("Pilih") } }, dismissButton = { TextButton(onClick = close) { Text("Batal") } }) { DatePicker(state) }
}

@Composable
private fun Report(s: AppState, edit: (Expense) -> Unit, delete: (Expense) -> Unit) {
    var period by remember { mutableStateOf(Period.DAY) }
    var anchor by remember { mutableStateOf(LocalDate.now()) }
    var chooseDate by remember { mutableStateOf(false) }
    val range = periodRange(anchor, period)
    val rows = visibleExpenses(s.records, range)
    val label = when (period) { Period.DAY -> dayLabel(anchor); Period.WEEK -> "${range.start.format(DateTimeFormatter.ofPattern("d MMM", Indonesian))} — ${dayLabel(range.end)}"; Period.MONTH -> anchor.format(DateTimeFormatter.ofPattern("MMMM yyyy", Indonesian)) }
    fun move(direction: Long) { anchor = when (period) { Period.DAY -> anchor.plusDays(direction); Period.WEEK -> anchor.plusWeeks(direction); Period.MONTH -> anchor.plusMonths(direction) } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("Lihat ke mana uangmu pergi.", color = Muted, fontSize = 14.sp)
            Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Period.entries.forEach { p -> FilterChip(selected = period == p, onClick = { period = p }, label = { Text(when (p) { Period.DAY -> "Hari"; Period.WEEK -> "Minggu"; Period.MONTH -> "Bulan" }) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { move(-1) }) { Icon(UiIcons.ChevronLeft, "Periode sebelumnya") }
                TextButton(onClick = { chooseDate = true }, modifier = Modifier.weight(1f)) { Text(label, textAlign = TextAlign.Center, fontSize = 14.sp) }
                IconButton(onClick = { move(1) }) { Icon(UiIcons.ChevronRight, "Periode berikutnya") }
            }
            Surface(color = TonedPanel, shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp)) {
                    Text("TOTAL PENGELUARAN", fontSize = 10.sp, letterSpacing = 1.4.sp, color = Green)
                    Text(rupiah(rows.sumOf { it.amount }), fontSize = 35.sp, letterSpacing = (-1.2).sp, modifier = Modifier.padding(top = 12.dp))
                    Text("${rows.size} pengeluaran tercatat", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Text("Rincian pengeluaran", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(top = 28.dp, bottom = 8.dp))
        }
        if (rows.isEmpty()) item { EmptyState("Belum ada pengeluaran", "Catatan pada periode ini akan muncul di sini.") }
        rows.groupBy { it.date }.forEach { (date, group) ->
            item(key = "date-$date") { Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(dayLabel(LocalDate.parse(date)), color = Muted, fontSize = 11.sp); Text(rupiah(group.sumOf { it.amount }), color = Muted, fontSize = 11.sp) } }
            items(group, key = { it.id }) { ExpenseRow(it, !s.busy, edit, delete) }
        }
    }
    if (chooseDate) DateChooser(anchor, { chooseDate = false }) { anchor = it }
}

@Composable
private fun DraftEditor(s: AppState, vm: AppViewModel, record: () -> Unit) {
    var chooseDate by remember { mutableStateOf(false) }
    val valid = s.drafts.isNotEmpty() && s.drafts.all { it.valid }
    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Pastikan nama dan nominalnya benar.", color = Muted, fontSize = 14.sp)
                TextButton(onClick = { chooseDate = true }, enabled = !s.busy, contentPadding = PaddingValues(0.dp), modifier = Modifier.padding(top = 12.dp)) { Icon(UiIcons.CalendarToday, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(dayLabel(s.date), fontSize = 13.sp) }
                if (s.transcript.isNotBlank()) Surface(color = SoftPanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Column(Modifier.padding(16.dp)) { Text("SUARA YANG TERBACA", fontSize = 9.sp, letterSpacing = 1.sp, color = Muted); Text(s.transcript, fontSize = 13.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp)) } }
            }
            items(s.drafts, key = { it.key }) { draft ->
                Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Line)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text("Pengeluaran ${s.drafts.indexOf(draft) + 1}", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f)); if (s.editing == null) IconButton(onClick = { vm.removeDraft(draft.key) }, enabled = !s.busy, modifier = Modifier.size(30.dp)) { Icon(UiIcons.Close, "Hapus baris", Modifier.size(17.dp)) } }
                        OutlinedTextField(value = draft.name, onValueChange = { vm.updateDraft(draft.key, name = it.take(200)) }, label = { Text("Nama pengeluaran") }, singleLine = true, enabled = !s.busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp))
                        OutlinedTextField(value = draft.amountText, onValueChange = { vm.updateDraft(draft.key, amount = it.filter(Char::isDigit).take(13)) }, label = { Text("Nominal rupiah") }, prefix = { Text("Rp ") }, singleLine = true, enabled = !s.busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = draft.amountText.isNotBlank() && draft.amount == null, supportingText = { Text(draft.warning ?: draft.amount?.let(::rupiah) ?: "Isi nominal lengkap, contoh 12000") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp))
                    }
                }
            }
            if (s.editing == null) item { OutlinedButton(onClick = vm::addDraft, enabled = !s.busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(UiIcons.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Tambah pengeluaran") } }
            if (s.transcript.isNotBlank()) item { TextButton(onClick = record, enabled = !s.busy, modifier = Modifier.fillMaxWidth()) { Icon(UiIcons.Mic, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Rekam ulang") } }
        }
        Surface(color = Paper, tonalElevation = 0.dp, shadowElevation = 3.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Total ${s.drafts.size} pengeluaran", color = Muted, fontSize = 12.sp); Text(rupiah(s.drafts.sumOf { it.amount ?: 0 }), fontWeight = FontWeight.SemiBold, fontSize = 20.sp) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = vm::back, enabled = !s.busy, modifier = Modifier.weight(1f)) { Text("Batal") }
                    Button(onClick = vm::save, enabled = valid && !s.busy, modifier = Modifier.weight(2f).height(54.dp), shape = RoundedCornerShape(18.dp)) { Text(if (s.busy) "Menyimpan…" else if (s.editing != null) "Simpan perubahan" else "Simpan semua", fontSize = 15.sp) }
                }
            }
        }
    }
    if (chooseDate) DateChooser(s.date, { chooseDate = false }, vm::setDate)
}

@Composable
private fun Settings(s: AppState, theme: (ThemeMode) -> Unit, export: () -> Unit, import: () -> Unit, share: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Catatanmu, di HP kamu.", fontSize = 26.sp, letterSpacing = (-.8).sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 22.dp))
            Text("Suara, pencatatan, dan laporan bekerja tanpa internet.", fontSize = 13.sp, color = Muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        }
        item {
            Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Line)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Tema tampilan", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(selected = s.themeMode == mode, onClick = { theme(mode) }, label = { Text(mode.label) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp))
                        }
                    }
                }
            }
        }
        item {
            Surface(color = Panel, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Line)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Cadangan & pemulihan", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text(if (s.backupAt == 0L) "Belum pernah disimpan ke file backup" else "Terakhir disimpan: ${Instant.ofEpochMilli(s.backupAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy, HH.mm", Indonesian))}", fontSize = 12.sp, color = Muted, lineHeight = 19.sp, modifier = Modifier.padding(top = 10.dp))
                    if (s.changedSinceBackup) Text("Ada perubahan yang belum dicadangkan.", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = export, enabled = !s.busy && !s.loading, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(50.dp), shape = RoundedCornerShape(15.dp)) { Icon(UiIcons.SaveAlt, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Cadangkan data") }
                    OutlinedButton(onClick = share, enabled = !s.busy && !s.loading, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(50.dp), shape = RoundedCornerShape(15.dp)) { Icon(UiIcons.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Bagikan backup") }
                    TextButton(onClick = import, enabled = !s.busy && !s.loading, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Icon(UiIcons.Restore, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Pulihkan data dari file") }
                }
            }
        }
        item { Surface(color = TonedPanel, shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(UiIcons.Info, null, tint = Green, modifier = Modifier.size(20.dp)); Text("Simpan salinan backup ke laptop, flashdisk, atau cloud saat ada internet. Salinan di luar HP membantu saat HP hilang atau rusak.", fontSize = 12.sp, lineHeight = 21.sp, color = Green) } } }
        item { Text("Notra · Versi ${BuildConfig.VERSION_NAME}", fontSize = 11.sp, color = Muted, modifier = Modifier.padding(top = 12.dp)) }
    }
}

@Composable
private fun RestoreScreen(s: AppState, vm: AppViewModel) {
    val backup = s.restore ?: return
    val plan = s.merge ?: return
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("Periksa sebelum dipulihkan.", fontSize = 25.sp, fontWeight = FontWeight.Medium, letterSpacing = (-.6).sp)
                Text("Backup ${Instant.ofEpochMilli(backup.exportedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy, HH.mm", Indonesian))}", fontSize = 13.sp, color = Muted, modifier = Modifier.padding(top = 8.dp))
                Surface(color = TonedPanel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("${plan.additions.size} catatan tambahan"); Text("${plan.unchanged} catatan sama, dilewati"); Text("${plan.conflicts.size} catatan memiliki versi berbeda") } }
                Text("Data sekarang tetap dipertahankan. Pilih versi backup pada catatan berbeda yang ingin kamu pulihkan.", fontSize = 13.sp, color = Muted, lineHeight = 21.sp, modifier = Modifier.padding(top = 18.dp))
            }
            items(plan.conflicts, key = { it.second.id }) { (current, incoming) ->
                Surface(color = Panel, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Line)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(current.name, fontWeight = FontWeight.Medium)
                        Text("Sekarang: ${if (current.deleted) "dihapus" else "${rupiah(current.amount)} · ${current.date}"}", color = Muted, fontSize = 12.sp)
                        Text("Backup: ${if (incoming.deleted) "dihapus" else "${incoming.name} · ${rupiah(incoming.amount)} · ${incoming.date}"}", color = Muted, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = incoming.id in s.restoreChoices, onCheckedChange = { vm.chooseConflict(incoming.id, it) }, enabled = !s.busy); Text("Gunakan versi backup", fontSize = 13.sp) }
                    }
                }
            }
        }
        Button(onClick = vm::restore, enabled = !s.busy, modifier = Modifier.fillMaxWidth().padding(24.dp).height(54.dp), shape = RoundedCornerShape(18.dp)) { Text(if (s.busy) "Memulihkan…" else "Konfirmasi pemulihan") }
    }
}
