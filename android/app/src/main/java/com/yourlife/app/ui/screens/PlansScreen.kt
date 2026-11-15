package com.yourlife.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourlife.app.AppViewModel
import com.yourlife.app.Route
import com.yourlife.app.data.Catalog
import com.yourlife.app.data.Level
import com.yourlife.app.data.Plan
import com.yourlife.app.data.Program
import com.yourlife.app.data.ProgramStatus
import com.yourlife.app.data.Programs
import com.yourlife.app.data.forLevel
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.BarProgress
import com.yourlife.app.ui.components.IconBubble
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.PrimaryButton
import com.yourlife.app.ui.components.SecondaryButton
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.components.staggerIn
import com.yourlife.app.ui.icons.AppIcons

private val DAYS = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")

@Composable
fun PlansScreen(vm: AppViewModel, bottomPadding: PaddingValues) {
    val status = vm.programStatus
    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp,
            bottom = bottomPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.padding(top = 8.dp).staggerIn(0)) {
                Text("Planos", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "Programas por objetivo, com níveis e dias definidos por você.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (status != null) item { MyPlanCard(vm, status) }
        item { SectionTitle("Programas por objetivo", Modifier.padding(top = 6.dp)) }
        items(Programs.all, key = { it.id }) { p ->
            ProgramCard(p, active = vm.enrollment?.programId == p.id) { vm.route = Route.ProgramDetail(p.id) }
        }
        item {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("Combos de treino")
                Text(
                    "Treinos que juntam grupos musculares em uma sessão só.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(Catalog.plans.filter { it.combo }, key = { it.id }) { plan ->
            ComboCard(plan) { vm.route = Route.PlanDetail(plan.id) }
        }
    }
}

@Composable
private fun MyPlanCard(vm: AppViewModel, st: ProgramStatus) {
    val scheme = MaterialTheme.colorScheme
    AppCard(Modifier.staggerIn(1)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("MEU PLANO", style = MaterialTheme.typography.labelMedium, color = scheme.primary)
            Text(st.program.name, style = MaterialTheme.typography.titleLarge)
            if (st.finished) {
                Text(
                    "Programa concluído. Escolha outro objetivo ou recomece.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Dia ${st.dayNumber} de ${st.totalDays}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                BarProgress(st.dayNumber / st.totalDays.toFloat())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip("Nível: ${st.level.label}", highlight = true)
                    InfoChip("Semana ${st.weekInPhase} da fase")
                    if (st.deload) InfoChip("Semana leve")
                }
                if (st.deload) {
                    Text(
                        "Esta semana tem uma série a menos em cada exercício, para o corpo recuperar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton("Mais fácil", { vm.shiftLevel(-1) }, Modifier.weight(1f), AppIcons.Minus)
                    SecondaryButton("Mais difícil", { vm.shiftLevel(1) }, Modifier.weight(1f), AppIcons.Plus)
                }
                val off = vm.enrollment?.levelOffset ?: 0
                if (off != 0) {
                    Text(
                        "Ajuste manual: ${if (off > 0) "+$off" else "$off"} nível(is) em relação ao previsto na fase.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                val plan = st.todayPlan
                if (plan != null) {
                    Text("Hoje: ${plan.name}", style = MaterialTheme.typography.titleMedium)
                    PrimaryButton("Ver treino de hoje", { vm.route = Route.PlanDetail(plan.id, plan.level, plan.deload) }, icon = AppIcons.Play)
                } else {
                    Text("Hoje é dia de descanso.", style = MaterialTheme.typography.titleMedium)
                }
                Text("Semana", style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                st.phase.week.forEachIndexed { i, id ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val isToday = java.time.LocalDate.now().dayOfWeek.value - 1 == i
                        Text(
                            DAYS[i], style = MaterialTheme.typography.bodyMedium,
                            color = if (isToday) scheme.primary else scheme.onSurfaceVariant
                        )
                        Text(
                            id?.let { Catalog.plan(it).name } ?: "Descanso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isToday) scheme.primary else scheme.onSurface
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("Ver programa", { vm.route = Route.ProgramDetail(st.program.id) }, Modifier.weight(1f))
                SecondaryButton("Encerrar", { vm.endProgram() }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProgramCard(p: Program, active: Boolean, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBubble(AppIcons.forPlan(p.icon), size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(p.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    p.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoChip(p.goal, highlight = true)
                    if (active) InfoChip("Em andamento", icon = AppIcons.Check)
                }
            }
            AppIcon(AppIcons.Chevron, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
    }
}

@Composable
private fun ComboCard(plan: Plan, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBubble(AppIcons.forPlan(plan.icon), size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${plan.items.size} exercícios · ~${plan.estMin} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AppIcon(AppIcons.Chevron, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
    }
}
