package com.yourlife.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.yourlife.app.ui.components.AppIcon
import com.yourlife.app.ui.icons.AppIcons
import com.yourlife.app.ui.screens.ExerciseSheet
import com.yourlife.app.ui.screens.HomeScreen
import com.yourlife.app.ui.screens.PlanDetailScreen
import com.yourlife.app.ui.screens.PlansScreen
import com.yourlife.app.ui.screens.ProgramDetailScreen
import com.yourlife.app.ui.screens.ProgressScreen
import com.yourlife.app.ui.screens.SettingsScreen
import com.yourlife.app.ui.screens.WorkoutScreen

@Composable
fun YourLifeApp(vm: AppViewModel) {
    // ao voltar de outro app (ex.: tela de permissões), confere o estado do Health Connect
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshHealth(); vm.onResumed() }

    val route = vm.route
    BackHandler(enabled = route != Route.Home && route !is Route.Workout) { vm.goBack() }

    val showBar = route == Route.Home || route == Route.Plans || route == Route.Progress || route == Route.Settings

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(visible = showBar, enter = fadeIn(), exit = fadeOut()) {
                BottomBar(route) { vm.route = it }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            AnimatedContent(
                targetState = route,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(140)) },
                label = "route"
            ) { r ->
                when (r) {
                    Route.Home -> HomeScreen(vm, padding)
                    Route.Plans -> PlansScreen(vm, padding)
                    Route.Progress -> ProgressScreen(vm, padding)
                    Route.Settings -> SettingsScreen(vm, padding)
                    is Route.PlanDetail -> PlanDetailScreen(vm, r.planId, r.level, r.deload)
                    is Route.ProgramDetail -> ProgramDetailScreen(vm, r.programId)
                    is Route.Workout -> WorkoutScreen(vm)
                }
            }
        }
    }

    vm.sheetExercise?.let { ExerciseSheet(it) { vm.sheetExercise = null } }
}

@Composable
private fun BottomBar(current: Route, onSelect: (Route) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BarItem("Treinos", AppIcons.Dumbbell, current == Route.Home) { onSelect(Route.Home) }
        BarItem("Planos", AppIcons.Calendar, current == Route.Plans) { onSelect(Route.Plans) }
        BarItem("Progresso", AppIcons.Chart, current == Route.Progress) { onSelect(Route.Progress) }
        BarItem("Ajustes", AppIcons.Sliders, current == Route.Settings) { onSelect(Route.Settings) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { AppIcon(icon) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
