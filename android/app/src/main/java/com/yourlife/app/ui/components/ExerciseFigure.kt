package com.yourlife.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.yourlife.app.data.ANIM_GEOMETRY
import com.yourlife.app.data.Exercise
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// Mesmos comprimentos usados em tools/gen_poses.py
private const val UA = 12f
private const val FA = 11f
private const val TH = 15f
private const val SH = 15f
private const val TORSO = 22f
private const val NECK = 4f
private const val HEAD_R = 5.5f
private const val FOOT = 5.5f

private fun rad(deg: Float): Float = deg * (PI.toFloat() / 180f)

/** Cinemática inversa de 2 ossos. Retorna [juntaX, juntaY, pontaX, pontaY]. */
private fun ik(rx: Float, ry: Float, tx: Float, ty: Float, l1: Float, l2: Float, bend: Float): FloatArray {
    val dx = tx - rx
    val dy = ty - ry
    val dist = max(hypot(dx, dy), 1e-6f)
    val dd = min(max(dist, abs(l1 - l2) + 0.05f), l1 + l2 - 0.05f)
    val ux = dx / dist
    val uy = dy / dist
    val a = (l1 * l1 - l2 * l2 + dd * dd) / (2f * dd)
    val h = sqrt(max(0f, l1 * l1 - a * a))
    return floatArrayOf(
        rx + ux * a - uy * h * bend,
        ry + uy * a + ux * h * bend,
        rx + ux * dd,
        ry + uy * dd
    )
}

/** Mistura as poses A e B (os sinais de dobra, índices 6, 9, 12 e 15, não são interpolados). */
private fun mix(a: FloatArray, b: FloatArray, t: Float): FloatArray {
    val o = FloatArray(18)
    for (i in 0 until 18) {
        o[i] = if (i == 6 || i == 9 || i == 12 || i == 15) a[i] else a[i] + (b[i] - a[i]) * t
    }
    return o
}

/**
 * Boneco articulado que repete o movimento do exercício (pose A -> pose B -> pose A).
 * Quando `animate` é falso, mostra só a pose inicial.
 */
@Composable
fun ExerciseFigure(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    showPhase: Boolean = true
) {
    val geo = ANIM_GEOMETRY[exercise.anim] ?: return
    val scheme = MaterialTheme.colorScheme
    val near = scheme.primary
    val far = scheme.primary.copy(alpha = 0.42f)
    val propColor = scheme.outline
    val groundColor = scheme.outline

    val progress: State<Float> = if (animate) {
        val transition = rememberInfiniteTransition(label = "figure")
        val move = exercise.cycleMs
        val hold = 260
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2 * move + 2 * hold
                    0f at 0 using FastOutSlowInEasing
                    1f at move
                    1f at (move + hold) using FastOutSlowInEasing
                    0f at (2 * move + hold)
                    0f at (2 * move + 2 * hold)
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "t"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val inB by remember { derivedStateOf { progress.value >= 0.5f } }

    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceVariant)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawFigure(geo.props, mix(geo.a, geo.b, progress.value), near, far, propColor, groundColor)
        }
        if (showPhase) {
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                shape = RoundedCornerShape(50),
                color = scheme.surface.copy(alpha = 0.85f)
            ) {
                Text(
                    text = if (inB) exercise.phaseB else exercise.phaseA,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun DrawScope.drawFigure(
    props: List<com.yourlife.app.data.Prop>,
    p: FloatArray,
    near: androidx.compose.ui.graphics.Color,
    far: androidx.compose.ui.graphics.Color,
    propColor: androidx.compose.ui.graphics.Color,
    groundColor: androidx.compose.ui.graphics.Color
) {
    val s = size.width / 100f
    val groundY = size.height - 6f * s
    fun pt(x: Float, y: Float) = Offset(x * s, groundY - y * s)

    // chão
    drawLine(groundColor, Offset(0f, groundY), Offset(size.width, groundY), strokeWidth = 2f * s * 0.5f)

    // objetos de cena
    props.forEach { pr ->
        if (pr.kind == 0) {
            val tl = pt(pr.x0, pr.y1)
            val br = pt(pr.x1, pr.y0)
            drawRect(propColor, topLeft = tl, size = Size(br.x - tl.x, br.y - tl.y))
        } else {
            drawLine(propColor, pt(pr.x0, pr.y0), pt(pr.x1, pr.y1), strokeWidth = 3f * s)
        }
    }

    val hipX = p[0]
    val hipY = p[1]
    val shX = hipX + TORSO * cos(rad(p[2]))
    val shY = hipY + TORSO * sin(rad(p[2]))
    val hAng = p[2] + p[3]
    val neckX = shX + NECK * cos(rad(hAng))
    val neckY = shY + NECK * sin(rad(hAng))
    val headX = shX + (NECK + HEAD_R) * cos(rad(hAng))
    val headY = shY + (NECK + HEAD_R) * sin(rad(hAng))

    val armN = ik(shX, shY, p[4], p[5], UA, FA, p[6])
    val armF = ik(shX, shY, p[7], p[8], UA, FA, p[9])
    val legN = ik(hipX, hipY, p[10], p[11], TH, SH, p[12])
    val legF = ik(hipX, hipY, p[13], p[14], TH, SH, p[15])

    val limbW = 3.4f * s
    val torsoW = 4.4f * s

    fun limb(root: Offset, l: FloatArray, color: androidx.compose.ui.graphics.Color) {
        val j = pt(l[0], l[1])
        val e = pt(l[2], l[3])
        drawLine(color, root, j, strokeWidth = limbW, cap = StrokeCap.Round)
        drawLine(color, j, e, strokeWidth = limbW, cap = StrokeCap.Round)
    }

    fun foot(l: FloatArray, angle: Float, color: androidx.compose.ui.graphics.Color) {
        val e = pt(l[2], l[3])
        val t = pt(l[2] + FOOT * cos(rad(angle)), l[3] + FOOT * sin(rad(angle)))
        drawLine(color, e, t, strokeWidth = limbW * 0.9f, cap = StrokeCap.Round)
    }

    val hipO = pt(hipX, hipY)
    val shO = pt(shX, shY)

    // membros do lado de trás (mais claros)
    limb(shO, armF, far)
    limb(hipO, legF, far)
    foot(legF, p[17], far)

    // tronco e pescoço
    drawLine(near, hipO, shO, strokeWidth = torsoW, cap = StrokeCap.Round)
    drawLine(near, shO, pt(neckX, neckY), strokeWidth = limbW, cap = StrokeCap.Round)

    // membros do lado da frente
    limb(hipO, legN, near)
    foot(legN, p[16], near)
    limb(shO, armN, near)

    // cabeça
    drawCircle(near, radius = HEAD_R * s, center = pt(headX, headY))
}
