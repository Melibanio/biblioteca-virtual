// Contrato esperado do backend (prefixo /api): GET /livros | POST /usuarios | POST /login
// GET /favoritos | POST /favoritos {livroId} | DELETE /favoritos/{livroId}   (autenticados via "Authorization: Bearer <token>")
const API = location.port === '8080' ? '' : 'http://localhost:8080';
const sessao = {
  get token() { return localStorage.getItem('token'); },
  get usuario() { try { return JSON.parse(localStorage.getItem('usuario')); } catch { return null; } },
  salvar(token, usuario) { localStorage.setItem('token', token); localStorage.setItem('usuario', JSON.stringify(usuario)); },
  sair() { localStorage.removeItem('token'); localStorage.removeItem('usuario'); }
};
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const norm = s => String(s ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

async function api(caminho, { metodo = 'GET', corpo } = {}) {
  const cab = { 'Content-Type': 'application/json' };
  if (sessao.token) cab.Authorization = 'Bearer ' + sessao.token;
  let r;
  try { r = await fetch(API + '/api' + caminho, { method: metodo, headers: cab, body: corpo ? JSON.stringify(corpo) : undefined }); }
  catch { throw new Error('Não foi possível conectar ao servidor. Verifique se o backend está em execução.'); }
  const d = await r.json().catch(() => ({}));
  if (!r.ok) {
    if (r.status === 401 && sessao.token) sessao.sair();
    throw new Error(d.mensagem || d.erro || 'Erro ' + r.status + ' ao falar com o servidor.');
  }
  return d;
}

function mensagem(el, texto, tipo) { el.textContent = texto; el.className = 'msg ' + tipo; el.focus?.(); }

// Valida campos obrigatórios/formato e devolve a primeira mensagem de erro (ou '')
function validar(form) {
  for (const el of form.elements) {
    if (!el.name) continue;
    el.removeAttribute('aria-invalid');
    if (el.checkValidity()) continue;
    el.setAttribute('aria-invalid', 'true'); el.focus();
    const nome = form.querySelector(`label[for=${el.id}]`)?.textContent || el.name;
    if (el.validity.valueMissing) return `Preencha o campo "${nome}".`;
    if (el.validity.typeMismatch) return 'Digite um e-mail válido.';
    return el.validationMessage;
  }
  return '';
}

// ---- Favoritos e cards (usados no catálogo, nos detalhes e na página de favoritos)
let favoritos = new Set();
async function carregarFavoritos() {
  if (!sessao.token) return [];
  const lista = await api('/favoritos');
  favoritos = new Set(lista.map(f => (f.livro || f).id ?? f.livroId));
  return lista.map(f => f.livro || f);
}
async function alternarFavorito(id, aviso) {
  if (!sessao.token) { location.href = 'login.html?voltar=' + encodeURIComponent(location.pathname.split('/').pop() + location.search); return false; }
  const jaFavorito = favoritos.has(id);
  try {
    if (jaFavorito) await api('/favoritos/' + id, { metodo: 'DELETE' });
    else await api('/favoritos', { metodo: 'POST', corpo: { livroId: id } });
    jaFavorito ? favoritos.delete(id) : favoritos.add(id);
    return true;
  } catch (e) { if (aviso) mensagem(aviso, e.message, 'erro'); return false; }
}
const capaHtml = l => `<div class="capa c${l.id % 6}">${l.capa ? `<img src="${esc(l.capa)}" alt="Capa de ${esc(l.titulo)}" onerror="this.replaceWith(document.createTextNode(this.alt.slice(8)))">` : esc(l.titulo)}</div>`;
const coracaoHtml = id => { const f = favoritos.has(id); return `<button class="coracao" data-fav="${id}" aria-pressed="${f}" aria-label="${f ? 'Remover dos favoritos' : 'Adicionar aos favoritos'}">${f ? '♥' : '♡'}</button>`; };
const cardHtml = l => `<article class="card">${capaHtml(l)}<h3>${esc(l.titulo)}</h3><p>${esc(l.autor)}</p>${coracaoHtml(l.id)}<a class="btn" href="detalhes.html?id=${l.id}">Ver detalhes</a></article>`;
