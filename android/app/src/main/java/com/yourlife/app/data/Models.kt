package com.yourlife.app.data

/** Objeto de cena (cadeira, mesa, parede). kind: 0 = retângulo, 1 = linha. */
class Prop(val kind: Int, val x0: Float, val y0: Float, val x1: Float, val y1: Float)

/** Poses A e B (18 números cada) geradas por tools/gen_poses.py. */
/** `front` = desenhada de frente (dois ombros e dois quadris); caso contrário, de perfil. */
class AnimGeometry(val a: FloatArray, val b: FloatArray, val props: List<Prop>, val front: Boolean = false)

enum class Muscle(val label: String) {
    PEITO("Peito"),
    TRICEPS("Tríceps"),
    OMBROS("Ombros"),
    COSTAS("Costas"),
    BICEPS("Bíceps"),
    ABDOMEN("Abdômen"),
    QUADRICEPS("Quadríceps"),
    GLUTEOS("Glúteos"),
    POSTERIOR("Posteriores"),
    PANTURRILHA("Panturrilhas"),
    LOMBAR("Lombar"),
    CARDIO("Cardio")
}

/** Os três níveis de dificuldade, em ordem crescente. */
enum class Level(val label: String) {
    INICIANTE("Iniciante"),
    MEDIO("Médio"),
    AVANCADO("Avançado");

    companion object {
        fun clamp(index: Int): Level = entries[index.coerceIn(0, entries.size - 1)]
    }
}

enum class PlanIcon { FLAME, DUMBBELL, BOLT, TARGET, SHIELD, PEAK }

class Exercise(
    val id: String,
    val name: String,
    val anim: String,
    val muscles: List<Muscle>,
    val level: Level,
    val phaseA: String,
    val phaseB: String,
    val steps: List<String>,
    val mistakes: List<String>,
    val cycleMs: Int = 1300,
    val equipment: String? = null
)

/** Um exercício dentro de um plano: reps OU seconds (um dos dois). */
class PlanItem(
    val exerciseId: String,
    val sets: Int,
    val rest: Int,
    val reps: Int? = null,
    val seconds: Int? = null
) {
    val target: String get() = if (seconds != null) "${seconds}s" else "$reps reps"
}

/**
 * Um treino. Os planos do catálogo são escritos no nível Médio;
 * `Plan.forLevel()` gera as versões Iniciante e Avançado.
 */
class Plan(
    val id: String,
    val name: String,
    val tagline: String,
    val level: Level,
    val icon: PlanIcon,
    val focus: List<Muscle>,
    val items: List<PlanItem>,
    val combo: Boolean = false,
    /** Semana leve (recuperação): menos séries. */
    val deload: Boolean = false
) {
    /** Estimativa: 40 s por série de repetições; séries por tempo usam o próprio tempo. */
    val estMin: Int
        get() = Math.round(items.sumOf { it.sets * ((it.seconds ?: 40) + it.rest) } / 60f)

    val totalSets: Int get() = items.sumOf { it.sets }
}

/** Uma fase do programa: quantos dias no nível e qual é a semana de treinos (segunda a domingo). */
class ProgramPhase(
    val level: Level,
    val days: Int,
    /** 7 posições, segunda a domingo; null = descanso. */
    val week: List<String?>
)

class Program(
    val id: String,
    val name: String,
    val goal: String,
    val tagline: String,
    val description: String,
    val note: String?,
    val icon: PlanIcon,
    val phases: List<ProgramPhase>
)

/** O programa que o usuário está seguindo. */
data class Enrollment(
    val programId: String,
    val startDay: Long,            // LocalDate.toEpochDay()
    val phaseDays: List<Int>,      // dias em cada fase (iniciante, médio, avançado)
    val levelOffset: Int = 0       // ajuste manual: -2..+2 níveis
)

data class Session(
    val id: Long,
    val date: String,   // AAAA-MM-DD
    val startTs: Long,
    val ts: Long,       // fim do treino
    val planId: String,
    val name: String,
    val seconds: Int,
    val synced: Boolean
)

data class Settings(
    val theme: Int = 0,        // 0 = sistema, 1 = claro, 2 = escuro
    val autoSync: Boolean = true,
    val reminderOn: Boolean = false,
    val reminderMinutes: Int = 18 * 60,   // horário do lembrete, em minutos desde meia-noite
    val weightKg: Int = 0                 // 0 = não informado (usado só na estimativa de calorias)
)
