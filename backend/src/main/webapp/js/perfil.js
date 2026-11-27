// Perfil do usuário: dados, senha, leituras e favoritos.
// Rotas: GET/PUT /usuarios/perfil | PUT /usuarios/senha | GET /leitura | PUT /leitura/{livroId} | GET /livros | /favoritos
(() => {
  if (!sessao.token) { location.replace('login.html?voltar=perfil.html'); return; }

  const $ = id => document.getElementById(id);
  const aviso = $('aviso'), formDados = $('form-dados'), formSenha = $('form-senha');
  const STATUS = { NAO_INICIADO: 'Não iniciado', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído' };
  let favs = [], leituras = [], livrosPorId = new Map();

  const iniciais = n => String(n || '?').split(/\s+/).filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('');
  const ehAdmin = u => String(u?.tipo).toLowerCase() === 'admin';

  function desenharPerfil(u) {
    $('avatar').textContent = iniciais(u.nome);
    $('perfil-nome').textContent = u.nome;
    $('perfil-email').textContent = u.email;
    $('selo').textContent = ehAdmin(u) ? 'Administrador' : 'Leitor(a)';
    $('link-admin').hidden = !ehAdmin(u);
    formDados.nome.value = u.nome;
    formDados.email.value = u.email;
    document.title = u.nome.split(' ')[0] + ' | Biblioteca Virtual';
    const ola = document.querySelector('.acoes a[href="perfil.html"]');
    if (ola) ola.textContent = 'Olá, ' + u.nome.split(' ')[0];
  }

  function desenharStats() {
    $('st-fav').textContent = favs.length;
    $('st-lendo').textContent = leituras.filter(l => l.status !== 'CONCLUIDO').length;
    $('st-fim').textContent = leituras.filter(l => l.status === 'CONCLUIDO').length;
  }

  function desenharLeituras() {
    const alvo = $('leituras');
    if (!leituras.length) {
      alvo.innerHTML = '<li class="vazio">Você ainda não começou nenhuma leitura. <a href="index.html#catalogo"><strong>Escolha um livro no catálogo</strong></a> e toque em "Começar a ler".</li>';
      return;
    }
    alvo.innerHTML = leituras.map(l => {
      const livro = livrosPorId.get(l.livroId) || { id: l.livroId, titulo: l.titulo, autor: l.autor, capa: null };
      return `<li class="leitura-item" data-livro="${l.livroId}">
        <a class="leitura-capa" href="detalhes.html?id=${l.livroId}" aria-label="Abrir ${esc(l.titulo)}">${capaHtml(livro)}</a>
        <div class="leitura-info">
          <h3><a href="detalhes.html?id=${l.livroId}">${esc(l.titulo)}</a></h3>
          <p>${esc(l.autor)}</p>
          <div class="barra" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${l.progresso}" aria-label="Progresso de leitura"><span style="width:${l.progresso}%"></span></div>
          <small>${l.progresso}% · ${esc(STATUS[l.status] || l.status)}</small>
          <div class="leitura-ajuste">
            <label for="r-${l.livroId}">Atualizar progresso</label>
            <input id="r-${l.livroId}" type="range" min="0" max="100" step="5" value="${l.progresso}">
            <output>${l.progresso}%</output>
            <button type="button" class="btn" data-salvar>Salvar</button>
          </div>
        </div>
      </li>`;
    }).join('');
  }

  function desenharFavoritos() {
    $('favoritos').innerHTML = favs.length
      ? favs.map(cardHtml).join('')
      : '<p class="vazio">Você ainda não favoritou nenhum livro. <a href="index.html#catalogo"><strong>Explorar o catálogo</strong></a></p>';
  }

  // ---- Dados da conta
  formDados.addEventListener('submit', async e => {
    e.preventDefault();
    const msg = $('msg-dados');
    const erro = validar(formDados);
    if (erro) return mensagem(msg, erro, 'erro');
    const btn = formDados.querySelector('button'); btn.disabled = true;
    try {
      const u = await api('/usuarios/perfil', { metodo: 'PUT', corpo: { nome: formDados.nome.value.trim(), email: formDados.email.value.trim() } });
      sessao.salvar(sessao.token, u);
      desenharPerfil(u);
      mensagem(msg, 'Dados atualizados com sucesso.', 'ok');
    } catch (err) { mensagem(msg, err.message, 'erro'); }
    finally { btn.disabled = false; }
  });

  // ---- Senha
  formSenha.addEventListener('submit', async e => {
    e.preventDefault();
    const msg = $('msg-senha');
    let erro = validar(formSenha);
    if (!erro && formSenha.novaSenha.value !== formSenha.confirmar.value) {
      erro = 'As senhas não coincidem.';
      formSenha.confirmar.setAttribute('aria-invalid', 'true'); formSenha.confirmar.focus();
    }
    if (erro) return mensagem(msg, erro, 'erro');
    const btn = formSenha.querySelector('button'); btn.disabled = true;
    try {
      await api('/usuarios/senha', { metodo: 'PUT', corpo: { senhaAtual: formSenha.senhaAtual.value, novaSenha: formSenha.novaSenha.value } });
      formSenha.reset();
      mensagem(msg, 'Senha alterada com sucesso.', 'ok');
    } catch (err) { mensagem(msg, err.message, 'erro'); }
    finally { btn.disabled = false; }
  });

  // ---- Leituras: ajustar e salvar o progresso
  $('leituras').addEventListener('input', e => {
    if (e.target.type === 'range') e.target.nextElementSibling.textContent = e.target.value + '%';
  });
  $('leituras').addEventListener('click', async e => {
    const botao = e.target.closest('[data-salvar]'); if (!botao) return;
    const item = botao.closest('[data-livro]');
    const livroId = Number(item.dataset.livro);
    const progresso = Number(item.querySelector('input[type=range]').value);
    botao.disabled = true;
    try {
      const novo = await api('/leitura/' + livroId, { metodo: 'PUT', corpo: { progresso } });
      leituras = leituras.map(l => l.livroId === livroId ? novo : l);
      desenharLeituras(); desenharStats();
      mensagem(aviso, 'Progresso salvo.', 'ok');
    } catch (err) { botao.disabled = false; mensagem(aviso, err.message, 'erro'); }
  });

  // ---- Favoritos: o coração remove da lista
  $('favoritos').addEventListener('click', async e => {
    const b = e.target.closest('[data-fav]'); if (!b) return;
    const id = Number(b.dataset.fav);
    if (await alternarFavorito(id, aviso)) {
      favs = favs.filter(l => l.id !== id);
      desenharFavoritos(); desenharStats();
      mensagem(aviso, 'Livro removido dos favoritos.', 'ok');
    }
  });

  // ---- Carga inicial
  (async () => {
    try {
      const [u, hist, livros, lista] = await Promise.all([
        api('/usuarios/perfil'), api('/leitura'), api('/livros'), carregarFavoritos()
      ]);
      livrosPorId = new Map(livros.map(l => [l.id, l]));
      leituras = hist; favs = lista;
      sessao.salvar(sessao.token, u);
      desenharPerfil(u); desenharStats(); desenharLeituras(); desenharFavoritos();
    } catch (err) {
      if (!sessao.token) { location.replace('login.html?voltar=perfil.html'); return; }
      mensagem(aviso, err.message, 'erro');
    }
  })();
})();
