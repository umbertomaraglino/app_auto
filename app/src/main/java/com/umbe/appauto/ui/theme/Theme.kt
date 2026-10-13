package com.umbe.appauto.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Brand colors
val Primary = Color(0xFF1E3A8A)
val PrimaryContainer = Color(0xFFDBEAFE)
val OnPrimaryContainer = Color(0xFF1E3A8A)
val Secondary = Color(0xFF0369A1)
val SecondaryContainer = Color(0xFFE0F2FE)
val Surface = Color(0xFFF8FAFC)
val Background = Color(0xFFF1F5F9)
val SurfaceVariant = Color(0xFFEEF2FF)

// Urgency colors
val UrgencyGreen = Color(0xFF166534)
val UrgencyGreenBg = Color(0xFFDCFCE7)
val UrgencyYellow = Color(0xFF854D0E)
val UrgencyYellowBg = Color(0xFFFEF9C3)
val UrgencyOrange = Color(0xFF9A3412)
val UrgencyOrangeBg = Color(0xFFFFEDD5)
val UrgencyRed = Color(0xFF991B1B)
val UrgencyRedBg = Color(0xFFFEE2E2)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = Color(0xFF0C4A6E),
    surface = Surface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    background = Background,
    onBackground = Color(0xFF0F172A),
    outline = Color(0xFF94A3B8),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = UrgencyRedBg,
    onErrorContainer = UrgencyRed,
)

@Composable
fun AppAutoTheme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(LocalContext.current)
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
