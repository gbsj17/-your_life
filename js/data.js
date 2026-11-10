// Planos de treino. Para editar/adicionar, basta mexer neste arquivo.
// Cada exercício usa "reps" (repetições) OU "time" (segundos).
// sets = séries | rest = descanso (segundos) após cada série.
const PLANS = [
  {
    id: 'corpo-todo',
    name: 'Corpo todo',
    icon: '🔥',
    level: 'Iniciante',
    desc: 'Treino completo em casa, sem equipamento.',
    exercises: [
      { name: 'Agachamento', sets: 3, reps: 15, rest: 45, tip: 'Pés na largura dos ombros, joelhos alinhados com os pés e coluna reta.' },
      { name: 'Flexão de braços', sets: 3, reps: 10, rest: 45, tip: 'Corpo em linha reta. Se pesar, apoie os joelhos no chão.' },
      { name: 'Afundo (cada lado)', sets: 3, reps: 10, rest: 45, tip: 'Dê um passo à frente e desça até os dois joelhos formarem cerca de 90°.' },
      { name: 'Superman', sets: 3, reps: 12, rest: 30, tip: 'Deitado de bruços, eleve braços e pernas juntos e segure 1 segundo.' },
      { name: 'Polichinelo', sets: 3, time: 30, rest: 30, tip: 'Ritmo constante, aterrissando suave nos pés.' },
      { name: 'Prancha', sets: 3, time: 30, rest: 30, tip: 'Cotovelos sob os ombros, abdômen firme, sem deixar o quadril cair.' }
    ]
  },
  {
    id: 'peito-triceps',
    name: 'Peito e tríceps',
    icon: '💪',
    level: 'Iniciante/Intermediário',
    desc: 'Foco na parte superior do corpo, sem equipamento.',
    exercises: [
      { name: 'Flexão de braços', sets: 4, reps: 12, rest: 60, tip: 'Desça até o peito quase tocar o chão e suba sem estender o cotovelo de vez.' },
      { name: 'Flexão com mãos afastadas', sets: 3, reps: 10, rest: 60, tip: 'Mãos mais abertas que os ombros para enfatizar o peito.' },
      { name: 'Flexão diamante', sets: 3, reps: 8, rest: 60, tip: 'Mãos juntas formando um losango; trabalha mais o tríceps.' },
      { name: 'Mergulho na cadeira', sets: 3, reps: 10, rest: 60, tip: 'Use uma cadeira firme. Desça flexionando os cotovelos para trás.' },
      { name: 'Prancha com toque no ombro', sets: 3, reps: 20, rest: 45, tip: 'Alterne as mãos tocando o ombro oposto sem balançar o quadril.' }
    ]
  },
  {
    id: 'costas-biceps',
    name: 'Costas e bíceps',
    icon: '🦍',
    level: 'Iniciante/Intermediário',
    desc: 'Costas e postura. A remada precisa de uma mesa firme.',
    exercises: [
      { name: 'Superman', sets: 4, reps: 12, rest: 45, tip: 'Eleve braços e pernas ao mesmo tempo, sem forçar o pescoço.' },
      { name: 'Superman em Y', sets: 3, reps: 12, rest: 45, tip: 'De bruços, braços abertos em Y; eleve-os apertando as costas.' },
      { name: 'Remada invertida na mesa', sets: 4, reps: 10, rest: 60, tip: 'Deitado sob uma mesa resistente, puxe o peito até a borda. Teste a mesa antes.' },
      { name: 'Ponte de glúteo', sets: 3, reps: 15, rest: 45, tip: 'Eleve o quadril apertando glúteos e lombar sem arquear demais.' },
      { name: 'Prancha invertida', sets: 3, time: 20, rest: 45, tip: 'Sentado com as mãos atrás, eleve o quadril e mantenha o corpo reto.' }
    ]
  },
  {
    id: 'pernas-gluteos',
    name: 'Pernas e glúteos',
    icon: '🦵',
    level: 'Iniciante/Intermediário',
    desc: 'Parte inferior do corpo, sem equipamento.',
    exercises: [
      { name: 'Agachamento', sets: 4, reps: 15, rest: 60, tip: 'Desça controlando e suba empurrando o chão com os calcanhares.' },
      { name: 'Afundo (cada lado)', sets: 3, reps: 12, rest: 60, tip: 'Tronco ereto e joelho da frente alinhado com o pé.' },
      { name: 'Agachamento búlgaro (cada lado)', sets: 3, reps: 10, rest: 60, tip: 'Pé de trás apoiado numa cadeira; desça em linha reta.' },
      { name: 'Elevação de quadril', sets: 4, reps: 15, rest: 45, tip: 'No topo, aperte os glúteos por 1 segundo.' },
      { name: 'Panturrilha em pé', sets: 4, reps: 20, rest: 30, tip: 'Suba na ponta dos pés e desça devagar.' },
      { name: 'Agachamento na parede', sets: 3, time: 40, rest: 45, tip: 'Costas na parede, coxas paralelas ao chão.' }
    ]
  },
  {
    id: 'ombros-abdomen',
    name: 'Ombros e abdômen',
    icon: '🎯',
    level: 'Iniciante/Intermediário',
    desc: 'Ombros e core, sem equipamento.',
    exercises: [
      { name: 'Flexão pike', sets: 4, reps: 8, rest: 60, tip: 'Quadril alto formando um V invertido; desça a cabeça entre as mãos.' },
      { name: 'Prancha', sets: 3, time: 45, rest: 45, tip: 'Abdômen e glúteos contraídos, corpo em linha reta.' },
      { name: 'Escalador', sets: 3, time: 30, rest: 30, tip: 'Em posição de flexão, traga os joelhos ao peito alternadamente.' },
      { name: 'Abdominal', sets: 3, reps: 20, rest: 30, tip: 'Suba o tronco sem puxar o pescoço com as mãos.' },
      { name: 'Elevação de pernas', sets: 3, reps: 12, rest: 45, tip: 'Deitado, eleve as pernas retas mantendo a lombar no chão.' },
      { name: 'Prancha lateral (cada lado)', sets: 3, time: 30, rest: 30, tip: 'Apoie o antebraço e mantenha o quadril elevado.' }
    ]
  }
];
