package com.yourlife.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourlife.app.AppViewModel
import com.yourlife.app.data.Session
import com.yourlife.app.ui.components.AppCard
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.components.IconBubble
import com.yourlife.app.ui.components.SecondaryButton
import com.yourlife.app.ui.components.SectionTitle
import com.yourlife.app.ui.components.StatTile
import com.yourlife.app.ui.components.fmtClock
import com.yourlife.app.ui.icons.AppIcons
import com.yourlife.app.ui.theme.Yl
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DayStyle
import java.util.Locale

@Composable
fun ProgressScreen(vm: AppViewModel, bottomPadding: PaddingValues) {
    val history = vm.history
    val today = LocalDate.now()
    val days = history.map { it.date }.toSet()
    var confirmClear by remember { mutableStateOf(false) }

    // sequência de dias seguidos (hoje ainda pode estar em aberto)
    var streak = 0
    var d = if (today.toString() in days) today else today.minusDays(1)
    while (d.toString() in days) {
        streak++
        d = d.minusDays(1)
    }

    val week = (6 downTo 0).map { i ->
        val day = today.minusDays(i.toLong())
        day to history.count { it.date == day.toString() }
    }
    val weekTotal = week.sumOf { it.second }
    val minutes = Math.round(history.sumOf { it.seconds } / 60f)
    val fmtDate = DateTimeFormatter.ofPattern("dd/MM · HH:mm")

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp,
            bottom = bottomPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Progresso", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 8.dp)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("${history.size}", "treinos feitos", Modifier.weight(1f))
                    StatTile("$weekTotal", "nos últimos 7 dias", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("$streak", "dias seguidos", Modifier.weight(1f))
                    StatTile("$minutes", "minutos treinados", Modifier.weight(1f))
                }
            }
        }
        item {
            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Últimos 7 dias", style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier.fillMaxWidth().height(110.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        week.forEach { (day, n) ->
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val fraction = if (n > 0) minOf(1f, 0.3f + n * 0.35f) else 0.06f
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fraction)
                                            .background(
                                                if (n > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(10.dp)
                                            )
                                    )
                                }
                                Text(
                                    day.dayOfWeek.getDisplayName(DayStyle.NARROW, Locale("pt", "BR")).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item { SectionTitle("Histórico", Modifier.padding(top = 6.dp)) }
        if (history.isEmpty()) {
            item {
                Text(
                    "Nenhum treino ainda. Conclua o primeiro para ver seu progresso aqui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val recent: List<Session> = history.takeLast(30).reversed()
            recent.forEach { s ->
                item(key = s.id) {
                    AppCard {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconBubble(AppIcons.Check, size = 44.dp)
                            Column(Modifier.weight(1f)) {
                                Text(s.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    Instant.ofEpochMilli(s.ts).atZone(ZoneId.systemDefault()).format(fmtDate) +
                                        " · " + fmtClock(s.seconds),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (s.synced) {
                                AppIcon(AppIcons.Heart, tint = Yl.extra.success, size = 20.dp)
                            }
                        }
                    }
                }
            }
            item {
                SecondaryButton(
                    "Apagar histórico",
                    { confirmClear = true },
                    Modifier.fillMaxWidth(),
                    icon = AppIcons.Trash
                )
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Apagar histórico?") },
            text = { Text("Todos os treinos registrados neste aparelho serão removidos. Os já enviados ao Health Connect continuam lá.") },
            confirmButton = {
                TextButton(onClick = { vm.clearHistory(); confirmClear = false }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } }
        )
    }
}
