// Service worker: guarda os arquivos no celular para o app abrir offline.
// Ao alterar qualquer arquivo do app, aumente o número em CACHE (v1 -> v2) para atualizar.
const CACHE = 'yourlife-v1';
const ASSETS = [
  './',
  'index.html',
  'css/style.css',
  'js/data.js',
  'js/storage.js',
  'js/app.js',
  'manifest.webmanifest',
  'icons/icon-192.png',
  'icons/icon-512.png'
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)));
  self.skipWaiting();
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
  );
  self.clients.claim();
});

self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET') return;
  e.respondWith(caches.match(e.request).then(hit => hit || fetch(e.request)));
});
