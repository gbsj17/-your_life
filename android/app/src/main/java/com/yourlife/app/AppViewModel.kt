package com.yourlife.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.health.connect.client.HealthConnectClient
import com.yourlife.app.data.Enrollment
import com.yourlife.app.data.Exercise
import com.yourlife.app.data.Level
import com.yourlife.app.data.ProgramStatus
import com.yourlife.app.data.Programs
import com.yourlife.app.data.Swaps
import com.yourlife.app.data.Plan
import com.yourlife.app.data.PlanItem
import com.yourlife.app.data.Session
import com.yourlife.app.data.Settings
import com.yourlife.app.data.Store
import com.yourlife.app.health.HealthConnectManager
import com.yourlife.app.update.UpdateInfo
import com.yourlife.app.update.Updater
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface Route {
    data object Home : Route
    data object Plans : Route
    data object Progress : Route
    data object Settings : Route
    data class PlanDetail(val planId: String, val level: Level = Level.MEDIO, val deload: Boolean = false) : Route
    data class ProgramDetail(val programId: String) : Route
    data object Month : Route
    data class Workout(val planId: String) : Route
}

enum class Phase { WORK, RUN, REST, DONE }

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState
    data class Ready(val info: UpdateInfo, val file: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/** Estado do treino em andamento. */
class WorkoutState(val plan: Plan, val startedAt: Long) {
    var index by mutableIntStateOf(0)
    var setNo by mutableIntStateOf(1)
    var phase by mutableStateOf(Phase.WORK)
    var paused by mutableStateOf(false)
    var endAt by mutableLongStateOf(0L)
    var leftMs by mutableLongStateOf(0L)
    var totalMs by mutableLongStateOf(1L)
    var totalSeconds by mutableIntStateOf(0)

    val item: PlanItem get() = plan.items[index]
    val hasNext: Boolean get() = index < plan.items.size - 1 || setNo < item.sets
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = Store(app)
    val health = HealthConnectManager(app)

    var route by mutableStateOf<Route>(Route.Home)
    var settings by mutableStateOf(store.settings())
        private set
    var history by mutableStateOf(store.history())
        private set
    var workout by mutableStateOf<WorkoutState?>(null)
        private set
    var sheetExercise by mutableStateOf<Exercise?>(null)
    var enrollment by mutableStateOf(store.enrollment())
        private set

    /** Aumenta a cada alerta (fim de descanso/série) para a tela vibrar. */
    var cue by mutableIntStateOf(0)
        private set

    var hcStatus by mutableIntStateOf(HealthConnectClient.SDK_UNAVAILABLE)
        private set
    var hcGranted by mutableStateOf(false)
        private set
    var syncMessage by mutableStateOf<String?>(null)
        private set
    var syncing by mutableStateOf(false)
        private set

    var update by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    init {
        Swaps.map = store.swaps()
        refreshHealth()
        checkUpdate()
        viewModelScope.launch {
            while (isActive) {
                delay(100)
                tick()
            }
        }
    }

    // ---------- navegação ----------
    fun goBack(): Boolean {
        return when (route) {
            Route.Home -> false
            is Route.ProgramDetail, Route.Month -> { route = Route.Plans; true }
            is Route.PlanDetail, Route.Plans, Route.Progress, Route.Settings -> { route = Route.Home; true }
            is Route.Workout -> false // a tela do treino cuida do próprio "voltar"
        }
    }

    // ---------- atualização do app ----------
    /** Se o Android já permite este app instalar atualizações; reavaliado toda vez que o app volta ao primeiro plano. */
    var installAllowed by mutableStateOf(Updater.canInstall(app))
        private set
    private var installRequested = false

    fun requestInstallPermission() {
        installRequested = true
        Updater.openInstallPermission(getApplication())
    }

    fun installUpdate() {
        val ready = update as? UpdateState.Ready ?: return
        if (Updater.canInstall(getApplication())) {
            installRequested = false
            Updater.install(getApplication(), ready.file)
        } else requestInstallPermission()
    }

    /** Chamado ao voltar para o app: se a permissão acabou de ser dada, segue direto para a instalação. */
    fun onResumed() {
        installAllowed = Updater.canInstall(getApplication())
        if (installAllowed && installRequested && update is UpdateState.Ready) installUpdate()
    }

    fun checkUpdate() {
        if (update is UpdateState.Checking || update is UpdateState.Downloading) return
        update = UpdateState.Checking
        viewModelScope.launch {
            update = try {
                val info = Updater.fetchLatest()
                if (info.versionCode > Updater.installedCode(getApplication())) UpdateState.Available(info)
                else UpdateState.UpToDate
            } catch (e: Exception) {
                UpdateState.Failed("Não foi possível verificar. Confira a internet.")
            }
        }
    }

    fun downloadUpdate() {
        val info = (update as? UpdateState.Available)?.info ?: return
        update = UpdateState.Downloading(info, 0f)
        viewModelScope.launch {
            update = try {
                val file = Updater.download(getApplication(), info) { p ->
                    update = UpdateState.Downloading(info, p)
                }
                UpdateState.Ready(info, file)
            } catch (e: Exception) {
                UpdateState.Failed("Falha no download. Tente de novo.")
            }
        }
    }

    // ---------- trocas de exercício ----------
    /** Troca `original` por `replacement` em todos os treinos; null restaura o original. */
    fun setSwap(original: String, replacement: String?) {
        val m = Swaps.map.toMutableMap()
        if (replacement == null || replacement == original) m.remove(original) else m[original] = replacement
        Swaps.map = m
        store.saveSwaps(m)
    }

    // ---------- programas ----------
    /** Situação de hoje no programa ativo (calculada na hora, depende do dia). */
    val programStatus: ProgramStatus? get() = enrollment?.let { Programs.status(it) }

    fun enroll(programId: String, phaseDays: List<Int>) {
        enrollment = Enrollment(programId, LocalDate.now().toEpochDay(), phaseDays, 0)
        store.saveEnrollment(enrollment)
    }

    fun endProgram() {
        enrollment = null
        store.saveEnrollment(null)
    }

    /** Dias em cada nível do programa ativo, em passos de 7. Zero pula o nível. */
    fun setPhaseDays(index: Int, days: Int) {
        val e = enrollment ?: return
        val list = e.phaseDays.toMutableList()
        list[index] = days.coerceIn(0, 84)
        if (list.sum() < 7) return
        enrollment = e.copy(phaseDays = list)
        store.saveEnrollment(enrollment)
    }

    /** Ajuste manual de dificuldade: -1 mais fácil, +1 mais difícil (em relação ao nível da fase). */
    fun shiftLevel(delta: Int) {
        val e = enrollment ?: return
        enrollment = e.copy(levelOffset = (e.levelOffset + delta).coerceIn(-2, 2))
        store.saveEnrollment(enrollment)
    }

    // ---------- ajustes ----------
    fun setTheme(mode: Int) {
        settings = settings.copy(theme = mode)
        store.saveSettings(settings)
    }

    fun setAutoSync(on: Boolean) {
        settings = settings.copy(autoSync = on)
        store.saveSettings(settings)
    }

    fun clearHistory() {
        store.clearHistory()
        history = emptyList()
    }

    // ---------- Health Connect ----------
    fun refreshHealth() {
        hcStatus = health.status()
        if (hcStatus == HealthConnectClient.SDK_AVAILABLE) {
            viewModelScope.launch { hcGranted = health.hasPermissions() }
        } else {
            hcGranted = false
        }
    }

    fun onPermissionResult(granted: Set<String>) {
        hcGranted = granted.containsAll(health.permissions)
        syncMessage = if (hcGranted) null else "Permissão não concedida."
        if (hcGranted) syncPending()
    }

    val pendingSync: Int get() = history.count { !it.synced }

    fun syncPending() {
        if (!hcGranted || syncing) return
        viewModelScope.launch {
            syncing = true
            val pending = history.filter { !it.synced }
            val done = mutableSetOf<Long>()
            for (s in pending) if (health.write(s)) done.add(s.id)
            if (done.isNotEmpty()) {
                store.markSynced(done)
                history = store.history()
            }
            syncMessage = when {
                pending.isEmpty() -> "Nada a sincronizar."
                done.size == pending.size -> "${done.size} treino(s) enviado(s) ao Health Connect."
                else -> "${done.size} de ${pending.size} treinos enviados. Tente de novo mais tarde."
            }
            syncing = false
        }
    }

    // ---------- treino ----------
    fun startWorkout(plan: Plan) {
        workout = WorkoutState(plan, System.currentTimeMillis())
        route = Route.Workout(plan.id)
    }

    fun quitWorkout() {
        val planId = workout?.plan?.id
        val level = workout?.plan?.level ?: Level.MEDIO
        val deload = workout?.plan?.deload ?: false
        workout = null
        route = if (planId != null) Route.PlanDetail(planId, level, deload) else Route.Home
    }

    fun closeFinishedWorkout() {
        workout = null
        route = Route.Progress
    }

    fun startTimed() {
        val w = workout ?: return
        val sec = w.item.seconds ?: return
        beginCountdown(w, Phase.RUN, sec * 1000L)
    }

    fun completeSet() {
        val w = workout ?: return
        val item = w.item
        when {
            w.setNo < item.sets -> {
                w.setNo++
                beginCountdown(w, Phase.REST, item.rest * 1000L)
            }
            w.index < w.plan.items.size - 1 -> {
                w.index++
                w.setNo = 1
                beginCountdown(w, Phase.REST, item.rest * 1000L)
            }
            else -> finish(w)
        }
    }

    fun skipRest() {
        val w = workout ?: return
        if (w.phase == Phase.REST) w.phase = Phase.WORK
    }

    fun addRest(seconds: Int) {
        val w = workout ?: return
        if (w.phase != Phase.REST) return
        w.endAt += seconds * 1000L
        w.totalMs += seconds * 1000L
        w.leftMs += seconds * 1000L
    }

    fun togglePause() {
        val w = workout ?: return
        if (w.phase != Phase.REST && w.phase != Phase.RUN) return
        if (w.paused) {
            w.endAt = System.currentTimeMillis() + w.leftMs
            w.paused = false
        } else {
            w.leftMs = (w.endAt - System.currentTimeMillis()).coerceAtLeast(0L)
            w.paused = true
        }
    }

    private fun beginCountdown(w: WorkoutState, phase: Phase, ms: Long) {
        w.phase = phase
        w.paused = false
        w.totalMs = ms.coerceAtLeast(1L)
        w.leftMs = ms
        w.endAt = System.currentTimeMillis() + ms
    }

    private fun tick() {
        val w = workout ?: return
        if (w.paused || (w.phase != Phase.REST && w.phase != Phase.RUN)) return
        w.leftMs = (w.endAt - System.currentTimeMillis()).coerceAtLeast(0L)
        if (w.leftMs <= 0L) {
            cue++
            if (w.phase == Phase.REST) w.phase = Phase.WORK else completeSet()
        }
    }

    private fun finish(w: WorkoutState) {
        val now = System.currentTimeMillis()
        val seconds = ((now - w.startedAt) / 1000L).toInt()
        w.totalSeconds = seconds
        w.phase = Phase.DONE
        cue++
        val session = Session(
            id = now,
            date = LocalDate.now().toString(),
            startTs = w.startedAt,
            ts = now,
            planId = w.plan.id,
            name = if (w.plan.level == Level.MEDIO && !w.plan.deload) w.plan.name
                else w.plan.name + " (" + w.plan.level.label + (if (w.plan.deload) ", leve" else "") + ")",
            seconds = seconds,
            synced = false
        )
        store.add(session)
        history = store.history()
        if (settings.autoSync && hcGranted) syncPending()
    }
}
