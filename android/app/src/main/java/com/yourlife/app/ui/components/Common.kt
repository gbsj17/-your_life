@file:OptIn(ExperimentalMaterial3Api::class)

package com.yourlife.app.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yourlife.app.ui.icons.AppIcons

fun fmtClock(totalSeconds: Int): String = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

@Composable
fun AppIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    Icon(imageVector = icon, contentDescription = null, modifier = modifier.size(size), tint = tint)
}

/** Botão de ícone redondo (voltar, fechar, pausar...). */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        color = container,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        if (icon != null) {
            AppIcon(icon, size = 20.dp)
            Box(Modifier.size(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        if (icon != null) {
            AppIcon(icon, size = 20.dp)
            Box(Modifier.size(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun InfoChip(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null, highlight: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (highlight) scheme.primaryContainer else scheme.surfaceVariant
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                AppIcon(icon, size = 14.dp, tint = if (highlight) scheme.primary else scheme.onSurfaceVariant)
            }
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = if (highlight) scheme.onPrimaryContainer else scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = modifier)
}

/** Cartão padrão: superfície com borda fina. Clicável quando `onClick` é informado. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = border,
            content = content
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = border,
            content = content
        )
    }
}

/** Bolha colorida com ícone (usada nos cartões de treino e nos ajustes). */
@Composable
fun IconBubble(icon: ImageVector, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon, tint = MaterialTheme.colorScheme.primary, size = size * 0.5f)
    }
}

@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Anel de contagem regressiva; `progress` vai de 1 (cheio) a 0 (vazio). */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val color = MaterialTheme.colorScheme.primary
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = strokeWidth.toPx()
            val inset = w / 2f
            val arcSize = Size(size.width - w, size.height - w)
            drawArc(
                color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width = w)
            )
            drawArc(
                color = color, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = Stroke(width = w, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

@Composable
fun ClickableRow(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.clickable(onClick = onClick)) { content() }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    RoundIconButton(AppIcons.Back, "Voltar", onClick)
}

/** Seletor de nível (Iniciante / Médio / Avançado). */
@Composable
fun LevelChips(selected: com.yourlife.app.data.Level, onSelect: (com.yourlife.app.data.Level) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        com.yourlife.app.data.Level.entries.forEach { lv ->
            val on = lv == selected
            Surface(
                onClick = { onSelect(lv) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = if (on) scheme.primary else scheme.surface,
                border = BorderStroke(1.dp, if (on) scheme.primary else scheme.outlineVariant)
            ) {
                Box(Modifier.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                    Text(
                        lv.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (on) scheme.onPrimary else scheme.onSurface
                    )
                }
            }
        }
    }
}

/** Barra de progresso simples (0..1). */
@Composable
fun BarProgress(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(
            Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(8.dp)
                .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary)
        )
    }
}
