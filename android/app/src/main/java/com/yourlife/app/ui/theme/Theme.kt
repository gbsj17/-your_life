package com.yourlife.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFFE5392B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2DD),
    onPrimaryContainer = Color(0xFF5A0F08),
    secondary = Color(0xFF3B4455),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E9F0),
    onSecondaryContainer = Color(0xFF1B2230),
    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF15171C),
    surface = Color.White,
    onSurface = Color(0xFF15171C),
    surfaceVariant = Color(0xFFECEEF3),
    onSurfaceVariant = Color(0xFF5E6573),
    outline = Color(0xFFD7DAE2),
    outlineVariant = Color(0xFFE4E7ED),
    error = Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6A57),
    onPrimary = Color(0xFF3A0905),
    primaryContainer = Color(0xFF4A1A14),
    onPrimaryContainer = Color(0xFFFFD9D3),
    secondary = Color(0xFFB7C0D1),
    onSecondary = Color(0xFF1B2230),
    secondaryContainer = Color(0xFF2A3140),
    onSecondaryContainer = Color(0xFFE2E7F1),
    background = Color(0xFF0E1013),
    onBackground = Color(0xFFF2F4F8),
    surface = Color(0xFF171A20),
    onSurface = Color(0xFFF2F4F8),
    surfaceVariant = Color(0xFF21252D),
    onSurfaceVariant = Color(0xFF9AA3B2),
    outline = Color(0xFF343A46),
    outlineVariant = Color(0xFF2A2F39),
    error = Color(0xFFFFB4AB)
)

/** Cores extras do app (que o Material não tem). */
@Immutable
class YlExtra(
    val success: Color,
    val onSuccess: Color,
    val heroBrush: Brush
)

private val LightExtra = YlExtra(
    success = Color(0xFF1E9E63),
    onSuccess = Color.White,
    heroBrush = Brush.linearGradient(listOf(Color(0xFFE5392B), Color(0xFFFF8A3D)))
)

private val DarkExtra = YlExtra(
    success = Color(0xFF3DD68C),
    onSuccess = Color(0xFF06301D),
    heroBrush = Brush.linearGradient(listOf(Color(0xFFC62E22), Color(0xFFE8702C)))
)

val LocalYlExtra = staticCompositionLocalOf { LightExtra }

object Yl {
    val extra: YlExtra
        @Composable get() = LocalYlExtra.current
}

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun YourLifeTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalYlExtra provides (if (dark) DarkExtra else LightExtra)) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
