(() => {
  const $ = s => document.querySelector(s);
  const app = $('#app');
  const title = $('#title');
  const backBtn = $('#back');
  const tabs = document.querySelectorAll('.tabs button');

  let view = 'plans';       // plans | detail | workout | progress
  let currentPlanId = null; // plano aberto na tela de detalhes
  let session = null;       // treino em andamento

  // ---------- utilitários ----------
  const pad = n => String(n).padStart(2, '0');
  const fmt = s => `${pad(Math.floor(s / 60))}:${pad(s % 60)}`;
  const dayKey = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const planById = id => PLANS.find(p => p.id === id);
  // Estimativa: 40s por série de repetições; séries por tempo usam o próprio tempo.
  const estMin = p => Math.round(p.exercises.reduce((t, e) => t + e.sets * ((e.time || 40) + e.rest), 0) / 60);
  const target = e => (e.time ? `${e.time}s` : `${e.reps} reps`);

  // ---------- navegação ----------
  function show(v, arg) {
    view = v;
    document.body.classList.toggle('in-workout', v === 'workout');
    backBtn.hidden = !(v === 'detail' || v === 'workout');
    tabs.forEach(t => t.classList.toggle('active', t.dataset.view === (v === 'progress' ? 'progress' : 'plans')));
    if (v === 'plans') renderPlans();
    else if (v === 'detail') renderDetail(arg);
    else if (v === 'workout') renderWorkout();
    else if (v === 'progress') renderProgress();
    window.scrollTo(0, 0);
  }

  tabs.forEach(t => t.addEventListener('click', () => show(t.dataset.view)));

  backBtn.addEventListener('click', () => {
    if (view === 'workout') {
      if (confirm('Sair do treino? O progresso desta sessão será perdido.')) {
        session = null;
        show('detail', currentPlanId);
      }
    } else {
      show('plans');
    }
  });

  // ---------- telas ----------
  function renderPlans() {
    title.textContent = 'Treinos';
    app.innerHTML = PLANS.map(p => `
      <button class="card" data-act="open" data-id="${p.id}">
        <span class="ico">${p.icon}</span>
        <span class="info">
          <strong>${p.name}</strong>
          <small>${p.exercises.length} exercícios · ~${estMin(p)} min · ${p.level}</small>
          <small>${p.desc}</small>
        </span>
      </button>`).join('');
  }

  function renderDetail(id) {
    currentPlanId = id;
    const p = planById(id);
    title.textContent = p.name;
    app.innerHTML = `
      <p class="muted">${p.desc} Duração estimada: ~${estMin(p)} min.</p>
      <ol class="list">
        ${p.exercises.map(e => `
          <li>
            <strong>${e.name}</strong>
            <span>${e.sets} × ${target(e)} · descanso ${e.rest}s</span>
            <small>${e.tip}</small>
          </li>`).join('')}
      </ol>
      <button class="btn" data-act="start" data-id="${p.id}">Iniciar treino</button>`;
  }

  function renderWorkout() {
    const s = session;
    const p = s.plan;
    const e = p.exercises[s.ex];
    title.textContent = p.name;
    const progress = `<div class="bar"><i style="width:${(s.ex / p.exercises.length) * 100}%"></i></div>`;

    if (s.phase === 'rest') {
      app.innerHTML = `${progress}
        <div class="center">
          <p class="muted">Descanso</p>
          <div class="count" id="count">${fmt(Math.max(0, Math.ceil((s.endAt - Date.now()) / 1000)))}</div>
          <p class="muted">Próximo: <strong>${e.name}</strong> · série ${s.set}/${e.sets}</p>
          <div class="row">
            <button class="btn ghost" data-act="add">+15s</button>
            <button class="btn" data-act="skip">Pular</button>
          </div>
        </div>`;
      return;
    }

    let action;
    if (!e.time) {
      action = `<div class="goal">${e.reps} repetições</div>
        <button class="btn" data-act="done">Concluir série</button>`;
    } else if (!s.running) {
      action = `<div class="goal">${e.time}s</div>
        <button class="btn" data-act="go">Iniciar</button>`;
    } else {
      action = `<div class="count" id="count">${fmt(Math.max(0, Math.ceil((s.endAt - Date.now()) / 1000)))}</div>
        <button class="btn ghost" data-act="done">Concluir agora</button>`;
    }

    app.innerHTML = `${progress}
      <div class="center">
        <p class="muted">Exercício ${s.ex + 1}/${p.exercises.length}</p>
        <h2>${e.name}</h2>
        <p class="muted">Série ${s.set}/${e.sets}</p>
        ${action}
        <p class="tip">${e.tip}</p>
      </div>`;
  }

  function renderProgress() {
    title.textContent = 'Progresso';
    const h = Store.all();
    const days = new Set(h.map(x => x.date));

    // sequência: dias seguidos com treino (hoje ainda pode estar em aberto)
    let streak = 0;
    const d = new Date();
    if (!days.has(dayKey(d))) d.setDate(d.getDate() - 1);
    while (days.has(dayKey(d))) { streak++; d.setDate(d.getDate() - 1); }

    // últimos 7 dias
    const names = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];
    const week = [];
    for (let i = 6; i >= 0; i--) {
      const x = new Date();
      x.setDate(x.getDate() - i);
      const k = dayKey(x);
      week.push({ label: names[x.getDay()], n: h.filter(y => y.date === k).length });
    }
    const weekTotal = week.reduce((t, w) => t + w.n, 0);
    const mins = Math.round(h.reduce((t, x) => t + x.seconds, 0) / 60);

    const recent = h.slice(-20).reverse().map(x => `
      <li>
        <strong>${esc(x.name)}</strong>
        <span>${new Date(x.ts).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' })} · ${fmt(x.seconds)}</span>
      </li>`).join('');

    app.innerHTML = `
      <div class="stats">
        <div><b>${h.length}</b><small>treinos</small></div>
        <div><b>${weekTotal}</b><small>nos últimos 7 dias</small></div>
        <div><b>${streak}</b><small>dias seguidos</small></div>
        <div><b>${mins}</b><small>minutos</small></div>
      </div>
      <div class="week">
        ${week.map(w => `
          <div class="day">
            <div class="col"><i class="${w.n ? 'on' : ''}" style="height:${w.n ? Math.min(100, w.n * 40) : 6}%"></i></div>
            <span>${w.label}</span>
          </div>`).join('')}
      </div>
      ${h.length ? `<h3>Histórico</h3><ul class="list">${recent}</ul>
        <button class="btn ghost danger" data-act="clear">Apagar histórico</button>`
        : '<p class="muted center">Nenhum treino ainda. Conclua o primeiro para ver seu progresso.</p>'}`;
  }

  // ---------- treino ----------
  function startWorkout(id) {
    session = { plan: planById(id), ex: 0, set: 1, phase: 'work', running: false, endAt: 0, startedAt: Date.now() };
    show('workout');
  }

  function startTimed() {
    const s = session;
    s.running = true;
    s.endAt = Date.now() + s.plan.exercises[s.ex].time * 1000;
    renderWorkout();
  }

  function completeSet() {
    const s = session;
    const p = s.plan;
    const e = p.exercises[s.ex];
    s.running = false;
    if (s.set < e.sets) s.set++;
    else if (s.ex < p.exercises.length - 1) { s.ex++; s.set = 1; }
    else return finishWorkout();
    s.phase = 'rest';
    s.endAt = Date.now() + e.rest * 1000; // descanso do exercício que acabou de ser feito
    renderWorkout();
  }

  function skipRest() {
    session.phase = 'work';
    renderWorkout();
  }

  function finishWorkout() {
    const s = session;
    const seconds = Math.round((Date.now() - s.startedAt) / 1000);
    Store.add({ date: dayKey(new Date()), ts: Date.now(), planId: s.plan.id, name: s.plan.name, seconds });
    s.phase = 'done';
    backBtn.hidden = true;
    app.innerHTML = `
      <div class="center">
        <div class="trophy">🎉</div>
        <h2>Treino concluído!</h2>
        <p class="muted">${esc(s.plan.name)} · ${fmt(seconds)}</p>
        <button class="btn" data-act="finish">Ver progresso</button>
      </div>`;
  }

  // Atualiza só o número do cronômetro (a cada 250ms) usando o horário de término,
  // assim a contagem continua correta mesmo se a tela apagar por um tempo.
  function tick() {
    const s = session;
    if (!s) return;
    const counting = s.phase === 'rest' || (s.phase === 'work' && s.running);
    if (!counting) return;
    const left = Math.max(0, Math.ceil((s.endAt - Date.now()) / 1000));
    const c = $('#count');
    if (c) c.textContent = fmt(left);
    if (left <= 0) {
      if (s.phase === 'rest') { s.phase = 'work'; renderWorkout(); }
      else completeSet();
    }
  }
  setInterval(tick, 250);

  // ---------- cliques (um único listener para todos os botões) ----------
  app.addEventListener('click', e => {
    const el = e.target.closest('[data-act]');
    if (!el) return;
    const id = el.dataset.id;
    const actions = {
      open: () => show('detail', id),
      start: () => startWorkout(id),
      go: startTimed,
      done: completeSet,
      skip: skipRest,
      add: () => { session.endAt += 15000; tick(); },
      finish: () => { session = null; show('progress'); },
      clear: () => { if (confirm('Apagar todo o histórico?')) { Store.clear(); renderProgress(); } }
    };
    if (actions[el.dataset.act]) actions[el.dataset.act]();
  });

  // ---------- início ----------
  if ('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js');
  show('plans');
})();
