package id.catatcatat

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CaptureScreen(s: AppState, today: LocalDate, record: () -> Unit, vm: AppViewModel) {
    val active = s.page == Page.VOICE
    val recording = active && s.voicePhase == VoicePhase.RECORDING
    val failed = active && s.voicePhase == VoicePhase.FAILED
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        val expandedSize = minOf(268.dp, maxWidth * .70f, maxHeight * .45f)
        val targetSize = if (active) expandedSize else minOf(112.dp, maxWidth * .30f)
        val orbSize by animateDpAsState(targetSize, tween(520, easing = FastOutSlowInEasing), label = "orb-expansion")
        val targetBottom = if (active) maxOf(154.dp, (maxHeight - expandedSize) / 2) else 118.dp
        val orbBottom by animateDpAsState(targetBottom, tween(520, easing = FastOutSlowInEasing), label = "orb-position")
        if (!active) Text(
            today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
        val orbAction = if (recording) vm::stopRecording else record
        val orbLabel = when {
            !active -> "Catat lewat suara"
            recording -> "Selesai bicara. Rekaman ${s.seconds} detik"
            failed -> "Rekam ulang"
            else -> "Memproses suara, ${s.processingSeconds} detik"
        }
        val canTapOrb = !s.loading && (if (!active) !s.busy else recording || failed)
        Box(
            Modifier.align(Alignment.BottomCenter).padding(bottom = orbBottom)
                .size(orbSize).clip(CircleShape)
                .semantics { contentDescription = orbLabel }
                .clickable(enabled = canTapOrb, role = Role.Button, onClick = orbAction),
            contentAlignment = Alignment.Center
        ) {
            VoiceOrb(orbSize, active = active && !failed, level = if (recording) s.level else .10f)
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (active) {
                if (failed) Text(
                    s.voiceError.orEmpty(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)
                )
                when {
                    recording -> GlassButton("Selesai bicara", icon = UiIcons.Stop, showLabel = false, modifier = Modifier.size(56.dp), onClick = vm::stopRecording)
                    failed -> GlassButton("Rekam ulang", icon = UiIcons.Mic, showLabel = false, modifier = Modifier.size(56.dp), onClick = record)
                    else -> Box(Modifier.size(56.dp).background(Color(0xFF17191A), CircleShape).semantics { contentDescription = "Memproses suara, ${s.processingSeconds} detik. Batas waktu 45 detik." }, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(25.dp), color = Color.White, strokeWidth = 2.dp)
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
            GlassButton(
                "Isi manual", enabled = !s.loading && (active || !s.busy),
                modifier = Modifier.fillMaxWidth().height(58.dp),
                onClick = if (active) vm::voiceToManual else vm::newManual
            )
        }
    }
}

@Composable
private fun GlassButton(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showLabel: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier.shadow(9.dp, shape, ambientColor = Color.Black.copy(alpha = .09f), spotColor = Color.Black.copy(alpha = .12f))
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF383A3B), Color(0xFF0C0D0E), Color(0xFF131516), Color(0xFF292C2D))))
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { if (!showLabel) contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val corner = CornerRadius(size.height / 2)
            drawRoundRect(
                Brush.radialGradient(listOf(Color.White.copy(alpha = .24f), Color.Transparent), Offset(size.width * .2f, -size.height * .4f), size.width * .55f),
                cornerRadius = corner
            )
            val stroke = 1.dp.toPx()
            drawRoundRect(
                Brush.verticalGradient(listOf(Color.White.copy(alpha = .65f), Color.White.copy(alpha = .10f), Color.White.copy(alpha = .34f))),
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                cornerRadius = corner, style = Stroke(stroke)
            )
            drawRoundRect(
                Color.White.copy(alpha = .055f), topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                cornerRadius = corner, style = Stroke(.5.dp.toPx())
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            icon?.let { Icon(it, null, Modifier.size(22.dp), tint = Color.White.copy(alpha = if (enabled) .92f else .40f)) }
            if (showLabel) {
                if (icon != null) Spacer(Modifier.width(8.dp))
                Text(label, color = Color.White.copy(alpha = if (enabled) .95f else .40f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
