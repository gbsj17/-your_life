package com.yourlife.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Trocas de exercício escolhidas pelo usuário (exercício original -> substituto).
 * Fica num objeto único e observável para que qualquer tela que monte um treino já use a troca.
 */
object Swaps {
    var map by mutableStateOf<Map<String, String>>(emptyMap())

    fun resolve(id: String): String = map[id] ?: id

    /** Exercícios que aparecem com tempo (e não repetições) em algum treino do catálogo. */
    private val timed: Set<String> by lazy {
        Catalog.plans.flatMap { p -> p.items.filter { it.seconds != null }.map { it.exerciseId } }.toSet()
    }

    fun isTimed(id: String) = id in timed

    /**
     * Substitutos de `original`: trabalham o mesmo músculo principal, usam a mesma medida
     * (repetições ou tempo) e são do mesmo nível ou mais fáceis. Os mais fáceis vêm primeiro.
     */
    fun alternatives(original: String): List<Exercise> {
        val ex = Catalog.exercise(original)
        val pool = Catalog.exercises.filter {
            it.id != original && isTimed(it.id) == isTimed(original) && it.level.ordinal <= ex.level.ordinal
        }
        val main = pool.filter { it.muscles.firstOrNull() == ex.muscles.firstOrNull() }
        val list = main.ifEmpty { pool.filter { o -> o.muscles.any { it in ex.muscles } } }
        return list.sortedBy { it.level.ordinal }
    }
}
