// Histórico salvo no próprio celular (localStorage). Não usa servidor nem login.
const Store = {
  key: 'yourlife.history.v1',

  all() {
    try { return JSON.parse(localStorage.getItem(this.key)) || []; }
    catch { return []; }
  },

  add(session) {
    const list = this.all();
    list.push(session);
    localStorage.setItem(this.key, JSON.stringify(list));
  },

  clear() {
    localStorage.removeItem(this.key);
  }
};
