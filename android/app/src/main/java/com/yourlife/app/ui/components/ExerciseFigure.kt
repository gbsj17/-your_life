package com.yourlife.app.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
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
    val palette = Palette(shirt = scheme.primary)
    val propColor = scheme.outline
    val groundColor = scheme.outline

    val progress: State<Float> = if (animate) {
        val transition = rememberInfiniteTransition(label = "figure")
        val move = exercise.cycleMs
        val hold = 320
        val ease = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 2 * move + 2 * hold
                    0f at 0 using ease
                    1f at move
                    1f at (move + hold) using ease
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
            drawFigure(geo.props, mix(geo.a, geo.b, progress.value), palette, propColor, groundColor, geo.front)
        }
        if (showPhase) {
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                shape = RoundedCornerShape(50),
                color = scheme.surface.copy(alpha = 0.85f)
            ) {
                AnimatedContent(
                    targetState = if (inB) exercise.phaseB else exercise.phaseA,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "phase"
                ) { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private class Palette(shirt: Color) {
    val shirt = shirt
    val shirtFar = lerp(shirt, Color.Black, 0.28f)
    val skin = Color(0xFFE2AE8E)
    val skinFar = Color(0xFFC48E70)
    val shorts = Color(0xFF2C3442)
    val shortsFar = Color(0xFF1F2530)
    val shoe = Color(0xFFF1F3F7)
    val shoeFar = Color(0xFFC9CED8)
    val hair = Color(0xFF3B2A22)
}

/** Direção da luz na tela (de cima, um pouco da esquerda), para dar volume aos membros. */
private val LIGHT = Offset(-0.45f, -0.9f)

/** Cápsula afunilada (de A até B, com raios ra e rb) com realce e sombra para parecer volumétrica. */
private fun DrawScope.capsule(a: Offset, b: Offset, ra: Float, rb: Float, color: Color, volume: Boolean = true) {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = max(hypot(dx, dy), 1e-3f)
    val nx = -dy / len
    val ny = dx / len
    fun body(pa: Offset, pb: Offset, r1: Float, r2: Float, c: Color) {
        val path = Path().apply {
            moveTo(pa.x + nx * r1, pa.y + ny * r1)
            lineTo(pb.x + nx * r2, pb.y + ny * r2)
            lineTo(pb.x - nx * r2, pb.y - ny * r2)
            lineTo(pa.x - nx * r1, pa.y - ny * r1)
            close()
        }
        drawPath(path, c)
        drawCircle(c, r1, pa)
        drawCircle(c, r2, pb)
    }
    body(a, b, ra, rb, color)
    if (volume) {
        // lado voltado para a luz recebe um realce; o oposto, uma sombra
        val toward = if (nx * LIGHT.x + ny * LIGHT.y >= 0f) 1f else -1f
        val sx = nx * toward
        val sy = ny * toward
        body(
            Offset(a.x + sx * ra * 0.38f, a.y + sy * ra * 0.38f),
            Offset(b.x + sx * rb * 0.38f, b.y + sy * rb * 0.38f),
            ra * 0.38f, rb * 0.38f, Color.White.copy(alpha = 0.20f)
        )
        body(
            Offset(a.x - sx * ra * 0.55f, a.y - sy * ra * 0.55f),
            Offset(b.x - sx * rb * 0.55f, b.y - sy * rb * 0.55f),
            ra * 0.4f, rb * 0.4f, Color.Black.copy(alpha = 0.12f)
        )
    }
}

private fun lerpOff(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

/** Tronco: contorno com ombros, peito, cintura e quadril, entre o quadril (a) e o ombro (b). */
private val SIDE_PROFILE = listOf(0f to 5.0f, 0.22f to 5.2f, 0.5f to 4.5f, 0.8f to 5.6f, 1f to 4.3f)
private val FRONT_PROFILE = listOf(0f to 5.4f, 0.25f to 5.0f, 0.55f to 5.6f, 0.85f to 6.9f, 1f to 6.2f)

private fun DrawScope.torso(
    a: Offset, b: Offset, s: Float, t0: Float, t1: Float, color: Color,
    prof: List<Pair<Float, Float>> = SIDE_PROFILE
) {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val len = max(hypot(dx, dy), 1e-3f)
    val nx = -dy / len
    val ny = dx / len
    // (posição ao longo da coluna, meia largura) – em unidades do quadro
    fun radiusAt(t: Float): Float {
        for (k in 0 until prof.size - 1) {
            val (ta, ra) = prof[k]
            val (tb, rb) = prof[k + 1]
            if (t <= tb) {
                val u = ((t - ta) / (tb - ta)).coerceIn(0f, 1f)
                val e = u * u * (3f - 2f * u)
                return (ra + (rb - ra) * e) * s
            }
        }
        return prof.last().second * s
    }
    val steps = 14
    val left = ArrayList<Offset>()
    val right = ArrayList<Offset>()
    for (i in 0..steps) {
        val t = t0 + (t1 - t0) * i / steps
        val c = lerpOff(a, b, t)
        val r = radiusAt(t)
        left.add(Offset(c.x + nx * r, c.y + ny * r))
        right.add(Offset(c.x - nx * r, c.y - ny * r))
    }
    val path = Path().apply {
        moveTo(left[0].x, left[0].y)
        left.drop(1).forEach { lineTo(it.x, it.y) }
        right.asReversed().forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, color)
    drawCircle(color, radiusAt(t0), lerpOff(a, b, t0))
    drawCircle(color, radiusAt(t1), lerpOff(a, b, t1))
}

private fun DrawScope.drawFigure(
    props: List<com.yourlife.app.data.Prop>,
    p: FloatArray,
    pal: Palette,
    propColor: Color,
    groundColor: Color,
    front: Boolean = false
) {
    val s = size.width / 100f
    val groundY = size.height - 6f * s
    fun pt(x: Float, y: Float) = Offset(x * s, groundY - y * s)

    // chão
    drawLine(groundColor, Offset(0f, groundY), Offset(size.width, groundY), strokeWidth = 1f * s)

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

    if (front) {
        drawFrontBody(::pt, s, groundY, p, pal)
        return
    }

    val hipX = p[0]
    val hipY = p[1]
    val shX = hipX + TORSO * cos(rad(p[2]))
    val shY = hipY + TORSO * sin(rad(p[2]))
    val hAng = p[2] + p[3]
    val headX = shX + (NECK + HEAD_R) * cos(rad(hAng))
    val headY = shY + (NECK + HEAD_R) * sin(rad(hAng))

    val armN = ik(shX, shY, p[4], p[5], UA, FA, p[6])
    val armF = ik(shX, shY, p[7], p[8], UA, FA, p[9])
    val legN = ik(hipX, hipY, p[10], p[11], TH, SH, p[12])
    val legF = ik(hipX, hipY, p[13], p[14], TH, SH, p[15])

    val hipO = pt(hipX, hipY)
    val shO = pt(shX, shY)

    // sombra de contato no chão, do tamanho do corpo
    val xs = listOf(hipX, shX, headX, legN[2], legF[2], armN[2], armF[2])
    val x0 = xs.min()
    val x1 = xs.max()
    drawOval(
        Color.Black.copy(alpha = 0.13f),
        topLeft = Offset((x0 - 1f) * s, groundY - 1.2f * s),
        size = Size((x1 - x0 + 2f) * s, 2.8f * s)
    )

    fun arm(l: FloatArray, far: Boolean) {
        val sk = if (far) pal.skinFar else pal.skin
        val sleeve = if (far) pal.shirtFar else pal.shirt
        val e = pt(l[0], l[1])
        val w = pt(l[2], l[3])
        capsule(e, w, 2.5f * s, 1.9f * s, sk)                 // antebraço
        capsule(shO, e, 3.1f * s, 2.5f * s, sk)               // braço
        capsule(shO, lerpOff(shO, e, 0.5f), 3.3f * s, 2.9f * s, sleeve) // manga
        drawCircle(sk, 2.2f * s, w)                           // mão
    }

    fun leg(l: FloatArray, footAngle: Float, far: Boolean) {
        val sk = if (far) pal.skinFar else pal.skin
        val sh = if (far) pal.shortsFar else pal.shorts
        val shoe = if (far) pal.shoeFar else pal.shoe
        val k = pt(l[0], l[1])
        val a = pt(l[2], l[3])
        capsule(k, a, 3.0f * s, 2.1f * s, sk)                 // canela
        capsule(hipO, k, 4.7f * s, 3.2f * s, sk)              // coxa
        capsule(hipO, lerpOff(hipO, k, 0.52f), 4.9f * s, 4.1f * s, sh)  // bermuda
        val toe = pt(l[2] + 6.5f * cos(rad(footAngle)), l[3] + 6.5f * sin(rad(footAngle)))
        capsule(a, toe, 2.5f * s, 1.8f * s, shoe)             // tênis
    }

    // lado de trás
    arm(armF, true)
    leg(legF, p[17], true)

    // tronco: camiseta e, na base, a bermuda
    torso(hipO, shO, s, 0f, 1f, pal.shirt)
    torso(hipO, shO, s, 0f, 0.26f, pal.shorts)
    // volume do tronco
    drawLine(
        Color.White.copy(alpha = 0.14f),
        lerpOff(hipO, shO, 0.3f).let { Offset(it.x + LIGHT.x * 2.2f * s, it.y + LIGHT.y * 2.2f * s) },
        lerpOff(hipO, shO, 0.95f).let { Offset(it.x + LIGHT.x * 2.2f * s, it.y + LIGHT.y * 2.2f * s) },
        strokeWidth = 2.6f * s, cap = StrokeCap.Round
    )

    // pescoço e cabeça
    val neckO = pt(shX + NECK * cos(rad(hAng)), shY + NECK * sin(rad(hAng)))
    capsule(shO, neckO, 2.0f * s, 1.9f * s, pal.skin, volume = false)
    val headO = pt(headX, headY)
    drawCircle(pal.skin, HEAD_R * s, headO)
    // cabelo: cobre o topo e a nuca. A frente do rosto fica 90° à direita da direção do pescoço
    // (em pé olha para a direita; deitado de costas olha para cima; de bruços olha para baixo).
    drawArc(
        pal.hair, startAngle = -(hAng + 50f) - 100f, sweepAngle = 200f, useCenter = true,
        topLeft = Offset(headO.x - HEAD_R * s, headO.y - HEAD_R * s),
        size = Size(HEAD_R * 2f * s, HEAD_R * 2f * s)
    )
    val faceAng = hAng - 90f
    fun onHead(front: Float, up: Float, r: Float): Offset {
        val dx = cos(rad(faceAng)) * front + cos(rad(hAng)) * up
        val dy = sin(rad(faceAng)) * front + sin(rad(hAng)) * up
        return Offset(headO.x + dx * r * s, headO.y - dy * r * s)
    }
    drawCircle(Color(0xFFD39A7C), 0.95f * s, onHead(1.0f, -0.05f, HEAD_R))           // nariz
    drawCircle(Color(0xFF2B2320), 0.8f * s, onHead(0.55f, 0.14f, HEAD_R))            // olho
    drawCircle(Color.White.copy(alpha = 0.16f), HEAD_R * 0.3f * s, Offset(headO.x + LIGHT.x * 2.8f * s, headO.y + LIGHT.y * 2.8f * s))

    // lado da frente
    leg(legN, p[16], false)
    arm(armN, false)
}


/**
 * Desenho de frente: dois ombros e dois quadris (deslocados na perpendicular do tronco),
 * rosto voltado para quem vê. "near" é o lado esquerdo da tela.
 */
private fun DrawScope.drawFrontBody(pt: (Float, Float) -> Offset, s: Float, groundY: Float, p: FloatArray, pal: Palette) {
    val th = rad(p[2])
    val ux = cos(th)
    val uy = sin(th)
    val nx = -uy
    val ny = ux
    val hx = p[0]
    val hy = p[1]
    val sx = hx + TORSO * ux
    val sy = hy + TORSO * uy

    val armN = ik(sx + nx * 6.4f, sy + ny * 6.4f, p[4], p[5], UA, FA, p[6])
    val armF = ik(sx - nx * 6.4f, sy - ny * 6.4f, p[7], p[8], UA, FA, p[9])
    val legN = ik(hx + nx * 3.7f, hy + ny * 3.7f, p[10], p[11], TH, SH, p[12])
    val legF = ik(hx - nx * 3.7f, hy - ny * 3.7f, p[13], p[14], TH, SH, p[15])

    // sombra no chão
    val xs = listOf(hx, sx, legN[2], legF[2], armN[2], armF[2])
    val x0 = xs.min()
    val x1 = xs.max()
    drawOval(
        Color.Black.copy(alpha = 0.13f),
        topLeft = Offset((x0 - 1f) * s, groundY - 1.2f * s),
        size = Size((x1 - x0 + 2f) * s, 2.8f * s)
    )

    fun leg(rx: Float, ry: Float, l: FloatArray, outward: Float) {
        val root = pt(rx, ry)
        val k = pt(l[0], l[1])
        val a = pt(l[2], l[3])
        capsule(k, a, 2.9f * s, 2.1f * s, pal.skin)
        capsule(root, k, 4.3f * s, 3.0f * s, pal.skin)
        capsule(root, lerpOff(root, k, 0.55f), 4.5f * s, 3.8f * s, pal.shorts)
        drawOval(pal.shoe, topLeft = Offset(a.x + outward * 2.2f * s - 3f * s, a.y - 1f * s), size = Size(6f * s, 3f * s))
    }

    fun arm(rx: Float, ry: Float, l: FloatArray) {
        val root = pt(rx, ry)
        val e = pt(l[0], l[1])
        val w = pt(l[2], l[3])
        capsule(e, w, 2.4f * s, 1.9f * s, pal.skin)
        capsule(root, e, 3.0f * s, 2.4f * s, pal.skin)
        capsule(root, lerpOff(root, e, 0.5f), 3.2f * s, 2.8f * s, pal.shirt)
        drawCircle(pal.skin, 2.1f * s, w)
    }

    // o lado "near" é o de maior projeção na perpendicular; as pontas dos pés apontam para fora
    val nearOut = if (nx >= 0f) 1f else -1f
    leg(hx - nx * 3.7f, hy - ny * 3.7f, legF, -nearOut)
    leg(hx + nx * 3.7f, hy + ny * 3.7f, legN, nearOut)
    arm(sx - nx * 6.4f, sy - ny * 6.4f, armF)
    arm(sx + nx * 6.4f, sy + ny * 6.4f, armN)

    val hipO = pt(hx, hy)
    val shO = pt(sx, sy)
    torso(hipO, shO, s, 0f, 1f, pal.shirt, FRONT_PROFILE)
    torso(hipO, shO, s, 0f, 0.28f, pal.shorts, FRONT_PROFILE)

    // pescoço, cabeça e rosto
    val hAng = p[2] + p[3]
    val hux = cos(rad(hAng))
    val huy = sin(rad(hAng))
    val neckO = pt(sx + hux * NECK, sy + huy * NECK)
    capsule(shO, neckO, 2.2f * s, 2.2f * s, pal.skin, volume = false)
    val cx = sx + hux * (NECK + HEAD_R)
    val cy = sy + huy * (NECK + HEAD_R)
    val headO = pt(cx, cy)
    drawCircle(pal.skin, HEAD_R * s, headO)
    drawArc(
        pal.hair, startAngle = -hAng - 85f, sweepAngle = 170f, useCenter = true,
        topLeft = Offset(headO.x - HEAD_R * s, headO.y - HEAD_R * s),
        size = Size(HEAD_R * 2f * s, HEAD_R * 2f * s)
    )
    for (sg in listOf(-1f, 1f)) {
        val ex = cx + nx * 2.0f * sg - hux * 0.6f
        val ey = cy + ny * 2.0f * sg - huy * 0.6f
        drawCircle(Color(0xFF2B2320), 0.7f * s, pt(ex, ey))
    }
    val m = pt(cx - hux * 2.6f, cy - huy * 2.6f)
    drawArc(
        Color(0xFF8A4B3A), startAngle = 20f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(m.x - 1.6f * s, m.y - 1.2f * s), size = Size(3.2f * s, 2.2f * s),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5f * s)
    )
}
