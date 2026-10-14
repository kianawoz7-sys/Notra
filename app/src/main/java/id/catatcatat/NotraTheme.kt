package id.catatcatat

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode(val label: String) {
    LIGHT("Terang"), DARK("Gelap"), SYSTEM("Sistem");
    companion object {
        fun fromPreference(value: String?) = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

fun applyApplicationTheme(context: Context, mode: ThemeMode) {
    if (Build.VERSION.SDK_INT >= 31) {
        // AUTO clears the application's night override, allowing the device
        // configuration to supply its -night qualifier again.
        val night = when (mode) {
            ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
            ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
        }
        context.getSystemService(UiModeManager::class.java)?.setApplicationNightMode(night)
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF202625), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8ECE6), onPrimaryContainer = Color(0xFF202625),
    secondary = Color(0xFF466454), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDF1EA), onSecondaryContainer = Color(0xFF466454),
    tertiary = Color(0xFF8F6D35),
    background = Color(0xFFFCFCFC), onBackground = Color(0xFF202625),
    surface = Color.White, onSurface = Color(0xFF202625),
    surfaceVariant = Color(0xFFF0F2ED), onSurfaceVariant = Color(0xFF777F7B),
    outline = Color(0xFFBBC3BD), outlineVariant = Color(0xFFE7EAE5),
    surfaceTint = Color(0xFF466454)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFEAEFEC), onPrimary = Color(0xFF171B19),
    primaryContainer = Color(0xFF303A34), onPrimaryContainer = Color(0xFFE4ECE6),
    secondary = Color(0xFFB2C8B9), onSecondary = Color(0xFF18271E),
    secondaryContainer = Color(0xFF25332B), onSecondaryContainer = Color(0xFFC1D5C8),
    tertiary = Color(0xFFDFC597),
    background = Color(0xFF101214), onBackground = Color(0xFFEEF1EF),
    surface = Color(0xFF1A1D1F), onSurface = Color(0xFFEEF1EF),
    surfaceVariant = Color(0xFF262C29), onSurfaceVariant = Color(0xFFA9B3AD),
    outline = Color(0xFF65726A), outlineVariant = Color(0xFF343D37),
    surfaceTint = Color(0xFFB2C8B9)
)

@Composable
fun NotraTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current
    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            @Suppress("DEPRECATION")
            window.statusBarColor = colors.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colors.background.toArgb()
            if (Build.VERSION.SDK_INT >= 29) {
                window.isNavigationBarContrastEnforced = false
                view.isForceDarkAllowed = false
            }
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}
