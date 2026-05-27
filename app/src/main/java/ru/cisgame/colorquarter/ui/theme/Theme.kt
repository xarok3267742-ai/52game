package ru.cisgame.colorquarter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.cisgame.colorquarter.data.QuarterTile

val Ink = Color(0xFF17242E)
val MutedInk = Color(0xFF5E6872)
val Paper = Color(0xFFF7F6F0)
val Panel = Color(0xFFFFFFFF)
val Line = Color(0xFFE2E0D7)
val Success = Color(0xFF247C5C)
val Warning = Color(0xFFE16B3D)

private val ColorQuarterScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Color(0xFF2BB3A3),
    onSecondary = Ink,
    tertiary = Color(0xFFE85D75),
    background = Paper,
    onBackground = Ink,
    surface = Panel,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEDEBE2),
    onSurfaceVariant = MutedInk,
    outline = Line,
    error = Color(0xFFBA1A1A),
)

@Composable
fun ColorQuarterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorQuarterScheme,
        typography = AppTypography,
        content = content,
    )
}

fun QuarterTile.uiColor(highContrast: Boolean = false): Color {
    return when (this) {
        QuarterTile.Lagoon -> if (highContrast) Color(0xFF008778) else Color(0xFF2BB3A3)
        QuarterTile.Sun -> if (highContrast) Color(0xFFC98200) else Color(0xFFF2B84B)
        QuarterTile.Berry -> if (highContrast) Color(0xFFC93455) else Color(0xFFE85D75)
        QuarterTile.Mint -> if (highContrast) Color(0xFF3E935D) else Color(0xFF75C978)
        QuarterTile.Violet -> if (highContrast) Color(0xFF5362C7) else Color(0xFF6C7AE0)
    }
}

fun QuarterTile.uiMarkerColor(): Color {
    return when (this) {
        QuarterTile.Sun,
        QuarterTile.Mint -> Ink
        QuarterTile.Lagoon,
        QuarterTile.Berry,
        QuarterTile.Violet -> Color.White
    }
}
