const alvo = document.getElementById('detalhes-livro'), aviso = document.getElementById('aviso');
const id = Number(new URLSearchParams(location.search).get('id'));
(async () => {
  try {
    const [lista] = await Promise.all([api('/livros'), carregarFavoritos().catch(() => {})]);
    const l = lista.find(x => x.id === id);
    if (!l) return mensagem(aviso, 'Livro não encontrado.', 'erro');
    document.title = l.titulo + ' | Biblioteca Virtual';
    const texto = l.descricao || l.sinopse || 'Este livro ainda não tem descrição.';
    const desenhar = () => alvo.innerHTML = `<div class="detalhe">${capaHtml(l)}<div><span class="rotulo">${esc(l.genero)}</span><h1>${esc(l.titulo)}</h1><p class="autor">${esc(l.autor)}</p>
      <p class="sinopse">${esc(texto)}</p>
      <button class="coracao" data-fav="${l.id}" aria-pressed="${favoritos.has(l.id)}">${favoritos.has(l.id) ? '♥ Nos favoritos' : '♡ Favoritar'}</button> <a class="btn" href="index.html#catalogo">Voltar ao catálogo</a></div></div>`;
    desenhar();
    alvo.addEventListener('click', async e => { if (e.target.closest('[data-fav]') && await alternarFavorito(l.id, aviso)) desenhar(); });
  } catch (e) { mensagem(aviso, e.message, 'erro'); }
})();
