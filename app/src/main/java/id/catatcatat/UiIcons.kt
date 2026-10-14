package id.catatcatat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

object UiIcons {
    private fun line(name: String, path: String) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).addPath(addPathNodes(path), stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round).build()
    val ArrowBack by lazy { line("Kembali", "M19 12H5 M11 6L5 12L11 18") }
    val Menu by lazy { line("Laporan", "M5 8H19 M5 16H14") }
    val Tune by lazy { line("Pengaturan", "M4 7H8 M14 7H20 M4 17H14 M20 17H20 M14 7a3 3 0 1 1,-6 0a3 3 0 1 1,6 0 M20 17a3 3 0 1 1,-6 0a3 3 0 1 1,6 0") }
    val Home by lazy { line("Catat", "M3 10L12 3L21 10 M5 9V20H10V14H14V20H19V9") }
    val BarChart by lazy { line("Laporan", "M4 20H20 M7 16V11 M12 16V5 M17 16V8") }
    val ReceiptLong by lazy { line("Catatan", "M6 3L8 5L10 3L12 5L14 3L16 5L18 3V21L16 19L14 21L12 19L10 21L8 19L6 21Z M9 9H15 M9 13H15") }
    val Keyboard by lazy { line("Manual", "M3 6H21V18H3Z M6 10H6.1 M10 10H10.1 M14 10H14.1 M18 10H18.1 M6 14H6.1 M10 14H17") }
    val MoreVert by lazy { line("Pilihan", "M12 5H12.1 M12 12H12.1 M12 19H12.1") }
    val Close by lazy { line("Batal", "M6 6L18 18 M18 6L6 18") }
    val ChevronLeft by lazy { line("Sebelumnya", "M15 6L9 12L15 18") }
    val ChevronRight by lazy { line("Berikutnya", "M9 6L15 12L9 18") }
    val CalendarToday by lazy { line("Tanggal", "M4 5H20V21H4Z M4 10H20 M8 3V7 M16 3V7") }
    val Add by lazy { line("Tambah", "M5 12H19 M12 5V19") }
    val Mic by lazy { line("Suara", "M9 5a3 3 0 0 1,6 0V11a3 3 0 0 1,-6 0Z M5 10V11a7 7 0 0 0,14 0V10 M12 18V21 M9 21H15") }
    val Stop by lazy { line("Selesai", "M6 6H18V18H6Z") }
    val SaveAlt by lazy { line("Cadangkan", "M12 3V15 M7 10L12 15L17 10 M4 16V21H20V16") }
    val Share by lazy { line("Bagikan", "M8 11L16 6 M8 13L16 18 M8 12a3 3 0 1 1,-6 0a3 3 0 1 1,6 0 M22 4a3 3 0 1 1,-6 0a3 3 0 1 1,6 0 M22 20a3 3 0 1 1,-6 0a3 3 0 1 1,6 0") }
    val Restore by lazy { line("Pulihkan", "M3 4V10H9 M3 10a9 9 0 1 1,1 8 M12 7V12L15 14") }
    val Info by lazy { line("Informasi", "M22 12a10 10 0 1 1,-20 0a10 10 0 1 1,20 0 M12 11V17 M12 7H12.1") }
}
