package com.yourlife.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourlife.app.AppViewModel
import com.yourlife.app.Route
import com.yourlife.app.data.Catalog
import com.yourlife.app.data.Level
import com.yourlife.app.data.Programs
import com.yourlife.app.data.forLevel
import com.yourlife.app.data.weeklySets
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.BackButton
import com.yourlife.app.ui.components.InfoChip
import com.yourlife.app.ui.components.LevelChips
import com.yourlife.app.ui.components.PrimaryButton
import com.yourlife.app.ui.components.RoundIconButton
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.icons.AppIcons

private val DAY_NAMES = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

@Composable
fun ProgramDetailScreen(vm: AppViewModel, programId: String) {
    val program = Programs.get(programId) ?: run { vm.route = Route.Plans; return }
    val enrolled = vm.enrollment?.takeIf { it.programId == programId }
    // se o programa já está ativo, os dias valem direto para ele; senão ficam só nesta tela até começar
    val local = remember { Programs.defaultDays(program).toMutableStateList() }
    val days = enrolled?.phaseDays ?: local
    var preview by remember { mutableStateOf(Level.MEDIO) }
    val scheme = MaterialTheme.colorScheme

    fun setDays(i: Int, v: Int) {
        val nv = v.coerceIn(0, 84)
        if (enrolled != null) vm.setPhaseDays(i, nv)
        else if (days.sum() - days[i] + nv >= 7) local[i] = nv
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BackButton { vm.route = Route.Plans }
            Text(program.name, style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(program.description, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            }
            program.note?.let { n ->
                item {
                    AppCard {
                        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            com.yourlife.app.ui.components.AppIcon(AppIcons.Info, tint = scheme.primary, size = 20.dp)
                            Text(n, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionTitle("Dias em cada nível")
                    Text(
                        "Defina quanto tempo fica em cada fase, de 7 em 7 dias. Zero pula o nível. " +
                            "A cada 4 semanas na mesma fase há uma semana leve de recuperação.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
            item {
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Level.entries.forEachIndexed { i, lv ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(lv.label, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${days[i]} dias · ${days[i] / 7} semanas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onSurfaceVariant
                                    )
                                }
                                RoundIconButton(AppIcons.Minus, "Menos 7 dias", { setDays(i, days[i] - 7) })
                                Text(
                                    "${days[i]}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                                RoundIconButton(AppIcons.Plus, "Mais 7 dias", { setDays(i, days[i] + 7) })
                            }
                        }
                        Text(
                            "Total: ${days.sum()} dias (${days.sum() / 7} semanas)",
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.primary
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Semana de treinos")
                    LevelChips(preview, { preview = it })
                    val phase = program.phases[preview.ordinal]
                    val trainDays = phase.week.count { it != null }
                    InfoChip("$trainDays treinos por semana", highlight = true)
                }
            }
            item {
                val phase = program.phases[preview.ordinal]
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        phase.week.forEachIndexed { i, id ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(DAY_NAMES[i], style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                                Text(
                                    id?.let { Catalog.plan(it).forLevel(preview).let { p -> "${p.name} · ~${p.estMin} min" } } ?: "Descanso",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            item {
                val phase = program.phases[preview.ordinal]
                val sets = weeklySets(phase.week.filterNotNull().map { Catalog.plan(it).forLevel(preview) })
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Séries por músculo na semana")
                    Text(
                        "Para ganhar massa, a referência é de pelo menos 10 séries por semana em cada grupo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    AppCard {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            sets.forEach { (m, n) ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(m.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "$n séries",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (n >= 10) scheme.primary else scheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Column(Modifier.padding(16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (enrolled != null) {
                PrimaryButton("Encerrar este programa", { vm.endProgram(); vm.route = Route.Plans })
            } else {
                PrimaryButton("Começar hoje", { vm.enroll(programId, days.toList()); vm.route = Route.Plans }, icon = AppIcons.Play)
            }
        }
    }
}
