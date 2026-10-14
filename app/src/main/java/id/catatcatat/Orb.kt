package id.catatcatat

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun VoiceOrb(size: Dp = 190.dp, active: Boolean = false, level: Float = 0f, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "orb")
    val phase by motion.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(if (active) 4200 else 10000, easing = LinearEasing)), label = "cloud")
    val amplitude by animateFloatAsState(if (active) level else 0.04f, tween(180), label = "voice-level")
    Canvas(modifier.size(size)) {
        val radius = this.size.minDimension / 2 * (0.94f + amplitude * 0.04f)
        val center = this.center
        val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center - Offset(radius, radius), center + Offset(radius, radius))) }
        drawCircle(Brush.radialGradient(listOf(Color(0x14000000), Color.Transparent), center + Offset(0f, radius * .35f), radius * 1.12f), radius * 1.1f, center + Offset(0f, radius * .15f))
        clipPath(circle) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF202425), Color(0xFF353A3D), Color(0xFFB8BFC3), Color(0xFFE3E7E9)), startY = center.y - radius, endY = center.y + radius))
            val wave = Path().apply {
                moveTo(center.x - radius, center.y + radius)
                lineTo(center.x - radius, center.y)
                for (i in 0..90) {
                    val x = center.x - radius + radius * 2 * i / 90f
                    val y = center.y + radius * (.14f + .12f * sin(i * .055f + phase) + .04f * cos(i * .19f - phase * 1.2f))
                    lineTo(x, y)
                }
                lineTo(center.x + radius, center.y + radius)
                close()
            }
            drawPath(wave, Brush.verticalGradient(listOf(Color.Transparent, Color(0x88EFF1F2)), center.y - radius * .05f, center.y + radius))
            for (i in 0..25) {
                val x = center.x - radius + radius * 2 * i / 25f
                val drift = sin(i * 1.8f + phase) * radius * .06f
                val y = center.y + radius * (.06f + .12f * sin(i * .20f + phase)) + drift
                val fogRadius = radius * (.17f + .07f * sin(i * 2.4f + phase).absoluteValue + amplitude * .04f)
                drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = .23f), Color.White.copy(alpha = .07f), Color.Transparent), Offset(x, y), fogRadius), fogRadius, Offset(x, y))
            }
            drawCircle(Brush.radialGradient(listOf(Color(0x00FFFFFF), Color(0x16FFFFFF)), center - Offset(radius * .25f, radius * .25f), radius * 2), radius, center)
        }
    }
}
