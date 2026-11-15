package com.yourlife.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yourlife.app.data.Enrollment
import com.yourlife.app.data.ProgramStatus
import com.yourlife.app.data.Programs
import com.yourlife.app.ui.components.BarProgress
import com.yourlife.app.ui.components.SecondaryButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourlife.app.AppViewModel
import com.yourlife.app.Route
import com.yourlife.app.data.Catalog
import com.yourlife.app.data.Plan
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.IconBubble
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.icons.AppIcons
import com.yourlife.app.ui.theme.Yl
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle as DayStyle
import java.util.Locale

@Composable
fun HomeScreen(vm: AppViewModel, bottomPadding: PaddingValues) {
    val today = LocalDate.now()
    val status = vm.programStatus
    var followPlan by remember { mutableStateOf(true) }
    val planMode = status != null && !status.finished && followPlan
    val programPlan = if (planMode) status?.todayPlan else null
    val suggested = programPlan ?: Catalog.suggestionFor(today.dayOfWeek)
    val heroLabel = when {
        programPlan != null -> "TREINO DE HOJE NO PLANO"
        planMode -> "DIA DE DESCANSO NO PLANO · SUGESTÃO LIVRE"
        else -> "TREINO ALEATÓRIO DO DIA"
    }
    val trainedToday = vm.history.any { it.date == today.toString() }
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp,
            bottom = bottomPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(Modifier.padding(top = 8.dp)) {
                Text(greeting, style = MaterialTheme.typography.headlineLarge)
                Text(
                    if (trainedToday) "Treino de hoje feito. Bom trabalho!" else "Pronto para treinar hoje?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            val e = vm.enrollment
            if (status != null && e != null && !status.finished) {
                PlanProgressCard(vm, status, e, followPlan) { followPlan = it }
            } else {
                NoPlanCard { vm.route = Route.Plans }
            }
        }
        item { HeroCard(suggested, heroLabel, trainedToday) { vm.route = Route.PlanDetail(suggested.id, suggested.level, suggested.deload) } }
        item { WeekStrip(vm, today) }
        item { SectionTitle("Todos os treinos", Modifier.padding(top = 6.dp)) }
        items(Catalog.plans, key = { it.id }) { plan ->
            PlanCard(plan) { vm.route = Route.PlanDetail(plan.id) }
        }
    }
}

@Composable
private fun HeroCard(plan: Plan, label: String, trainedToday: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Surface(onClick = onClick, shape = shape, color = Color.Transparent) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Yl.extra.heroBrush)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (trainedToday) "$label · FEITO" else label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(plan.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(plan.tagline, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = Color.White) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppIcon(AppIcons.Play, tint = Color(0xFFE5392B), size = 18.dp)
                            Text("Ver treino", style = MaterialTheme.typography.labelLarge, color = Color(0xFFB92418))
                        }
                    }
                    Text(
                        "${plan.items.size} exercícios · ~${plan.estMin} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(vm: AppViewModel, today: LocalDate) {
    val scheme = MaterialTheme.colorScheme
    AppCard {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 6 downTo 0) {
                val day = today.minusDays(i.toLong())
                val done = vm.history.any { it.date == day.toString() }
                val isToday = i == 0
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        day.dayOfWeek.getDisplayName(DayStyle.NARROW, Locale("pt", "BR")).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isToday) scheme.primary else scheme.onSurfaceVariant
                    )
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (done) Yl.extra.success else scheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            AppIcon(AppIcons.Check, tint = Yl.extra.onSuccess, size = 18.dp)
                        } else {
                            Text(
                                day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, onClick: () -> Unit) {
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
                InfoChip(plan.focus.take(2).joinToString(" · ") { it.label })
            }
            AppIcon(AppIcons.Chevron, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
    }
}

@Composable
private fun PlanProgressCard(
    vm: AppViewModel,
    st: ProgramStatus,
    e: Enrollment,
    followPlan: Boolean,
    onMode: (Boolean) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val planned = Programs.plannedTotal(e)
    val done = vm.history.count { java.time.LocalDate.parse(it.date).toEpochDay() >= e.startDay }
    val remaining = (planned - done).coerceAtLeast(0)
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MEU PLANO", style = MaterialTheme.typography.labelMedium, color = scheme.primary)
                    Text(st.program.name, style = MaterialTheme.typography.titleLarge)
                }
                InfoChip(st.level.label, highlight = true)
            }
            BarProgress(if (planned > 0) done / planned.toFloat() else 0f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$done feitos", style = MaterialTheme.typography.titleMedium, color = scheme.primary)
                Text("faltam $remaining de $planned", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
            Text(
                "Dia ${st.dayNumber} de ${st.totalDays}" + if (st.deload) " · semana leve" else "",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeChip("Seguir plano", followPlan, Modifier.weight(1f)) { onMode(true) }
                ModeChip("Treino do dia", !followPlan, Modifier.weight(1f)) { onMode(false) }
            }
        }
    }
}

@Composable
private fun ModeChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) scheme.primary else scheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) scheme.primary else scheme.outlineVariant)
    ) {
        Box(Modifier.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) scheme.onPrimary else scheme.onSurface
            )
        }
    }
}

@Composable
private fun NoPlanCard(onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBubble(AppIcons.Calendar, size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Siga um plano", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Escolha um objetivo e acompanhe quantos treinos já fez e quantos faltam.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AppIcon(AppIcons.Chevron, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
    }
}
