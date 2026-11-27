const alvo = document.getElementById('detalhes-livro'), aviso = document.getElementById('aviso');
const id = Number(new URLSearchParams(location.search).get('id'));
const STATUS = { NAO_INICIADO: 'Não iniciado', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído' };
(async () => {
  try {
    const [lista] = await Promise.all([api('/livros'), carregarFavoritos().catch(() => {})]);
    const l = lista.find(x => x.id === id);
    if (!l) return mensagem(aviso, 'Livro não encontrado.', 'erro');
    document.title = l.titulo + ' | Biblioteca Virtual';
    const texto = l.descricao || l.sinopse || 'Este livro ainda não tem descrição.';

    // Histórico de leitura (só para quem está logado)
    let hist = null;
    if (sessao.token) hist = await api('/leitura/' + l.id).catch(() => null);

    const leituraHtml = () => {
      if (!sessao.token) return `<p class="leitura-detalhe"><a href="login.html?voltar=${encodeURIComponent('detalhes.html?id=' + l.id)}">Entre na sua conta</a> para acompanhar sua leitura.</p>`;
      if (!hist) return '<p class="leitura-detalhe"><button class="btn" data-ler>Começar a ler</button></p>';
      return `<div class="leitura-detalhe"><div class="barra" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${hist.progresso}" aria-label="Progresso de leitura"><span style="width:${hist.progresso}%"></span></div><small>${hist.progresso}% · ${esc(STATUS[hist.status] || hist.status)} — <a href="perfil.html">atualizar no meu perfil</a></small></div>`;
    };

    const desenhar = () => alvo.innerHTML = `<div class="detalhe">${capaHtml(l)}<div><span class="rotulo">${esc(l.genero)}</span><h1>${esc(l.titulo)}</h1><p class="autor">${esc(l.autor)}</p>
      <p class="sinopse">${esc(texto)}</p>
      ${leituraHtml()}
      <button class="coracao" data-fav="${l.id}" aria-pressed="${favoritos.has(l.id)}">${favoritos.has(l.id) ? '♥ Nos favoritos' : '♡ Favoritar'}</button> <a class="btn" href="index.html#catalogo">Voltar ao catálogo</a></div></div>`;
    desenhar();
    alvo.addEventListener('click', async e => {
      if (e.target.closest('[data-fav]') && await alternarFavorito(l.id, aviso)) desenhar();
      if (e.target.closest('[data-ler]')) {
        try { hist = await api('/leitura', { metodo: 'POST', corpo: { livroId: l.id } }); desenhar(); mensagem(aviso, 'Leitura iniciada! Atualize seu progresso no perfil.', 'ok'); }
        catch (err) { mensagem(aviso, err.message, 'erro'); }
      }
    });
  } catch (e) { mensagem(aviso, e.message, 'erro'); }
})();
