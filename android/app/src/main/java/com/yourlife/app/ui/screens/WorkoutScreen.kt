package com.yourlife.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourlife.app.AppViewModel
import com.yourlife.app.Phase
import com.yourlife.app.WorkoutState
import com.yourlife.app.data.exercise
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.ExerciseFigure
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.PrimaryButton
import com.yourlife.app.ui.components.ProgressRing
import com.yourlife.app.ui.components.RoundIconButton
import com.yourlife.app.ui.components.SecondaryButton
import com.yourlife.app.ui.components.fmtClock
import com.yourlife.app.ui.icons.AppIcons
import com.yourlife.app.ui.theme.Yl

@Composable
fun WorkoutScreen(vm: AppViewModel) {
    val w = vm.workout ?: return
    var confirmQuit by remember { mutableStateOf(false) }

    // mantém a tela ligada durante o treino
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // vibra quando um descanso ou série por tempo termina
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(vm.cue) {
        if (vm.cue > 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    BackHandler {
        if (w.phase == Phase.DONE) vm.closeFinishedWorkout() else confirmQuit = true
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text("Sair do treino?") },
            text = { Text("O progresso desta sessão será perdido.") },
            confirmButton = {
                TextButton(onClick = { confirmQuit = false; vm.quitWorkout() }) { Text("Sair") }
            },
            dismissButton = { TextButton(onClick = { confirmQuit = false }) { Text("Continuar") } }
        )
    }

    Column(Modifier.fillMaxSize()) {
        if (w.phase == Phase.DONE) {
            DoneContent(vm, w)
        } else {
            WorkoutHeader(vm, w, onClose = { confirmQuit = true })
            AnimatedContent(
                targetState = (w.phase == Phase.REST) to w.index,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    (slideInVertically(tween(320)) { it / 12 } + fadeIn(tween(280))) togetherWith fadeOut(tween(140))
                },
                label = "workout-step"
            ) { (resting, _) ->
                if (resting) RestContent(vm, w) else WorkContent(vm, w)
            }
        }
    }
}

@Composable
private fun WorkoutHeader(vm: AppViewModel, w: WorkoutState, onClose: () -> Unit) {
    val timing = w.phase == Phase.REST || w.phase == Phase.RUN
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(AppIcons.Close, "Sair do treino", onClose)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w.plan.name, style = MaterialTheme.typography.labelLarge)
                Text(
                    "Exercício ${w.index + 1} de ${w.plan.items.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (timing) {
                RoundIconButton(
                    if (w.paused) AppIcons.Play else AppIcons.Pause,
                    if (w.paused) "Retomar" else "Pausar",
                    { vm.togglePause() }
                )
            } else {
                Spacer(Modifier.size(44.dp))
            }
        }
        // progresso por exercício
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            w.plan.items.forEachIndexed { i, _ ->
                val color = when {
                    i < w.index -> MaterialTheme.colorScheme.primary
                    i == w.index -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .background(color, RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
private fun WorkContent(vm: AppViewModel, w: WorkoutState) {
    val item = w.item
    val ex = item.exercise()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExerciseFigure(ex, animate = !w.paused)

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(ex.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip("Série ${w.setNo} de ${item.sets}", highlight = true)
                InfoChip(ex.muscles.first().label)
            }
        }

        if (item.seconds == null) {
            Text(
                "${item.reps} repetições",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else if (w.phase == Phase.RUN) {
            ProgressRing(progress = w.leftMs.toFloat() / w.totalMs.toFloat(), modifier = Modifier.size(170.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        fmtClock(((w.leftMs + 999) / 1000).toInt()),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (w.paused) {
                        Text("Pausado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            Text(
                "${item.seconds} segundos",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        when {
            item.seconds == null -> PrimaryButton("Concluir série", { vm.completeSet() }, icon = AppIcons.Check)
            w.phase == Phase.WORK -> PrimaryButton("Iniciar", { vm.startTimed() }, icon = AppIcons.Play)
            else -> SecondaryButton("Concluir agora", { vm.completeSet() }, Modifier.fillMaxWidth(), icon = AppIcons.Check)
        }

        Text(
            ex.steps.first(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = { vm.sheetExercise = ex }) {
            AppIcon(AppIcons.Info, size = 18.dp)
            Spacer(Modifier.width(6.dp))
            Text("Como fazer, passo a passo")
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RestContent(vm: AppViewModel, w: WorkoutState) {
    // o próximo exercício: a mesma série do item atual (já avançada pelo estado)
    val next = w.item.exercise()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Descanso", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ProgressRing(progress = w.leftMs.toFloat() / w.totalMs.toFloat(), modifier = Modifier.size(210.dp), strokeWidth = 14.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(fmtClock(((w.leftMs + 999) / 1000).toInt()), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold)
                if (w.paused) {
                    Text("Pausado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("+15s", { vm.addRest(15) }, Modifier.weight(1f), icon = AppIcons.Plus)
            SecondaryButton("Pular", { vm.skipRest() }, Modifier.weight(1f), icon = AppIcons.Skip)
        }
        AppCard(onClick = { vm.sheetExercise = next }) {
            Row(
                Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.width(110.dp)) { ExerciseFigure(next, showPhase = false) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("A SEGUIR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(next.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Série ${w.setNo} de ${w.item.sets} · ${w.item.target}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DoneContent(vm: AppViewModel, w: WorkoutState) {
    var popped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { popped = true }
    val pop by animateFloatAsState(
        if (popped) 1f else 0.2f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "trophy"
    )
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(96.dp)
                .graphicsLayer { scaleX = pop; scaleY = pop; alpha = pop.coerceIn(0f, 1f) }
                .background(Yl.extra.success, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(AppIcons.Trophy, tint = Yl.extra.onSuccess, size = 52.dp)
        }
        Text("Treino concluído!", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Text(
            "${w.plan.name} · ${fmtClock(w.totalSeconds)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val hcText = when {
            !vm.hcGranted -> null
            vm.syncing -> "Enviando ao Health Connect..."
            vm.history.lastOrNull()?.synced == true -> "Enviado ao Health Connect."
            else -> null
        }
        if (hcText != null) {
            InfoChip(hcText, icon = AppIcons.Heart, highlight = true)
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Ver progresso", { vm.closeFinishedWorkout() })
    }
}
