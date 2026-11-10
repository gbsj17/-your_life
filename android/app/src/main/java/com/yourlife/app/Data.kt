package com.yourlife.app

// Cada exercício usa "reps" (repetições) OU "time" (segundos).
// sets = séries | rest = descanso (segundos) após cada série.
data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int? = null,
    val time: Int? = null,
    val rest: Int,
    val tip: String
) {
    val target: String get() = if (time != null) "${time}s" else "$reps reps"
}

data class Plan(
    val id: String,
    val name: String,
    val icon: String,
    val level: String,
    val desc: String,
    val exercises: List<Exercise>
) {
    // Estimativa: 40s por série de repetições; séries por tempo usam o próprio tempo.
    val estMin: Int
        get() = Math.round(exercises.sumOf { it.sets * ((it.time ?: 40) + it.rest) } / 60f)
}

val PLANS = listOf(
    Plan(
        id = "corpo-todo", name = "Corpo todo", icon = "🔥", level = "Iniciante",
        desc = "Treino completo em casa, sem equipamento.",
        exercises = listOf(
            Exercise("Agachamento", sets = 3, reps = 15, rest = 45, tip = "Pés na largura dos ombros, joelhos alinhados com os pés e coluna reta."),
            Exercise("Flexão de braços", sets = 3, reps = 10, rest = 45, tip = "Corpo em linha reta. Se pesar, apoie os joelhos no chão."),
            Exercise("Afundo (cada lado)", sets = 3, reps = 10, rest = 45, tip = "Dê um passo à frente e desça até os dois joelhos formarem cerca de 90°."),
            Exercise("Superman", sets = 3, reps = 12, rest = 30, tip = "Deitado de bruços, eleve braços e pernas juntos e segure 1 segundo."),
            Exercise("Polichinelo", sets = 3, time = 30, rest = 30, tip = "Ritmo constante, aterrissando suave nos pés."),
            Exercise("Prancha", sets = 3, time = 30, rest = 30, tip = "Cotovelos sob os ombros, abdômen firme, sem deixar o quadril cair.")
        )
    ),
    Plan(
        id = "peito-triceps", name = "Peito e tríceps", icon = "💪", level = "Iniciante/Intermediário",
        desc = "Foco na parte superior do corpo, sem equipamento.",
        exercises = listOf(
            Exercise("Flexão de braços", sets = 4, reps = 12, rest = 60, tip = "Desça até o peito quase tocar o chão e suba sem estender o cotovelo de vez."),
            Exercise("Flexão com mãos afastadas", sets = 3, reps = 10, rest = 60, tip = "Mãos mais abertas que os ombros para enfatizar o peito."),
            Exercise("Flexão diamante", sets = 3, reps = 8, rest = 60, tip = "Mãos juntas formando um losango; trabalha mais o tríceps."),
            Exercise("Mergulho na cadeira", sets = 3, reps = 10, rest = 60, tip = "Use uma cadeira firme. Desça flexionando os cotovelos para trás."),
            Exercise("Prancha com toque no ombro", sets = 3, reps = 20, rest = 45, tip = "Alterne as mãos tocando o ombro oposto sem balançar o quadril.")
        )
    ),
    Plan(
        id = "costas-biceps", name = "Costas e bíceps", icon = "🦍", level = "Iniciante/Intermediário",
        desc = "Costas e postura. A remada precisa de uma mesa firme.",
        exercises = listOf(
            Exercise("Superman", sets = 4, reps = 12, rest = 45, tip = "Eleve braços e pernas ao mesmo tempo, sem forçar o pescoço."),
            Exercise("Superman em Y", sets = 3, reps = 12, rest = 45, tip = "De bruços, braços abertos em Y; eleve-os apertando as costas."),
            Exercise("Remada invertida na mesa", sets = 4, reps = 10, rest = 60, tip = "Deitado sob uma mesa resistente, puxe o peito até a borda. Teste a mesa antes."),
            Exercise("Ponte de glúteo", sets = 3, reps = 15, rest = 45, tip = "Eleve o quadril apertando glúteos e lombar sem arquear demais."),
            Exercise("Prancha invertida", sets = 3, time = 20, rest = 45, tip = "Sentado com as mãos atrás, eleve o quadril e mantenha o corpo reto.")
        )
    ),
    Plan(
        id = "pernas-gluteos", name = "Pernas e glúteos", icon = "🦵", level = "Iniciante/Intermediário",
        desc = "Parte inferior do corpo, sem equipamento.",
        exercises = listOf(
            Exercise("Agachamento", sets = 4, reps = 15, rest = 60, tip = "Desça controlando e suba empurrando o chão com os calcanhares."),
            Exercise("Afundo (cada lado)", sets = 3, reps = 12, rest = 60, tip = "Tronco ereto e joelho da frente alinhado com o pé."),
            Exercise("Agachamento búlgaro (cada lado)", sets = 3, reps = 10, rest = 60, tip = "Pé de trás apoiado numa cadeira; desça em linha reta."),
            Exercise("Elevação de quadril", sets = 4, reps = 15, rest = 45, tip = "No topo, aperte os glúteos por 1 segundo."),
            Exercise("Panturrilha em pé", sets = 4, reps = 20, rest = 30, tip = "Suba na ponta dos pés e desça devagar."),
            Exercise("Agachamento na parede", sets = 3, time = 40, rest = 45, tip = "Costas na parede, coxas paralelas ao chão.")
        )
    ),
    Plan(
        id = "ombros-abdomen", name = "Ombros e abdômen", icon = "🎯", level = "Iniciante/Intermediário",
        desc = "Ombros e core, sem equipamento.",
        exercises = listOf(
            Exercise("Flexão pike", sets = 4, reps = 8, rest = 60, tip = "Quadril alto formando um V invertido; desça a cabeça entre as mãos."),
            Exercise("Prancha", sets = 3, time = 45, rest = 45, tip = "Abdômen e glúteos contraídos, corpo em linha reta."),
            Exercise("Escalador", sets = 3, time = 30, rest = 30, tip = "Em posição de flexão, traga os joelhos ao peito alternadamente."),
            Exercise("Abdominal", sets = 3, reps = 20, rest = 30, tip = "Suba o tronco sem puxar o pescoço com as mãos."),
            Exercise("Elevação de pernas", sets = 3, reps = 12, rest = 45, tip = "Deitado, eleve as pernas retas mantendo a lombar no chão."),
            Exercise("Prancha lateral (cada lado)", sets = 3, time = 30, rest = 30, tip = "Apoie o antebraço e mantenha o quadril elevado.")
        )
    )
)
