package com.yourlife.app.data

import java.time.LocalDate

/** Escada de progressão: cada exercício aponta para a variação mais fácil / mais difícil (mesma unidade: reps ou tempo). */
private val EASIER = mapOf(
    "flexao-pes-elevados" to "flexao", "flexao" to "flexao-joelhos", "flexao-larga" to "flexao-joelhos",
    "flexao-diamante" to "flexao", "agachamento-salto" to "agachamento", "afundo" to "agachamento",
    "bulgaro" to "afundo", "pike-elevado" to "pike", "ponte-unilateral" to "ponte",
    "bicicleta" to "abdominal", "elevacao-pernas" to "tesoura", "escalador" to "joelho-alto",
    "remada-supinada" to "remada", "superman-y" to "superman"
)
private val HARDER = mapOf(
    "flexao-joelhos" to "flexao", "flexao" to "flexao-pes-elevados", "flexao-larga" to "flexao-pes-elevados",
    "flexao-diamante" to "flexao-pes-elevados", "agachamento" to "agachamento-salto", "afundo" to "bulgaro",
    "pike" to "pike-elevado", "ponte" to "ponte-unilateral", "abdominal" to "bicicleta",
    "tesoura" to "elevacao-pernas", "joelho-alto" to "escalador", "remada" to "remada-supinada",
    "superman" to "superman-y"
)

/**
 * Escala um treino (escrito no nível Médio) para outro nível.
 * Iniciante: variação mais fácil, -1 série, -20% reps/tempo, +25% descanso.
 * Avançado: variação mais difícil, +1 série, +25% reps/tempo, -20% descanso.
 * Semana leve (deload): -1 série em cada exercício, para recuperar.
 */
fun Plan.forLevel(target: Level, deloadWeek: Boolean = false): Plan {
    val scaled = items.map { it0 ->
        when (target) {
            Level.MEDIO -> it0
            Level.INICIANTE -> PlanItem(
                exerciseId = EASIER[it0.exerciseId] ?: it0.exerciseId,
                sets = (it0.sets - 1).coerceAtLeast(2),
                rest = Math.round(it0.rest * 1.25f),
                reps = it0.reps?.let { r -> Math.round(r * 0.8f).coerceAtLeast(5) },
                seconds = it0.seconds?.let { s -> (Math.round(s * 0.8f / 5f) * 5).coerceAtLeast(15) }
            )
            Level.AVANCADO -> PlanItem(
                exerciseId = HARDER[it0.exerciseId] ?: it0.exerciseId,
                sets = it0.sets + 1,
                rest = Math.round(it0.rest * 0.8f),
                reps = it0.reps?.let { r -> Math.round(r * 1.25f) },
                seconds = it0.seconds?.let { s -> Math.round(s * 1.25f / 5f) * 5 }
            )
        }
    }.map { if (deloadWeek) PlanItem(it.exerciseId, (it.sets - 1).coerceAtLeast(1), it.rest, it.reps, it.seconds) else it }
    return Plan(id, name, tagline, target, icon, focus, scaled, combo, deloadWeek)
}

/** Séries por semana para cada músculo (conta o músculo principal de cada exercício; cardio fica de fora). */
fun weeklySets(plans: List<Plan>): List<Pair<Muscle, Int>> {
    val acc = LinkedHashMap<Muscle, Int>()
    for (p in plans) for (it in p.items) {
        val m = it.exercise().muscles.firstOrNull() ?: continue
        if (m == Muscle.CARDIO) continue
        acc[m] = (acc[m] ?: 0) + it.sets
    }
    return acc.entries.sortedByDescending { it.value }.map { it.key to it.value }
}

/** Onde o usuário está dentro do programa hoje. */
class ProgramStatus(
    val program: Program,
    val dayNumber: Int,          // 1..totalDays
    val totalDays: Int,
    val phaseIndex: Int,
    val phase: ProgramPhase,
    val dayInPhase: Int,         // 1..dias da fase
    val phaseDays: Int,
    val weekInPhase: Int,        // 1..
    val deload: Boolean,
    val level: Level,            // nível efetivo (fase + ajuste manual)
    val todayPlan: Plan?,        // null = descanso
    val finished: Boolean
)

object Programs {
    val all: List<Program> = listOf(
        program(
            "queima-barriga", "Queima de barriga", "Perder barriga",
            "Cardio, core e corpo todo",
            "Combina treino de corpo todo, HIIT e core para aumentar o gasto de energia. O resultado na cintura vem do déficit calorie total e da constância ao longo das semanas.",
            "Não existe perda de gordura localizada: abdominais sozinhos não queimam a barriga. Por isso o plano mistura corpo todo, cardio e core. Espere resultados visíveis entre 8 e 12 semanas, junto com alimentação.",
            PlanIcon.FLAME,
            ini = listOf("combo-queima", null, "core-barriga", null, "corpo-todo", null, null),
            med = listOf("combo-queima", "core-barriga", null, "corpo-todo", null, "hiit-cardio", null),
            ava = listOf("combo-queima", "corpo-todo", "core-barriga", null, "hiit-cardio", "corpo-todo", null)
        ),
        program(
            "massa-corpo-todo", "Massa muscular", "Ganhar massa",
            "Corpo todo, progressão de dificuldade",
            "Treina todos os grupos musculares cerca de 2 vezes por semana, que é uma boa frequência para ganhar massa. A dificuldade sobe com variações mais difíceis, mais repetições e menos descanso.",
            "Para ganhar músculo, busque ao menos 10 séries por semana por grupo muscular, treinando perto da falha, e coma proteína e calorias suficientes.",
            PlanIcon.DUMBBELL,
            ini = listOf("corpo-todo", null, "corpo-todo", null, "corpo-todo", null, null),
            med = listOf("peito-triceps", "pernas-gluteos", null, "costas-postura", "ombros-abdomen", null, null),
            ava = listOf("peito-triceps", "pernas-gluteos", null, "costas-postura", "ombros-fortes", "bracos", null)
        ),
        program(
            "massa-bracos", "Massa nos braços", "Braços",
            "Bíceps, tríceps e ombros",
            "Foco em bíceps, tríceps e ombros, com os demais grupos mantidos para o corpo crescer por igual.",
            null,
            PlanIcon.DUMBBELL,
            ini = listOf("bracos", null, "peito-triceps", null, "bracos", null, null),
            med = listOf("bracos", "costas-postura", null, "combo-bracos-ombros", null, "peito-triceps", null),
            ava = listOf("bracos", "peito-triceps", null, "combo-bracos-ombros", "costas-postura", "bracos", null)
        ),
        program(
            "pernas-gluteos", "Pernas e glúteos", "Pernas",
            "Parte inferior com progressão",
            "Pernas e glúteos em treinos dedicados, com agachamentos, afundos e pontes que evoluem para variações unilaterais e com salto.",
            null,
            PlanIcon.PEAK,
            ini = listOf("pernas-gluteos", null, "rotina-rapida", null, "pernas-gluteos", null, null),
            med = listOf("pernas-gluteos", "core-barriga", null, "combo-pernas", null, "pernas-gluteos", null),
            ava = listOf("pernas-gluteos", "peito-triceps", null, "combo-pernas", "core-barriga", "pernas-gluteos", null)
        ),
        program(
            "ombros", "Ombros largos", "Ombros",
            "Ombros, costas e core",
            "Ombros com pike, flexões abertas e Y, apoiados por costas e core para postura e estabilidade.",
            null,
            PlanIcon.SHIELD,
            ini = listOf("ombros-fortes", null, "costas-postura", null, "ombros-abdomen", null, null),
            med = listOf("ombros-fortes", "pernas-gluteos", null, "ombros-abdomen", null, "costas-postura", null),
            ava = listOf("ombros-fortes", "pernas-gluteos", "combo-bracos-ombros", null, "ombros-abdomen", "costas-postura", null)
        ),
        program(
            "condicionamento", "Condicionamento", "Fôlego",
            "Cardio e resistência",
            "Melhora o fôlego e a resistência com HIIT e treinos de corpo todo, começando leve e subindo aos poucos.",
            null,
            PlanIcon.BOLT,
            ini = listOf("rotina-rapida", null, "hiit-cardio", null, "corpo-todo", null, null),
            med = listOf("corpo-todo", "hiit-cardio", null, "combo-queima", null, "rotina-rapida", null),
            ava = listOf("corpo-todo", "hiit-cardio", "combo-pernas", null, "combo-queima", "combo-superior", null)
        )
    )

    private fun program(
        id: String, name: String, goal: String, tagline: String, description: String, note: String?, icon: PlanIcon,
        ini: List<String?>, med: List<String?>, ava: List<String?>
    ) = Program(
        id, name, goal, tagline, description, note, icon,
        listOf(
            ProgramPhase(Level.INICIANTE, 28, ini),
            ProgramPhase(Level.MEDIO, 28, med),
            ProgramPhase(Level.AVANCADO, 28, ava)
        )
    )

    private val byId = all.associateBy { it.id }
    fun get(id: String): Program? = byId[id]

    fun defaultDays(program: Program): List<Int> = program.phases.map { it.days }

    fun totalDays(days: List<Int>) = days.sum()

    /** Total de treinos previstos no programa inteiro (conta os dias não vazios de cada semana). */
    fun plannedTotal(e: Enrollment): Int {
        val program = get(e.programId) ?: return 0
        var count = 0
        var day = 0
        for ((i, days) in e.phaseDays.withIndex()) {
            val week = program.phases[i].week
            repeat(days) {
                val dow = LocalDate.ofEpochDay(e.startDay + day).dayOfWeek.value - 1
                if (week[dow] != null) count++
                day++
            }
        }
        return count
    }

    /** Estado do programa na data `today`. */
    fun status(e: Enrollment, today: LocalDate = LocalDate.now()): ProgramStatus? {
        val program = get(e.programId) ?: return null
        val total = e.phaseDays.sum()
        val idx = (today.toEpochDay() - e.startDay).toInt().coerceAtLeast(0)
        val finished = idx >= total
        val pos = if (finished) total - 1 else idx
        var start = 0
        var phaseIndex = program.phases.lastIndex
        for (i in program.phases.indices) {
            val d = e.phaseDays.getOrElse(i) { 0 }
            if (d > 0 && pos < start + d) { phaseIndex = i; break }
            start += d
        }
        val phase = program.phases[phaseIndex]
        val phaseDays = e.phaseDays.getOrElse(phaseIndex) { 0 }
        val dayInPhase = (pos - start).coerceAtLeast(0)
        val week = dayInPhase / 7
        val deload = phaseDays >= 28 && week % 4 == 3
        val level = Level.clamp(phase.level.ordinal + e.levelOffset)
        val planId = phase.week[today.dayOfWeek.value - 1]
        val today0 = if (finished) null else planId?.let { Catalog.plan(it).forLevel(level, deload) }
        return ProgramStatus(
            program, pos + 1, total, phaseIndex, phase, dayInPhase + 1, phaseDays,
            week + 1, deload, level, today0, finished
        )
    }
}
