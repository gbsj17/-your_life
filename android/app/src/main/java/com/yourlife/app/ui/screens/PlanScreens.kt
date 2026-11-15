@file:OptIn(ExperimentalMaterial3Api::class)

package com.yourlife.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yourlife.app.data.Swaps
import com.yourlife.app.ui.components.SecondaryButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourlife.app.AppViewModel
import com.yourlife.app.data.Catalog
import com.yourlife.app.data.Exercise
import com.yourlife.app.data.Level
import com.yourlife.app.data.forLevel
import com.yourlife.app.ui.components.LevelChips
import com.yourlife.app.data.exercise
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.BackButton
import com.yourlife.app.ui.components.ExerciseFigure
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.PrimaryButton
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.icons.AppIcons

@Composable
fun PlanDetailScreen(vm: AppViewModel, planId: String, level: Level, deload: Boolean) {
    val plan = Catalog.plan(planId).forLevel(level, deload)
    // o mesmo treino sem as trocas: dá o exercício original de cada posição
    val raw = Catalog.plan(planId).forLevel(level, deload, swaps = false)
    var swapIdx by remember { mutableStateOf<Int?>(null) }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackButton { vm.route = com.yourlife.app.Route.Home }
            Text(plan.name, style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        plan.tagline,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LevelChips(level, { vm.route = com.yourlife.app.Route.PlanDetail(planId, it, deload) })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip("~${plan.estMin} min", icon = AppIcons.Timer, highlight = true)
                        InfoChip("${plan.items.size} exercícios", icon = AppIcons.Dumbbell)
                        if (deload) InfoChip("Semana leve")
                    }
                    Text(
                        "Foco: " + plan.focus.joinToString(", ") { it.label },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SectionTitle("Exercícios")
                    Text(
                        "Toque em um exercício para ver a animação e como fazer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            itemsIndexed(plan.items) { idx, item ->
                val ex = item.exercise()
                AppCard(onClick = { vm.sheetExercise = ex }) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.width(92.dp)) {
                            ExerciseFigure(ex, animate = false, showPhase = false)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${idx + 1}. ${ex.name}", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${item.sets} × ${item.target} · descanso ${item.rest}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                ex.muscles.take(3).joinToString(", ") { it.label },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val swapped = raw.items[idx].exerciseId != item.exerciseId
                            Text(
                                if (swapped) "Trocado · mudar" else "Trocar por outro",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { swapIdx = idx }.padding(vertical = 4.dp)
                            )
                        }
                        AppIcon(AppIcons.Info, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                    }
                }
            }
        }
        swapIdx?.let { i ->
            SwapSheet(
                original = Catalog.exercise(raw.items[i].exerciseId),
                currentId = plan.items[i].exerciseId,
                onPick = { vm.setSwap(raw.items[i].exerciseId, it); swapIdx = null },
                onDismiss = { swapIdx = null }
            )
        }
        Box(Modifier.padding(16.dp).navigationBarsPadding()) {
            PrimaryButton("Iniciar treino", onClick = { vm.startWorkout(plan) }, icon = AppIcons.Play)
        }
    }
}

/** Folha com a animação do exercício, músculos, passo a passo e erros comuns. */
@Composable
fun ExerciseSheet(exercise: Exercise, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ExerciseFigure(exercise)
            Text(exercise.name, style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(exercise.level.label, highlight = exercise.level.ordinal == 0)
                exercise.equipment?.let { InfoChip(it, icon = AppIcons.Info) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                exercise.muscles.take(3).forEach { InfoChip(it.label, icon = AppIcons.Target) }
            }

            SectionTitle("Como fazer")
            exercise.steps.forEachIndexed { i, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${i + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }

            SectionTitle("Erros comuns")
            AppCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    exercise.mistakes.forEach {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppIcon(AppIcons.Close, tint = MaterialTheme.colorScheme.error, size = 18.dp)
                            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Folha com substitutos do exercício: mesmo músculo, mesma medida (reps ou tempo), nível igual ou mais fácil. */
@Composable
fun SwapSheet(original: Exercise, currentId: String, onPick: (String?) -> Unit, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val options = Swaps.alternatives(original.id)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Trocar ${original.name}", style = MaterialTheme.typography.titleLarge)
            Text(
                "Opções que trabalham o mesmo músculo (${original.muscles.first().label}), do mesmo nível ou mais fáceis. A troca vale para todos os treinos com este exercício.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (currentId != original.id) {
                SecondaryButton("Voltar ao original (${original.name})", { onPick(null) }, Modifier.fillMaxWidth())
            }
            if (options.isEmpty()) {
                Text("Não há alternativas mais fáceis para este exercício.", style = MaterialTheme.typography.bodyMedium)
            }
            options.forEach { alt ->
                AppCard(onClick = { onPick(alt.id) }) {
                    Row(
                        Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.width(92.dp)) { ExerciseFigure(alt, animate = false, showPhase = false) }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(alt.name, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                InfoChip(
                                    if (alt.level.ordinal < original.level.ordinal) "Mais fácil" else "Mesmo nível",
                                    highlight = alt.level.ordinal < original.level.ordinal
                                )
                                if (alt.id == currentId) InfoChip("Em uso", icon = AppIcons.Check)
                            }
                            alt.equipment?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
