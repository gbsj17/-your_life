package com.yourlife.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.yourlife.app.data.PlanIcon

/**
 * Conjunto de ícones próprio do app, desenhado em grade 24 x 24 (traço arredondado de 2).
 * Cada ícone é definido por comandos de caminho SVG; a cor vem do `tint` de quem usa.
 */
object AppIcons {

    private fun icon(name: String, vararg strokes: String, filled: String? = null): ImageVector {
        val b = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        )
        strokes.forEach { p ->
            b.addPath(
                pathData = addPathNodes(p),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
        if (filled != null) {
            b.addPath(pathData = addPathNodes(filled), fill = SolidColor(Color.Black))
        }
        return b.build()
    }

    val Dumbbell = icon(
        "Dumbbell",
        "M6.5 6.5 L6.5 17.5", "M17.5 6.5 L17.5 17.5",
        "M3.5 9 L3.5 15", "M20.5 9 L20.5 15",
        "M6.5 12 L17.5 12"
    )

    val Flame = icon(
        "Flame",
        "M12 3 C13 6.5 17 8.2 17 13 A5 5 0 0 1 7 13 C7 11 8 9.8 9 8.8 C9 10.3 9.7 11 10.5 11.5 C10.8 8.2 11 5.5 12 3 Z"
    )

    val Bolt = icon("Bolt", "M13 2 L4 14 L11 14 L10 22 L19 10 L12 10 Z")

    val Target = icon(
        "Target",
        "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 Z",
        "M12 7.5 A4.5 4.5 0 1 0 12 16.5 A4.5 4.5 0 1 0 12 7.5 Z",
        "M12 12 L12.01 12"
    )

    val Shield = icon("Shield", "M12 3 L19 6 L19 11 C19 16 16 19.5 12 21 C8 19.5 5 16 5 11 L5 6 Z", "M9 12 L11 14 L15 10")

    val Peak = icon("Peak", "M3 19 L9 8 L13 14 L15.5 10.5 L21 19 Z")

    val Chart = icon("Chart", "M5 20 L5 13", "M12 20 L12 5", "M19 20 L19 10")

    val Sliders = icon(
        "Sliders",
        "M4 7 L20 7", "M4 17 L20 17",
        "M9 4 L9 10", "M15 14 L15 20"
    )

    val Home = icon("Home", "M4 11 L12 4 L20 11", "M6 10 L6 20 L18 20 L18 10", "M10 20 L10 14 L14 14 L14 20")

    val Play = icon("Play", "M8 5 L8 19 L19 12 Z", filled = "M8 5 L8 19 L19 12 Z")

    val Pause = icon("Pause", "M8 5 L8 19", "M16 5 L16 19")

    val Check = icon("Check", "M5 12.5 L9.5 17 L19 7")

    val Timer = icon(
        "Timer",
        "M12 6 A8 8 0 1 0 12 22 A8 8 0 1 0 12 6 Z",
        "M12 10 L12 14", "M10 2 L14 2", "M12 6 L12 2"
    )

    val Back = icon("Back", "M15 5 L8 12 L15 19")

    val Close = icon("Close", "M6 6 L18 18", "M18 6 L6 18")

    val Chevron = icon("Chevron", "M9 5 L16 12 L9 19")

    val Info = icon("Info", "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 Z", "M12 11 L12 16", "M12 8 L12.01 8")

    val Plus = icon("Plus", "M12 5 L12 19", "M5 12 L19 12")

    val Minus = icon("Minus", "M5 12 L19 12")

    val Skip = icon("Skip", "M5 5 L13 12 L5 19 Z", "M18 5 L18 19")

    val Sun = icon(
        "Sun",
        "M12 8 A4 4 0 1 0 12 16 A4 4 0 1 0 12 8 Z",
        "M12 2 L12 4", "M12 20 L12 22", "M2 12 L4 12", "M20 12 L22 12",
        "M4.9 4.9 L6.3 6.3", "M17.7 17.7 L19.1 19.1", "M4.9 19.1 L6.3 17.7", "M17.7 6.3 L19.1 4.9"
    )

    val Moon = icon("Moon", "M20 14.5 A8 8 0 0 1 9.5 4 A8 8 0 1 0 20 14.5 Z")

    val Phone = icon(
        "Phone",
        "M7 2 L17 2 A1 1 0 0 1 18 3 L18 21 A1 1 0 0 1 17 22 L7 22 A1 1 0 0 1 6 21 L6 3 A1 1 0 0 1 7 2 Z",
        "M11 18 L13 18"
    )

    val Heart = icon(
        "Heart",
        "M12 20 C12 20 5 15.6 5 10 A4 4 0 0 1 12 7.4 A4 4 0 0 1 19 10 C19 15.6 12 20 12 20 Z"
    )

    val Sync = icon(
        "Sync",
        "M20 11 A8 8 0 0 0 5.5 7.5", "M4 4 L4 8 L8 8",
        "M4 13 A8 8 0 0 0 18.5 16.5", "M20 20 L20 16 L16 16"
    )

    val Trophy = icon(
        "Trophy",
        "M8 4 L16 4 L16 9 A4 4 0 0 1 8 9 Z",
        "M8 6 L4 6 L4 7 A3 3 0 0 0 8 10", "M16 6 L20 6 L20 7 A3 3 0 0 1 16 10",
        "M12 13 L12 17", "M8 20 L16 20", "M10 17 L14 17"
    )

    val Calendar = icon(
        "Calendar",
        "M5 5 L19 5 A1 1 0 0 1 20 6 L20 19 A1 1 0 0 1 19 20 L5 20 A1 1 0 0 1 4 19 L4 6 A1 1 0 0 1 5 5 Z",
        "M4 10 L20 10", "M8 3 L8 7", "M16 3 L16 7"
    )

    val Trash = icon("Trash", "M4 7 L20 7", "M9 7 L9 4 L15 4 L15 7", "M6 7 L7 20 L17 20 L18 7", "M10 11 L10 16", "M14 11 L14 16")

    fun forPlan(icon: PlanIcon): ImageVector = when (icon) {
        PlanIcon.FLAME -> Flame
        PlanIcon.DUMBBELL -> Dumbbell
        PlanIcon.BOLT -> Bolt
        PlanIcon.TARGET -> Target
        PlanIcon.SHIELD -> Shield
        PlanIcon.PEAK -> Peak
    }
}
