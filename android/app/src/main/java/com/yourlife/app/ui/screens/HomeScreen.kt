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
    val programPlan = status?.todayPlan
    val suggested = programPlan ?: Catalog.suggestionFor(today.dayOfWeek)
    val heroLabel = when {
        programPlan != null -> "PLANO · DIA ${status?.dayNumber} DE ${status?.totalDays}"
        status != null && !status.finished -> "DIA DE DESCANSO NO PLANO · SUGESTÃO LIVRE"
        else -> "SUGESTÃO DE HOJE"
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
