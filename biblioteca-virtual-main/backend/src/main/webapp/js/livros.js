const grade = document.getElementById('catalogo-grade'), aviso = document.getElementById('aviso');
const campo = document.getElementById('busca'), filtros = document.getElementById('filtros'), contador = document.getElementById('contador');
let livros = [], genero = 'Todos';

function mostrar() {
  const q = norm(campo.value.trim());
  const lista = livros.filter(l => (genero === 'Todos' || l.genero === genero) && (!q || norm(l.titulo).includes(q) || norm(l.autor).includes(q)));
  contador.textContent = lista.length + (lista.length === 1 ? ' livro' : ' livros');
  grade.innerHTML = lista.length ? lista.map(cardHtml).join('') : '<p class="vazio">Nenhum livro encontrado. Tente outro título, autor ou categoria.</p>';
}
function mostrarFiltros() {
  const gens = ['Todos', ...new Set(livros.map(l => l.genero).filter(Boolean))];
  filtros.innerHTML = gens.map(g => `<button class="chip" data-g="${esc(g)}" aria-pressed="${g === genero}">${esc(g)}</button>`).join('');
}
filtros.addEventListener('click', e => { const b = e.target.closest('[data-g]'); if (b) { genero = b.dataset.g; mostrarFiltros(); mostrar(); } });
campo.addEventListener('input', mostrar);
grade.addEventListener('click', async e => {
  const b = e.target.closest('[data-fav]'); if (!b) return;
  if (await alternarFavorito(Number(b.dataset.fav), aviso)) mostrar();
});
(async () => {
  try {
    livros = await api('/livros');
    await carregarFavoritos().catch(() => {});
    mostrarFiltros(); mostrar();
  } catch (e) { contador.textContent = ''; mensagem(aviso, e.message, 'erro'); }
})();
