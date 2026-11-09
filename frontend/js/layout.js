// Cabeçalho e rodapé iguais em todas as páginas (estilo do menu)
(() => {
  const pg = document.body.dataset.pagina, u = sessao.usuario;
  const link = (h, t, p) => `<a href="${h}"${p === pg ? ' class="active" aria-current="page"' : ''}>${t}</a>`;
  document.body.insertAdjacentHTML('afterbegin', `<header class="header"><a href="index.html" class="logo" aria-label="Biblioteca Virtual - início"><strong>biblioteca</strong><small>-virtual-</small></a>
    <nav class="menu" aria-label="Menu principal">${link('index.html', 'Início', 'inicio')}${link('index.html#catalogo', 'Catálogo', '')}${link('favoritos.html', 'Favoritos', 'favoritos')}${u && u.tipo === 'admin' ? link('admin.html', 'Administração', 'admin') : ''}</nav>
    <div class="acoes">${u ? `<span>Olá, ${esc(u.nome.split(' ')[0])}</span><button id="sair">Sair</button>` : `${link('login.html', 'Entrar', 'login')}${link('cadastro.html', 'Criar conta', 'cadastro')}`}</div></header>`);
  document.body.insertAdjacentHTML('beforeend', '<footer><span>© 2026 biblioteca-virtual</span><span>Literatura brasileira para todos os estudantes.</span></footer>');
  document.getElementById('sair')?.addEventListener('click', () => { sessao.sair(); location.href = 'index.html'; });
})();
