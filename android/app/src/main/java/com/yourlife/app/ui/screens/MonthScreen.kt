package com.yourlife.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourlife.app.AppViewModel
import com.yourlife.app.Route
import com.yourlife.app.data.Level
import com.yourlife.app.data.Programs
import com.yourlife.app.data.exercise
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.BackButton
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.PrimaryButton
import com.yourlife.app.ui.components.RoundIconButton
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.icons.AppIcons
import com.yourlife.app.ui.theme.Yl
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val WEEK = listOf("S", "T", "Q", "Q", "S", "S", "D")
private val PT = Locale("pt", "BR")

@Composable
private fun levelColor(l: Level): Color = when (l) {
    Level.INICIANTE -> Yl.extra.success
    Level.MEDIO -> MaterialTheme.colorScheme.primary
    Level.AVANCADO -> MaterialTheme.colorScheme.tertiary
}

private fun fmt(d: LocalDate) = "%02d/%02d".format(d.dayOfMonth, d.monthValue)

/** Calendário do mês com o plano de cada dia, mudanças de nível, semanas leves e o detalhe do dia escolhido. */
@Composable
fun MonthScreen(vm: AppViewModel) {
    val e = vm.enrollment
    val scheme = MaterialTheme.colorScheme
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(LocalDate.now()) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackButton { vm.route = Route.Plans }
            Text("Plano do mês", style = MaterialTheme.typography.titleLarge)
        }
        if (e == null) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Você ainda não começou um plano.", style = MaterialTheme.typography.bodyLarge)
                PrimaryButton("Escolher um plano", { vm.route = Route.Plans })
            }
            return@Column
        }
        val program = Programs.get(e.programId)!!
        val done = vm.history.map { it.date }.toSet()

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(AppIcons.Back, "Mês anterior", { month = month.minusMonths(1) })
                    Text(
                        month.month.getDisplayName(TextStyle.FULL, PT).replaceFirstChar { it.uppercase() } + " " + month.year,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    RoundIconButton(AppIcons.Chevron, "Próximo mês", { month = month.plusMonths(1) })
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        WEEK.forEach {
                            Text(
                                it, Modifier.weight(1f), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    val offset = month.atDay(1).dayOfWeek.value - 1
                    val total = month.lengthOfMonth()
                    repeat((offset + total + 6) / 7) { r ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(7) { c ->
                                val n = r * 7 + c - offset + 1
                                if (n in 1..total) {
                                    val date = month.atDay(n)
                                    DayCell(
                                        date, Programs.dayInfo(e, date), date.toString() in done,
                                        date == selected, date == LocalDate.now(), Modifier.weight(1f)
                                    ) { selected = date }
                                } else Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Level.entries.forEach { l ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(Modifier.size(9.dp).background(levelColor(l), CircleShape))
                            Text(l.label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        AppIcon(AppIcons.Check, tint = scheme.onSurfaceVariant, size = 11.dp)
                        Text("Feito", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                    }
                }
            }

            // ---- mudanças dentro do mês
            item {
                val first = month.atDay(1)
                val last = month.atEndOfMonth()
                val changes = Programs.phaseStarts(e).drop(1).filter { it.first in first..last }
                val phases = Programs.phaseStarts(e)
                // semanas leves: faixas de dias seguidos com deload
                val deloads = ArrayList<Pair<LocalDate, LocalDate>>()
                var runStart: LocalDate? = null
                var d = first
                while (!d.isAfter(last.plusDays(1))) {
                    val on = d <= last && Programs.dayInfo(e, d)?.deload == true
                    if (on && runStart == null) runStart = d
                    if (!on && runStart != null) { deloads.add(runStart to d.minusDays(1)); runStart = null }
                    d = d.plusDays(1)
                }
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("Mudanças neste mês")
                        if (changes.isEmpty() && deloads.isEmpty()) {
                            Text(
                                "Sem mudança de nível neste mês. Os exercícios continuam os mesmos.",
                                style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant
                            )
                        }
                        changes.forEach { (date, lv) ->
                            val from = phases[phases.indexOfFirst { it.first == date } - 1].second
                            Text("${fmt(date)}: sobe para ${lv.label}", style = MaterialTheme.typography.titleSmall)
                            val ex = Programs.exerciseChanges(program, from, lv)
                            ex.forEach { (a, b) ->
                                Text(
                                    "• $a → $b",
                                    style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "Além disso, mudam séries, repetições e descanso.",
                                style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant
                            )
                        }
                        deloads.forEach { (a, b) ->
                            Text(
                                "Semana leve: ${fmt(a)} a ${fmt(b)} (uma série a menos por exercício)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // ---- detalhe do dia escolhido
            item {
                val info = Programs.dayInfo(e, selected)
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            selected.dayOfWeek.getDisplayName(TextStyle.FULL, PT).replaceFirstChar { it.uppercase() } +
                                ", ${fmt(selected)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (info == null) {
                            Text(
                                "Fora do período do plano.",
                                style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoChip(info.level.label, highlight = true)
                                InfoChip("Dia ${info.dayNumber} de ${info.totalDays}")
                                if (info.deload) InfoChip("Semana leve")
                            }
                            val plan = info.todayPlan
                            if (plan == null) {
                                Text("Descanso.", style = MaterialTheme.typography.bodyLarge)
                            } else {
                                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                                plan.items.forEachIndexed { i, it ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${i + 1}. ${it.exercise().name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Text(
                                            "${it.sets} × ${it.target}",
                                            style = MaterialTheme.typography.bodyMedium, color = scheme.primary
                                        )
                                    }
                                }
                                PrimaryButton(
                                    "Ver treino",
                                    { vm.route = Route.PlanDetail(plan.id, plan.level, plan.deload) },
                                    icon = AppIcons.Play
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    info: com.yourlife.app.data.ProgramStatus?,
    done: Boolean,
    selected: Boolean,
    today: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.82f),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) scheme.primary.copy(alpha = 0.16f) else Color.Transparent,
        border = if (today) BorderStroke(1.5.dp, scheme.primary) else null
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                "${date.dayOfMonth}",
                fontSize = 13.sp,
                fontWeight = if (today || selected) FontWeight.Bold else FontWeight.Normal,
                color = if (info == null) scheme.onSurfaceVariant.copy(alpha = 0.5f) else scheme.onSurface
            )
            Spacer(Modifier.size(3.dp))
            when {
                info == null -> Spacer(Modifier.size(10.dp))
                info.todayPlan == null -> Text("–", fontSize = 11.sp, color = scheme.onSurfaceVariant)
                done -> AppIcon(AppIcons.Check, tint = levelColor(info.level), size = 12.dp)
                else -> Box(
                    Modifier.size(9.dp).let {
                        if (info.deload) it.background(Color.Transparent, CircleShape)
                            .then(Modifier.padding(0.dp))
                        else it.background(levelColor(info.level), CircleShape)
                    }
                ) {
                    if (info.deload) Box(Modifier.size(9.dp).background(levelColor(info.level).copy(alpha = 0.35f), CircleShape))
                }
            }
        }
    }
}
