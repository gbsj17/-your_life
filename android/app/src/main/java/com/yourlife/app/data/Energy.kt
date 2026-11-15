package com.yourlife.app.data

/** Estimativa de gasto de energia por MET (Compendium of Physical Activities). */
object Energy {
    /** Mínimo semanal de atividade moderada recomendado pelo ACSM para saúde. */
    const val WEEKLY_GOAL_MIN = 150

    /**
     * Calistenia, esforço moderado: 3,8 MET; calistenia vigorosa (polichinelo, burpee): 7,5 MET (Compendium 2024).
     * Treinos com foco em cardio entram como vigorosos; os demais, como moderados.
     */
    fun met(planId: String): Double =
        runCatching { if (Muscle.CARDIO in Catalog.plan(planId).focus) 7.5 else 3.8 }.getOrDefault(3.8)

    /** kcal = MET × peso (kg) × horas. É uma estimativa, não uma medição. */
    fun kcal(s: Session, weightKg: Int): Double = met(s.planId) * weightKg * s.seconds / 3600.0
}
