package com.yourlife.app.data

import java.time.DayOfWeek

/** Todos os exercícios e planos do app. Para editar um treino, mexa só neste arquivo. */
object Catalog {

    val exercises: List<Exercise> = listOf(
        Exercise(
            id = "agachamento", name = "Agachamento", anim = "squat",
            muscles = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS, Muscle.POSTERIOR),
            level = Level.INICIANTE, phaseA = "Em pé", phaseB = "Embaixo",
            steps = listOf(
                "Pés na largura dos ombros, pontas levemente para fora.",
                "Leve o quadril para trás e para baixo, como se fosse sentar numa cadeira.",
                "Desça até as coxas ficarem quase paralelas ao chão, com o peito aberto.",
                "Suba empurrando o chão com os calcanhares."
            ),
            mistakes = listOf("Joelhos caindo para dentro.", "Calcanhares saindo do chão.")
        ),
        Exercise(
            id = "afundo", name = "Afundo (cada lado)", anim = "lunge",
            muscles = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS),
            level = Level.INICIANTE, phaseA = "Em pé", phaseB = "Embaixo",
            steps = listOf(
                "Dê um passo largo à frente.",
                "Desça o joelho de trás em direção ao chão, com o tronco ereto.",
                "O joelho da frente fica alinhado com o pé.",
                "Empurre o chão para voltar. Troque de lado ao terminar as repetições."
            ),
            mistakes = listOf("Passo curto demais, que sobrecarrega o joelho.", "Inclinar o tronco para a frente.")
        ),
        Exercise(
            id = "bulgaro", name = "Agachamento búlgaro (cada lado)", anim = "bulgarian",
            muscles = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS),
            level = Level.MEDIO, phaseA = "Em cima", phaseB = "Embaixo",
            equipment = "Cadeira ou sofá firme",
            steps = listOf(
                "Apoie o peito do pé de trás na cadeira, de costas para ela.",
                "O pé da frente fica um passo à frente, com o tronco ereto.",
                "Desça em linha reta até a coxa da frente ficar quase paralela ao chão.",
                "Suba empurrando o calcanhar da frente. Troque de lado ao terminar."
            ),
            mistakes = listOf("Pé da frente perto demais da cadeira.", "Perder o equilíbrio por apressar a descida.")
        ),
        Exercise(
            id = "polichinelo", name = "Polichinelo", anim = "jack",
            muscles = listOf(Muscle.CARDIO, Muscle.OMBROS),
            level = Level.INICIANTE, phaseA = "Fechado", phaseB = "Aberto", cycleMs = 650,
            steps = listOf(
                "Comece em pé, com os pés juntos e os braços ao lado do corpo.",
                "Salte abrindo as pernas e levando os braços acima da cabeça.",
                "Salte de novo para voltar à posição inicial.",
                "Mantenha um ritmo constante e aterrisse suave, sobre a ponta dos pés."
            ),
            mistakes = listOf("Aterrissar com as pernas rígidas.", "Encolher os ombros ao subir os braços.")
        ),
        Exercise(
            id = "joelho-alto", name = "Elevação de joelhos", anim = "highknee",
            muscles = listOf(Muscle.CARDIO, Muscle.ABDOMEN, Muscle.QUADRICEPS),
            level = Level.INICIANTE, phaseA = "Joelho direito", phaseB = "Joelho esquerdo", cycleMs = 550,
            steps = listOf(
                "Em pé, tronco ereto e abdômen firme.",
                "Suba um joelho até a altura do quadril.",
                "Alterne rápido as pernas, balançando os braços em oposição.",
                "Mantenha o apoio na ponta dos pés."
            ),
            mistakes = listOf("Inclinar o tronco para trás.", "Joelhos subindo pouco.")
        ),
        Exercise(
            id = "flexao", name = "Flexão de braços", anim = "pushup",
            muscles = listOf(Muscle.PEITO, Muscle.TRICEPS, Muscle.OMBROS),
            level = Level.MEDIO, phaseA = "Em cima", phaseB = "Embaixo",
            steps = listOf(
                "Mãos um pouco mais abertas que os ombros, corpo em linha reta da cabeça aos pés.",
                "Contraia o abdômen e os glúteos.",
                "Desça flexionando os cotovelos até o peito quase tocar o chão.",
                "Empurre o chão para subir, sem travar os cotovelos."
            ),
            mistakes = listOf("Quadril caindo ou subindo demais.", "Cotovelos muito abertos, na altura dos ombros.")
        ),
        Exercise(
            id = "flexao-joelhos", name = "Flexão com joelhos apoiados", anim = "kneepush",
            muscles = listOf(Muscle.PEITO, Muscle.TRICEPS, Muscle.OMBROS),
            level = Level.INICIANTE, phaseA = "Em cima", phaseB = "Embaixo",
            steps = listOf(
                "Apoie os joelhos no chão e as mãos um pouco mais abertas que os ombros.",
                "Mantenha a linha reta dos joelhos até a cabeça.",
                "Desça até o peito quase tocar o chão.",
                "Suba empurrando o chão com as mãos."
            ),
            mistakes = listOf("Empinar o quadril.", "Descer só um pouco, sem amplitude.")
        ),
        Exercise(
            id = "flexao-larga", name = "Flexão com mãos afastadas", anim = "pushup",
            muscles = listOf(Muscle.PEITO, Muscle.OMBROS),
            level = Level.MEDIO, phaseA = "Em cima", phaseB = "Embaixo",
            steps = listOf(
                "Afaste as mãos além da largura dos ombros, dedos para a frente.",
                "Corpo em linha reta, abdômen contraído.",
                "Desça com controle até o peito chegar perto do chão.",
                "Suba até estender os braços, sem travar os cotovelos."
            ),
            mistakes = listOf("Mãos tão abertas que incomodam o ombro.", "Perder a linha do corpo.")
        ),
        Exercise(
            id = "flexao-diamante", name = "Flexão diamante", anim = "pushup",
            muscles = listOf(Muscle.TRICEPS, Muscle.PEITO),
            level = Level.MEDIO, phaseA = "Em cima", phaseB = "Embaixo",
            steps = listOf(
                "Junte as mãos sob o peito, formando um losango com polegares e indicadores.",
                "Mantenha o corpo em linha reta.",
                "Desça com os cotovelos próximos do corpo.",
                "Suba estendendo os braços."
            ),
            mistakes = listOf("Cotovelos abrindo para os lados.", "Quadril caindo.")
        ),
        Exercise(
            id = "prancha", name = "Prancha", anim = "plank",
            muscles = listOf(Muscle.ABDOMEN, Muscle.OMBROS, Muscle.LOMBAR),
            level = Level.INICIANTE, phaseA = "Posição firme", phaseB = "Abdômen contraído", cycleMs = 1800,
            steps = listOf(
                "Apoie os antebraços no chão, com os cotovelos sob os ombros.",
                "Estique as pernas e apoie a ponta dos pés.",
                "Contraia abdômen e glúteos, mantendo o corpo em linha reta.",
                "Respire normalmente e olhe para o chão."
            ),
            mistakes = listOf("Quadril caindo, o que sobrecarrega a lombar.", "Prender a respiração.")
        ),
        Exercise(
            id = "prancha-lateral", name = "Prancha lateral (cada lado)", anim = "sideplank",
            muscles = listOf(Muscle.ABDOMEN, Muscle.OMBROS, Muscle.LOMBAR),
            level = Level.INICIANTE, phaseA = "Quadril baixo", phaseB = "Quadril alto", cycleMs = 1800,
            steps = listOf(
                "Deite de lado, com o cotovelo sob o ombro e as pernas esticadas.",
                "Eleve o quadril até o corpo formar uma linha reta.",
                "Mantenha a posição, com o abdômen firme.",
                "Repita do outro lado."
            ),
            mistakes = listOf("Quadril caindo.", "Girar o tronco para a frente ou para trás.")
        ),
        Exercise(
            id = "escalador", name = "Escalador", anim = "climber",
            muscles = listOf(Muscle.CARDIO, Muscle.ABDOMEN, Muscle.OMBROS),
            level = Level.MEDIO, phaseA = "Joelho direito", phaseB = "Joelho esquerdo", cycleMs = 600,
            steps = listOf(
                "Posição de flexão, mãos sob os ombros.",
                "Traga um joelho em direção ao peito.",
                "Troque as pernas em saltos rápidos, como se corresse no lugar.",
                "Mantenha o quadril baixo e o abdômen firme."
            ),
            mistakes = listOf("Quadril subindo demais.", "Ombros saindo da frente das mãos.")
        ),
        Exercise(
            id = "toque-ombro", name = "Prancha com toque no ombro", anim = "shouldertap",
            muscles = listOf(Muscle.ABDOMEN, Muscle.OMBROS),
            level = Level.MEDIO, phaseA = "Apoiado", phaseB = "Mão no ombro", cycleMs = 1100,
            steps = listOf(
                "Posição de flexão, com os pés um pouco afastados para dar estabilidade.",
                "Leve uma mão até o ombro oposto sem girar o quadril.",
                "Volte a mão ao chão e alterne o lado.",
                "Mantenha o abdômen contraído o tempo todo."
            ),
            mistakes = listOf("Balançar o quadril de um lado para o outro.", "Pressa demais, perdendo o controle.")
        ),
        Exercise(
            id = "superman", name = "Superman", anim = "superman",
            muscles = listOf(Muscle.LOMBAR, Muscle.GLUTEOS, Muscle.COSTAS),
            level = Level.INICIANTE, phaseA = "Deitado", phaseB = "Elevado",
            steps = listOf(
                "Deite de bruços com os braços esticados à frente.",
                "Eleve braços, peito e pernas ao mesmo tempo.",
                "Segure 1 segundo no alto, contraindo as costas e os glúteos.",
                "Volte devagar."
            ),
            mistakes = listOf("Forçar o pescoço olhando para cima.", "Subir rápido demais.")
        ),
        Exercise(
            id = "superman-y", name = "Superman em Y", anim = "superman",
            muscles = listOf(Muscle.COSTAS, Muscle.OMBROS, Muscle.LOMBAR),
            level = Level.INICIANTE, phaseA = "Deitado", phaseB = "Elevado",
            steps = listOf(
                "Deite de bruços com os braços abertos à frente, formando um Y.",
                "Eleve os braços e o peito, apertando as costas.",
                "Mantenha as pernas no chão e o olhar para baixo.",
                "Desça com controle."
            ),
            mistakes = listOf("Encolher os ombros em direção às orelhas.", "Levantar a cabeça demais.")
        ),
        Exercise(
            id = "mergulho", name = "Mergulho na cadeira", anim = "dip",
            muscles = listOf(Muscle.TRICEPS, Muscle.OMBROS, Muscle.PEITO),
            level = Level.MEDIO, phaseA = "Em cima", phaseB = "Embaixo",
            equipment = "Cadeira firme, que não escorregue",
            steps = listOf(
                "Sente na beirada da cadeira e apoie as mãos ao lado do quadril.",
                "Deslize o quadril para a frente, com as pernas esticadas ou dobradas.",
                "Desça flexionando os cotovelos para trás, até cerca de 90°.",
                "Suba estendendo os braços."
            ),
            mistakes = listOf("Quadril muito longe da cadeira.", "Ombros subindo em direção às orelhas.")
        ),
        Exercise(
            id = "pike", name = "Flexão pike", anim = "pike",
            muscles = listOf(Muscle.OMBROS, Muscle.TRICEPS),
            level = Level.MEDIO, phaseA = "Em cima", phaseB = "Embaixo",
            steps = listOf(
                "Mãos e pés no chão, quadril alto, formando um V invertido.",
                "Flexione os cotovelos levando a cabeça em direção ao chão, entre as mãos.",
                "Mantenha o quadril alto durante todo o movimento.",
                "Empurre o chão para voltar."
            ),
            mistakes = listOf("Quadril caindo, o que vira uma flexão comum.", "Cotovelos muito abertos.")
        ),
        Exercise(
            id = "abdominal", name = "Abdominal", anim = "crunch",
            muscles = listOf(Muscle.ABDOMEN),
            level = Level.INICIANTE, phaseA = "Deitado", phaseB = "Contraído",
            steps = listOf(
                "Deite de costas com os joelhos dobrados e os pés no chão.",
                "Leve as mãos atrás da cabeça, sem entrelaçar os dedos com força.",
                "Suba o tronco contraindo o abdômen, sem puxar o pescoço.",
                "Desça devagar, sem relaxar totalmente."
            ),
            mistakes = listOf("Puxar o pescoço com as mãos.", "Usar impulso em vez do abdômen.")
        ),
        Exercise(
            id = "elevacao-pernas", name = "Elevação de pernas", anim = "legraise",
            muscles = listOf(Muscle.ABDOMEN),
            level = Level.MEDIO, phaseA = "Pernas no alto", phaseB = "Pernas baixas",
            steps = listOf(
                "Deite de costas com os braços ao lado do corpo.",
                "Com as pernas esticadas, suba até ficarem na vertical.",
                "Desça devagar, parando antes de tocar o chão.",
                "Mantenha a lombar colada no chão."
            ),
            mistakes = listOf("Lombar saindo do chão.", "Descer rápido, sem controle.")
        ),
        Exercise(
            id = "ponte", name = "Ponte de glúteo", anim = "bridge",
            muscles = listOf(Muscle.GLUTEOS, Muscle.POSTERIOR, Muscle.LOMBAR),
            level = Level.INICIANTE, phaseA = "Deitado", phaseB = "Quadril alto",
            steps = listOf(
                "Deite de costas com os joelhos dobrados e os pés no chão, na largura do quadril.",
                "Eleve o quadril até ombros, quadril e joelhos formarem uma linha.",
                "Aperte os glúteos por 1 segundo no alto.",
                "Desça devagar."
            ),
            mistakes = listOf("Arquear demais a lombar.", "Empurrar com a ponta dos pés em vez dos calcanhares.")
        ),
        Exercise(
            id = "panturrilha", name = "Panturrilha em pé", anim = "calf",
            muscles = listOf(Muscle.PANTURRILHA),
            level = Level.INICIANTE, phaseA = "Pés no chão", phaseB = "Na ponta dos pés",
            steps = listOf(
                "Em pé, com os pés na largura do quadril.",
                "Suba na ponta dos pés o mais alto possível.",
                "Segure 1 segundo no alto.",
                "Desça devagar até apoiar os calcanhares."
            ),
            mistakes = listOf("Balançar o corpo para ganhar impulso.", "Amplitude curta.")
        ),
        Exercise(
            id = "parede", name = "Agachamento na parede", anim = "wallsit",
            muscles = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS),
            level = Level.INICIANTE, phaseA = "Em pé", phaseB = "Sentado", cycleMs = 1900,
            equipment = "Parede lisa",
            steps = listOf(
                "Encoste as costas na parede, com os pés um passo à frente.",
                "Deslize para baixo até os joelhos ficarem em 90°.",
                "Mantenha as costas coladas e o peso nos calcanhares.",
                "Segure o tempo indicado."
            ),
            mistakes = listOf("Joelhos passando muito da ponta dos pés.", "Apoiar as mãos nas coxas.")
        ),
        Exercise(
            id = "remada", name = "Remada invertida na mesa", anim = "row",
            muscles = listOf(Muscle.COSTAS, Muscle.BICEPS, Muscle.OMBROS),
            level = Level.MEDIO, phaseA = "Braços esticados", phaseB = "Peito na mesa", cycleMs = 1500,
            equipment = "Mesa bem firme e pesada",
            steps = listOf(
                "Deite sob a mesa e segure a borda com as mãos, braços esticados.",
                "Com o corpo reto, puxe o peito em direção à borda.",
                "Junte as escápulas no alto.",
                "Desça devagar. Teste antes se a mesa aguenta seu peso."
            ),
            mistakes = listOf("Quadril caindo.", "Puxar só com os braços, sem juntar as costas.")
        ),
        Exercise(
            id = "prancha-invertida", name = "Prancha invertida", anim = "revplank",
            muscles = listOf(Muscle.GLUTEOS, Muscle.POSTERIOR, Muscle.LOMBAR, Muscle.OMBROS),
            level = Level.MEDIO, phaseA = "Quadril baixo", phaseB = "Quadril alto", cycleMs = 1700,
            steps = listOf(
                "Sente com as pernas esticadas e as mãos no chão, atrás do quadril.",
                "Eleve o quadril até o corpo formar uma linha reta.",
                "Olhe para a frente e aperte os glúteos.",
                "Segure o tempo indicado."
            ),
            mistakes = listOf("Quadril caindo.", "Ombros afundando em direção às orelhas.")
        ),
        Exercise(
            id = "flexao-pes-elevados", name = "Flexão com pés elevados", anim = "pushup_feet",
            muscles = listOf(Muscle.PEITO, Muscle.OMBROS, Muscle.TRICEPS),
            level = Level.AVANCADO, phaseA = "Em cima", phaseB = "Embaixo",
            equipment = "Cadeira ou degrau firme",
            steps = listOf(
                "Apoie os pés na cadeira e as mãos no chão, um pouco mais abertas que os ombros.",
                "Corpo em linha reta, abdômen e glúteos contraídos.",
                "Desça até o peito quase tocar o chão.",
                "Empurre o chão para subir, sem travar os cotovelos."
            ),
            mistakes = listOf("Quadril caindo.", "Cabeça caindo para a frente.")
        ),
        Exercise(
            id = "agachamento-salto", name = "Agachamento com salto", anim = "jumpsquat",
            muscles = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS, Muscle.CARDIO),
            level = Level.AVANCADO, phaseA = "Embaixo", phaseB = "No ar", cycleMs = 700,
            steps = listOf(
                "Agache como no agachamento comum, com o peito aberto.",
                "Salte para cima com força, balançando os braços.",
                "Aterrisse suave, amortecendo com os joelhos.",
                "Volte direto para o próximo agachamento."
            ),
            mistakes = listOf("Joelhos para dentro na aterrissagem.", "Aterrissar com as pernas rígidas.")
        ),
        Exercise(
            id = "pike-elevado", name = "Flexão pike com pés elevados", anim = "pike_feet",
            muscles = listOf(Muscle.OMBROS, Muscle.TRICEPS),
            level = Level.AVANCADO, phaseA = "Em cima", phaseB = "Embaixo",
            equipment = "Cadeira ou degrau firme",
            steps = listOf(
                "Apoie os pés na cadeira e as mãos no chão, com o quadril bem alto.",
                "Flexione os cotovelos levando a cabeça em direção ao chão, entre as mãos.",
                "Mantenha o quadril alto durante todo o movimento.",
                "Empurre o chão para voltar."
            ),
            mistakes = listOf("Quadril caindo.", "Cotovelos muito abertos.")
        ),
        Exercise(
            id = "ponte-unilateral", name = "Ponte com uma perna (cada lado)", anim = "bridge1",
            muscles = listOf(Muscle.GLUTEOS, Muscle.POSTERIOR, Muscle.LOMBAR),
            level = Level.MEDIO, phaseA = "Deitado", phaseB = "Quadril alto",
            steps = listOf(
                "Deite de costas, com uma perna dobrada e a outra esticada no ar.",
                "Eleve o quadril empurrando o calcanhar do pé apoiado.",
                "Mantenha o quadril alinhado, sem girar para o lado.",
                "Desça devagar e troque de perna ao terminar."
            ),
            mistakes = listOf("Quadril girando.", "Arquear a lombar para subir mais.")
        ),
        Exercise(
            id = "bicicleta", name = "Abdominal bicicleta", anim = "bicycle",
            muscles = listOf(Muscle.ABDOMEN),
            level = Level.MEDIO, phaseA = "Joelho direito", phaseB = "Joelho esquerdo", cycleMs = 900,
            steps = listOf(
                "Deite de costas, com as mãos atrás da cabeça e os ombros levemente elevados.",
                "Traga um joelho em direção ao peito e estique a outra perna.",
                "Troque de lado como se pedalasse, girando o tronco.",
                "Mantenha o ritmo controlado, sem puxar o pescoço."
            ),
            mistakes = listOf("Puxar o pescoço com as mãos.", "Ir rápido demais, sem girar o tronco.")
        ),
        Exercise(
            id = "tesoura", name = "Tesoura (pernas alternadas)", anim = "flutter",
            muscles = listOf(Muscle.ABDOMEN),
            level = Level.INICIANTE, phaseA = "Perna direita", phaseB = "Perna esquerda", cycleMs = 700,
            steps = listOf(
                "Deite de costas, com as mãos ao lado do corpo.",
                "Eleve as duas pernas esticadas a poucos centímetros do chão.",
                "Alterne as pernas para cima e para baixo, em movimentos curtos.",
                "Mantenha a lombar colada no chão."
            ),
            mistakes = listOf("Lombar saindo do chão.", "Pernas muito baixas, sem controle.")
        ),
        Exercise(
            id = "remada-supinada", name = "Remada invertida pegada supinada", anim = "row",
            muscles = listOf(Muscle.BICEPS, Muscle.COSTAS),
            level = Level.MEDIO, phaseA = "Braços esticados", phaseB = "Peito na mesa", cycleMs = 1500,
            equipment = "Mesa bem firme e pesada",
            steps = listOf(
                "Deite sob a mesa e segure a borda com as palmas das mãos viradas para você.",
                "Com o corpo reto, puxe o peito em direção à borda, levando os cotovelos junto ao corpo.",
                "Sinta o bíceps trabalhar no alto e segure 1 segundo.",
                "Desça devagar. Teste antes se a mesa aguenta seu peso."
            ),
            mistakes = listOf("Quadril caindo.", "Abrir os cotovelos para os lados.")
        )
    )

    private val exerciseById = exercises.associateBy { it.id }

    fun exercise(id: String): Exercise = exerciseById.getValue(id)

    val plans: List<Plan> = listOf(
        Plan(
            id = "rotina-rapida", name = "Rotina rápida", tagline = "Ative o corpo em cerca de 15 minutos",
            level = Level.INICIANTE, icon = PlanIcon.FLAME,
            focus = listOf(Muscle.CARDIO, Muscle.PEITO, Muscle.QUADRICEPS, Muscle.ABDOMEN),
            items = listOf(
                PlanItem("polichinelo", sets = 2, rest = 20, seconds = 40),
                PlanItem("agachamento", sets = 3, rest = 20, reps = 15),
                PlanItem("flexao", sets = 3, rest = 30, reps = 10),
                PlanItem("afundo", sets = 2, rest = 20, reps = 10),
                PlanItem("escalador", sets = 2, rest = 20, seconds = 30),
                PlanItem("prancha", sets = 2, rest = 20, seconds = 40),
                PlanItem("prancha-lateral", sets = 2, rest = 15, seconds = 30)
            )
        ),
        Plan(
            id = "corpo-todo", name = "Corpo todo", tagline = "Treino completo, sem equipamento",
            level = Level.INICIANTE, icon = PlanIcon.DUMBBELL,
            focus = listOf(Muscle.QUADRICEPS, Muscle.PEITO, Muscle.LOMBAR, Muscle.ABDOMEN),
            items = listOf(
                PlanItem("agachamento", sets = 3, rest = 45, reps = 15),
                PlanItem("flexao", sets = 3, rest = 45, reps = 10),
                PlanItem("afundo", sets = 3, rest = 45, reps = 10),
                PlanItem("superman", sets = 3, rest = 30, reps = 12),
                PlanItem("polichinelo", sets = 3, rest = 30, seconds = 30),
                PlanItem("prancha", sets = 3, rest = 30, seconds = 30)
            )
        ),
        Plan(
            id = "peito-triceps", name = "Peito e tríceps", tagline = "Parte superior do corpo",
            level = Level.MEDIO, icon = PlanIcon.DUMBBELL,
            focus = listOf(Muscle.PEITO, Muscle.TRICEPS, Muscle.OMBROS),
            items = listOf(
                PlanItem("flexao", sets = 4, rest = 60, reps = 12),
                PlanItem("flexao-larga", sets = 3, rest = 60, reps = 10),
                PlanItem("flexao-diamante", sets = 3, rest = 60, reps = 8),
                PlanItem("mergulho", sets = 3, rest = 60, reps = 10),
                PlanItem("toque-ombro", sets = 3, rest = 45, reps = 20)
            )
        ),
        Plan(
            id = "costas-postura", name = "Costas e postura", tagline = "Costas, lombar e postura",
            level = Level.MEDIO, icon = PlanIcon.SHIELD,
            focus = listOf(Muscle.COSTAS, Muscle.LOMBAR, Muscle.BICEPS, Muscle.GLUTEOS),
            items = listOf(
                PlanItem("superman", sets = 4, rest = 45, reps = 12),
                PlanItem("superman-y", sets = 3, rest = 45, reps = 12),
                PlanItem("remada", sets = 4, rest = 60, reps = 10),
                PlanItem("ponte", sets = 3, rest = 45, reps = 15),
                PlanItem("prancha-invertida", sets = 3, rest = 45, seconds = 20)
            )
        ),
        Plan(
            id = "pernas-gluteos", name = "Pernas e glúteos", tagline = "Parte inferior do corpo",
            level = Level.MEDIO, icon = PlanIcon.PEAK,
            focus = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS, Muscle.POSTERIOR, Muscle.PANTURRILHA),
            items = listOf(
                PlanItem("agachamento", sets = 4, rest = 60, reps = 15),
                PlanItem("afundo", sets = 3, rest = 60, reps = 12),
                PlanItem("bulgaro", sets = 3, rest = 60, reps = 10),
                PlanItem("ponte", sets = 4, rest = 45, reps = 15),
                PlanItem("panturrilha", sets = 4, rest = 30, reps = 20),
                PlanItem("parede", sets = 3, rest = 45, seconds = 40)
            )
        ),
        Plan(
            id = "ombros-abdomen", name = "Ombros e abdômen", tagline = "Ombros e core",
            level = Level.MEDIO, icon = PlanIcon.TARGET,
            focus = listOf(Muscle.OMBROS, Muscle.ABDOMEN, Muscle.LOMBAR),
            items = listOf(
                PlanItem("pike", sets = 4, rest = 60, reps = 8),
                PlanItem("prancha", sets = 3, rest = 45, seconds = 45),
                PlanItem("escalador", sets = 3, rest = 30, seconds = 30),
                PlanItem("abdominal", sets = 3, rest = 30, reps = 20),
                PlanItem("elevacao-pernas", sets = 3, rest = 45, reps = 12),
                PlanItem("prancha-lateral", sets = 3, rest = 30, seconds = 30)
            )
        ),
        Plan(
            id = "hiit-cardio", name = "HIIT cardio", tagline = "Intervalado, alta intensidade",
            level = Level.MEDIO, icon = PlanIcon.BOLT,
            focus = listOf(Muscle.CARDIO, Muscle.QUADRICEPS, Muscle.ABDOMEN),
            items = listOf(
                PlanItem("polichinelo", sets = 4, rest = 20, seconds = 40),
                PlanItem("joelho-alto", sets = 4, rest = 20, seconds = 30),
                PlanItem("escalador", sets = 4, rest = 20, seconds = 30),
                PlanItem("agachamento", sets = 4, rest = 20, reps = 20),
                PlanItem("flexao", sets = 3, rest = 30, reps = 12)
            )
        ),
        Plan(
            id = "bracos", name = "Braços", tagline = "Bíceps, tríceps e ombros",
            level = Level.MEDIO, icon = PlanIcon.DUMBBELL,
            focus = listOf(Muscle.BICEPS, Muscle.TRICEPS, Muscle.OMBROS),
            items = listOf(
                PlanItem("remada-supinada", sets = 4, rest = 60, reps = 10),
                PlanItem("flexao-diamante", sets = 4, rest = 60, reps = 10),
                PlanItem("mergulho", sets = 4, rest = 60, reps = 10),
                PlanItem("superman-y", sets = 3, rest = 45, reps = 12),
                PlanItem("prancha", sets = 3, rest = 30, seconds = 40)
            )
        ),
        Plan(
            id = "ombros-fortes", name = "Ombros fortes", tagline = "Ombros largos e estáveis",
            level = Level.MEDIO, icon = PlanIcon.SHIELD,
            focus = listOf(Muscle.OMBROS, Muscle.TRICEPS, Muscle.COSTAS),
            items = listOf(
                PlanItem("pike", sets = 4, rest = 60, reps = 8),
                PlanItem("flexao-larga", sets = 3, rest = 60, reps = 10),
                PlanItem("toque-ombro", sets = 3, rest = 45, reps = 20),
                PlanItem("superman-y", sets = 3, rest = 45, reps = 12),
                PlanItem("prancha", sets = 3, rest = 30, seconds = 40)
            )
        ),
        Plan(
            id = "core-barriga", name = "Core definido", tagline = "Abdômen e cintura, com cardio",
            level = Level.MEDIO, icon = PlanIcon.TARGET,
            focus = listOf(Muscle.ABDOMEN, Muscle.LOMBAR, Muscle.CARDIO),
            items = listOf(
                PlanItem("bicicleta", sets = 3, rest = 30, reps = 20),
                PlanItem("escalador", sets = 3, rest = 30, seconds = 30),
                PlanItem("elevacao-pernas", sets = 3, rest = 40, reps = 12),
                PlanItem("prancha", sets = 3, rest = 30, seconds = 40),
                PlanItem("prancha-lateral", sets = 3, rest = 30, seconds = 30)
            )
        ),
        Plan(
            id = "combo-queima", name = "Combo queima total", tagline = "HIIT e core em um treino só",
            level = Level.MEDIO, icon = PlanIcon.FLAME, combo = true,
            focus = listOf(Muscle.CARDIO, Muscle.ABDOMEN, Muscle.QUADRICEPS),
            items = listOf(
                PlanItem("polichinelo", sets = 3, rest = 20, seconds = 40),
                PlanItem("joelho-alto", sets = 3, rest = 20, seconds = 30),
                PlanItem("agachamento", sets = 3, rest = 20, reps = 20),
                PlanItem("escalador", sets = 3, rest = 20, seconds = 30),
                PlanItem("bicicleta", sets = 3, rest = 20, reps = 20),
                PlanItem("prancha", sets = 3, rest = 20, seconds = 40)
            )
        ),
        Plan(
            id = "combo-bracos-ombros", name = "Combo braços e ombros", tagline = "Parte superior com foco em braços",
            level = Level.MEDIO, icon = PlanIcon.DUMBBELL, combo = true,
            focus = listOf(Muscle.BICEPS, Muscle.TRICEPS, Muscle.OMBROS),
            items = listOf(
                PlanItem("flexao-diamante", sets = 3, rest = 45, reps = 10),
                PlanItem("mergulho", sets = 3, rest = 45, reps = 10),
                PlanItem("pike", sets = 3, rest = 45, reps = 8),
                PlanItem("remada-supinada", sets = 3, rest = 45, reps = 10),
                PlanItem("superman-y", sets = 3, rest = 30, reps = 12)
            )
        ),
        Plan(
            id = "combo-pernas", name = "Combo pernas explosivas", tagline = "Pernas e glúteos em um treino só",
            level = Level.MEDIO, icon = PlanIcon.PEAK, combo = true,
            focus = listOf(Muscle.QUADRICEPS, Muscle.GLUTEOS, Muscle.PANTURRILHA),
            items = listOf(
                PlanItem("agachamento", sets = 3, rest = 30, reps = 20),
                PlanItem("afundo", sets = 3, rest = 30, reps = 12),
                PlanItem("ponte", sets = 3, rest = 30, reps = 15),
                PlanItem("panturrilha", sets = 3, rest = 30, reps = 20),
                PlanItem("parede", sets = 3, rest = 30, seconds = 40)
            )
        ),
        Plan(
            id = "combo-superior", name = "Combo parte superior", tagline = "Peito, costas e ombros juntos",
            level = Level.MEDIO, icon = PlanIcon.SHIELD, combo = true,
            focus = listOf(Muscle.PEITO, Muscle.COSTAS, Muscle.OMBROS),
            items = listOf(
                PlanItem("flexao", sets = 3, rest = 45, reps = 12),
                PlanItem("remada", sets = 3, rest = 45, reps = 10),
                PlanItem("pike", sets = 3, rest = 45, reps = 8),
                PlanItem("mergulho", sets = 3, rest = 45, reps = 10),
                PlanItem("prancha", sets = 3, rest = 30, seconds = 40)
            )
        )
    )

    private val planById = plans.associateBy { it.id }

    fun plan(id: String): Plan = planById.getValue(id)

    /** Sugestão de treino por dia da semana (ajuste à vontade). */
    fun suggestionFor(day: DayOfWeek): Plan = plan(
        when (day) {
            DayOfWeek.MONDAY -> "peito-triceps"
            DayOfWeek.TUESDAY -> "pernas-gluteos"
            DayOfWeek.WEDNESDAY -> "rotina-rapida"
            DayOfWeek.THURSDAY -> "costas-postura"
            DayOfWeek.FRIDAY -> "ombros-abdomen"
            DayOfWeek.SATURDAY -> "corpo-todo"
            DayOfWeek.SUNDAY -> "hiit-cardio"
        }
    )
}

fun PlanItem.exercise(): Exercise = Catalog.exercise(exerciseId)
