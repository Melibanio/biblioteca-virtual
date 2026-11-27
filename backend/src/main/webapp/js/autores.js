// Carrossel de autores brasileiros famosos.
const AUTORES = [
  { nome: 'Machado de Assis', anos: '1839–1908', obra: 'Dom Casmurro', texto: 'Fundador da Academia Brasileira de Letras e mestre da ironia no romance realista.' },
  { nome: 'José de Alencar', anos: '1829–1877', obra: 'Iracema', texto: 'Grande nome do Romantismo, criou romances indianistas, urbanos e regionais.' },
  { nome: 'Lima Barreto', anos: '1881–1922', obra: 'Triste Fim de Policarpo Quaresma', texto: 'Retratou com crítica social o Rio de Janeiro do início do século XX.' },
  { nome: 'Monteiro Lobato', anos: '1882–1948', obra: 'Sítio do Picapau Amarelo', texto: 'Pioneiro da literatura infantil brasileira, criador de Emília e Narizinho.' },
  { nome: 'Graciliano Ramos', anos: '1892–1953', obra: 'Vidas Secas', texto: 'Escritor de prosa seca e precisa, voltado à realidade do sertão nordestino.' },
  { nome: 'Cecília Meireles', anos: '1901–1964', obra: 'Romanceiro da Inconfidência', texto: 'Uma das maiores poetas da língua portuguesa, de versos musicais e delicados.' },
  { nome: 'Carlos Drummond de Andrade', anos: '1902–1987', obra: 'A Rosa do Povo', texto: 'Poeta do cotidiano e da reflexão, figura central do Modernismo brasileiro.' },
  { nome: 'João Guimarães Rosa', anos: '1908–1967', obra: 'Grande Sertão: Veredas', texto: 'Reinventou a linguagem literária ao misturar o falar sertanejo com invenção poética.' },
  { nome: 'Jorge Amado', anos: '1912–2001', obra: 'Capitães da Areia', texto: 'Romancista baiano que deu voz ao povo, à cultura e às paisagens da Bahia.' },
  { nome: 'Clarice Lispector', anos: '1920–1977', obra: 'A Hora da Estrela', texto: 'Autora de uma escrita intimista e inovadora, referência da literatura do século XX.' }
];

(() => {
  const trilho = document.getElementById('autores-trilho');
  const ant = document.getElementById('autor-ant');
  const prox = document.getElementById('autor-prox');
  if (!trilho) return;

  const iniciais = n => n.split(' ').filter(p => p.length > 2 && p[0] === p[0].toUpperCase()).map(p => p[0]).slice(0, 2).join('');

  trilho.innerHTML = AUTORES.map(a => `
    <article class="autor">
      <div class="avatar" aria-hidden="true">${esc(iniciais(a.nome))}</div>
      <h3>${esc(a.nome)}</h3>
      <div class="anos">${esc(a.anos)}</div>
      <p>${esc(a.texto)}</p>
      <p class="obra">Obra de destaque: <strong>${esc(a.obra)}</strong></p>
      <a href="index.html?busca=${encodeURIComponent(a.nome)}#catalogo" data-autor="${esc(a.nome)}">Ver livros no catálogo</a>
    </article>`).join('');

  const passo = () => (trilho.querySelector('.autor')?.offsetWidth || 260) + 18;
  const atualizar = () => {
    ant.disabled = trilho.scrollLeft <= 2;
    prox.disabled = trilho.scrollLeft + trilho.clientWidth >= trilho.scrollWidth - 2;
  };

  ant.addEventListener('click', () => trilho.scrollBy({ left: -passo() }));
  prox.addEventListener('click', () => trilho.scrollBy({ left: passo() }));
  trilho.addEventListener('scroll', atualizar, { passive: true });
  trilho.addEventListener('keydown', e => {
    if (e.key === 'ArrowRight') { e.preventDefault(); trilho.scrollBy({ left: passo() }); }
    if (e.key === 'ArrowLeft') { e.preventDefault(); trilho.scrollBy({ left: -passo() }); }
  });
  window.addEventListener('resize', atualizar);
  atualizar();

  // Clicar no autor filtra o catálogo pela busca.
  trilho.addEventListener('click', e => {
    const link = e.target.closest('[data-autor]');
    const busca = document.getElementById('busca');
    if (!link || !busca) return;
    e.preventDefault();
    busca.value = link.dataset.autor;
    busca.dispatchEvent(new Event('input'));
    document.getElementById('catalogo').scrollIntoView({ behavior: 'smooth' });
  });
})();
