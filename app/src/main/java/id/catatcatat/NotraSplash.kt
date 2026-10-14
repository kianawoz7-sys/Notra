package id.catatcatat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NotraSplash(onFinished: () -> Unit) {
    val clock = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        clock.animateTo(1f, tween(1650, easing = LinearEasing))
        finish()
    }
    val foreground = MaterialTheme.colorScheme.onBackground
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = "Membuka Notra" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(96.dp)) {
            val elapsed = clock.value
            val morph = ((elapsed - .25f) / .30f).coerceIn(0f, 1f)
            val eased = morph * morph * (3 - 2 * morph)
            val rotation = ((elapsed - .55f) / .40f).coerceIn(0f, 1f) * 2 * PI.toFloat()
            val fade = (1 - ((elapsed - .93f) / .07f).coerceIn(0f, 1f))
            val spacing = 18.dp.toPx()
            for (i in 0..2) {
                val angle = -PI.toFloat() / 2 + i * 2 * PI.toFloat() / 3 + rotation
                val line = Offset(center.x + (i - 1) * spacing, center.y)
                val circle = Offset(center.x + cos(angle) * spacing, center.y + sin(angle) * spacing)
                val position = line * (1 - eased) + circle * eased
                val pulse = if (elapsed < .25f) .72f + .28f * sin(elapsed * 4 * PI.toFloat() - i * .7f) else 1f
                drawCircle(foreground.copy(alpha = fade * (.70f + i * .15f)), 3.5.dp.toPx() * pulse, position)
            }
        }
    }
}
