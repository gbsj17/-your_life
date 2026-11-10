@file:OptIn(ExperimentalMaterial3Api::class)

package com.yourlife.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Bg = Color(0xFF0F1115)
private val CardBg = Color(0xFF1A1D24)
private val Line = Color(0xFF272B35)
private val Accent = Color(0xFFFF4D4D)
private val Muted = Color(0xFF9AA3B2)
private val TextMain = Color(0xFFF2F4F8)

@Composable
fun YourLifeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Color.White,
            background = Bg,
            onBackground = TextMain,
            surface = CardBg,
            onSurface = TextMain
        ),
        content = content
    )
}

sealed interface Screen {
    data object Plans : Screen
    data object Progress : Screen
    data class Detail(val plan: Plan) : Screen
    data class Workout(val plan: Plan) : Screen
}

private fun fmt(s: Int): String = "%02d:%02d".format(s / 60, s % 60)

@Composable
fun YourLifeApp(store: Store) {
    var screen by remember { mutableStateOf<Screen>(Screen.Plans) }
    var history by remember { mutableStateOf(store.all()) }

    BackHandler(enabled = screen is Screen.Detail) { screen = Screen.Plans }

    Scaffold(
        bottomBar = {
            if (screen !is Screen.Workout) {
                NavigationBar(containerColor = CardBg) {
                    NavigationBarItem(
                        selected = screen is Screen.Plans || screen is Screen.Detail,
                        onClick = { screen = Screen.Plans },
                        icon = { Text("🏋️") },
                        label = { Text("Treinos") }
                    )
                    NavigationBarItem(
                        selected = screen is Screen.Progress,
                        onClick = {
                            history = store.all()
                            screen = Screen.Progress
                        },
                        icon = { Text("📈") },
                        label = { Text("Progresso") }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = screen) {
                is Screen.Plans -> PlansScreen(onOpen = { screen = Screen.Detail(it) })
                is Screen.Detail -> DetailScreen(
                    plan = s.plan,
                    onBack = { screen = Screen.Plans },
                    onStart = { screen = Screen.Workout(s.plan) }
                )
                is Screen.Workout -> WorkoutScreen(
                    plan = s.plan,
                    onSave = { seconds ->
                        store.add(
                            Session(
                                date = LocalDate.now().toString(),
                                ts = System.currentTimeMillis(),
                                planId = s.plan.id,
                                name = s.plan.name,
                                seconds = seconds
                            )
                        )
                        history = store.all()
                    },
                    onClose = { screen = Screen.Progress },
                    onQuit = { screen = Screen.Detail(s.plan) }
                )
                is Screen.Progress -> ProgressScreen(
                    history = history,
                    onClear = {
                        store.clear()
                        history = emptyList()
                    }
                )
            }
        }
    }
}

@Composable
private fun Header(text: String) {
    Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun BigButton(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    ghost: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = if (ghost) {
            ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = TextMain)
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

// ---------- lista de planos ----------
@Composable
fun PlansScreen(onOpen: (Plan) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Header("Treinos")
        PLANS.forEach { p ->
            Card(
                onClick = { onOpen(p) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(p.icon, fontSize = 32.sp)
                    Column {
                        Text(p.name, fontWeight = FontWeight.Bold)
                        Text("${p.exercises.size} exercícios · ~${p.estMin} min · ${p.level}", color = Muted, fontSize = 13.sp)
                        Text(p.desc, color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ---------- detalhes do plano ----------
@Composable
fun DetailScreen(plan: Plan, onBack: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", fontSize = 22.sp, color = TextMain) }
            Header(plan.name)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("${plan.desc} Duração estimada: ~${plan.estMin} min.", color = Muted)
            plan.exercises.forEach { e ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(e.name, fontWeight = FontWeight.Bold)
                        Text("${e.sets} × ${e.target} · descanso ${e.rest}s", color = Accent, fontSize = 14.sp)
                        Text(e.tip, color = Muted, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        BigButton("Iniciar treino", onClick = onStart)
    }
}

// ---------- treino em andamento ----------
@Composable
fun WorkoutScreen(plan: Plan, onSave: (Int) -> Unit, onClose: () -> Unit, onQuit: () -> Unit) {
    val startedAt = remember { System.currentTimeMillis() }
    var idx by remember { mutableIntStateOf(0) }      // exercício atual
    var serie by remember { mutableIntStateOf(1) }    // série atual
    var phase by remember { mutableStateOf("work") }  // work | rest | done
    var running by remember { mutableStateOf(false) } // cronômetro de série por tempo
    var endAt by remember { mutableLongStateOf(0L) }  // horário em que o cronômetro termina
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var total by remember { mutableIntStateOf(0) }
    var showExit by remember { mutableStateOf(false) }

    val e = plan.exercises[idx]

    fun completeSet() {
        val cur = plan.exercises[idx]
        running = false
        if (serie < cur.sets) {
            serie++
        } else if (idx < plan.exercises.size - 1) {
            idx++
            serie = 1
        } else {
            total = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
            onSave(total)
            phase = "done"
            return
        }
        phase = "rest"
        endAt = System.currentTimeMillis() + cur.rest * 1000L // descanso do exercício que acabou de ser feito
    }

    // Atualiza o relógio a cada 250ms. Usa o horário de término (endAt), então a contagem
    // continua correta mesmo se a tela apagar por um tempo.
    LaunchedEffect(phase, running) {
        while (phase == "rest" || (phase == "work" && running)) {
            now = System.currentTimeMillis()
            if (now >= endAt) {
                if (phase == "rest") phase = "work" else completeSet()
                break
            }
            delay(250)
        }
    }

    BackHandler { if (phase == "done") onClose() else showExit = true }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Sair do treino?") },
            text = { Text("O progresso desta sessão será perdido.") },
            confirmButton = { TextButton(onClick = onQuit) { Text("Sair") } },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Continuar") } }
        )
    }

    val left = ((endAt - now + 999) / 1000).coerceAtLeast(0).toInt()

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { if (phase == "done") 1f else idx.toFloat() / plan.exercises.size },
            modifier = Modifier.fillMaxWidth(),
            color = Accent,
            trackColor = Line
        )
        Spacer(Modifier.height(32.dp))

        when (phase) {
            "rest" -> {
                Text("Descanso", color = Muted)
                Text(fmt(left), fontSize = 72.sp, fontWeight = FontWeight.ExtraBold)
                Text("Próximo: ${e.name} · série $serie/${e.sets}", color = Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BigButton("+15s", modifier = Modifier.weight(1f), ghost = true) { endAt += 15000 }
                    BigButton("Pular", modifier = Modifier.weight(1f)) { phase = "work" }
                }
            }

            "done" -> {
                Spacer(Modifier.height(24.dp))
                Text("🎉", fontSize = 64.sp)
                Text("Treino concluído!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("${plan.name} · ${fmt(total)}", color = Muted)
                Spacer(Modifier.height(16.dp))
                BigButton("Ver progresso", onClick = onClose)
            }

            else -> {
                val t = e.time
                Text("Exercício ${idx + 1}/${plan.exercises.size}", color = Muted)
                Text(e.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Série $serie/${e.sets}", color = Muted)
                Spacer(Modifier.height(24.dp))
                if (t == null) {
                    Text("${e.reps} repetições", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                    BigButton("Concluir série") { completeSet() }
                } else if (!running) {
                    Text("${t}s", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                    BigButton("Iniciar") {
                        endAt = System.currentTimeMillis() + t * 1000L
                        now = System.currentTimeMillis()
                        running = true
                    }
                } else {
                    Text(fmt(left), fontSize = 72.sp, fontWeight = FontWeight.ExtraBold)
                    BigButton("Concluir agora", ghost = true) { completeSet() }
                }
                Spacer(Modifier.height(24.dp))
                Text(e.tip, color = Muted, fontSize = 15.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

// ---------- progresso ----------
@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.background(CardBg, RoundedCornerShape(14.dp)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProgressScreen(history: List<Session>, onClear: () -> Unit) {
    var confirmClear by remember { mutableStateOf(false) }

    val days = history.map { it.date }.toSet()
    val today = LocalDate.now()

    // sequência: dias seguidos com treino (hoje ainda pode estar em aberto)
    var streak = 0
    var d = if (today.toString() in days) today else today.minusDays(1)
    while (d.toString() in days) {
        streak++
        d = d.minusDays(1)
    }

    // últimos 7 dias
    val names = listOf("D", "S", "T", "Q", "Q", "S", "S") // indexado por dayOfWeek % 7 (domingo = 0)
    val week = (6 downTo 0).map { i ->
        val day = today.minusDays(i.toLong())
        names[day.dayOfWeek.value % 7] to history.count { it.date == day.toString() }
    }
    val weekTotal = week.sumOf { it.second }
    val mins = Math.round(history.sumOf { it.seconds } / 60f)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Header("Progresso")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat("${history.size}", "treinos", Modifier.weight(1f))
            Stat("$weekTotal", "últimos 7 dias", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat("$streak", "dias seguidos", Modifier.weight(1f))
            Stat("$mins", "minutos", Modifier.weight(1f))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            week.forEach { (label, n) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.fillMaxWidth().height(80.dp).background(CardBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val fraction = if (n > 0) minOf(1f, n * 0.4f) else 0.06f
                        Box(
                            Modifier.fillMaxWidth().fillMaxHeight(fraction)
                                .background(if (n > 0) Accent else Line, RoundedCornerShape(8.dp))
                        )
                    }
                    Text(label, color = Muted, fontSize = 12.sp)
                }
            }
        }

        if (history.isEmpty()) {
            Text(
                "Nenhum treino ainda. Conclua o primeiro para ver seu progresso.",
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Histórico", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            val fmtDate = DateTimeFormatter.ofPattern("dd/MM")
            history.takeLast(20).reversed().forEach { s ->
                val date = Instant.ofEpochMilli(s.ts).atZone(ZoneId.systemDefault()).format(fmtDate)
                Column(
                    Modifier.fillMaxWidth().background(CardBg, RoundedCornerShape(12.dp)).padding(14.dp)
                ) {
                    Text(s.name, fontWeight = FontWeight.Bold)
                    Text("$date · ${fmt(s.seconds)}", color = Accent, fontSize = 14.sp)
                }
            }
            BigButton("Apagar histórico", ghost = true) { confirmClear = true }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Apagar histórico?") },
            text = { Text("Todos os treinos registrados serão removidos.") },
            confirmButton = {
                TextButton(onClick = { onClear(); confirmClear = false }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } }
        )
    }
}
