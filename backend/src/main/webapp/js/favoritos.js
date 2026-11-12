const grade = document.getElementById('favoritos'), aviso = document.getElementById('aviso');
if (!sessao.token) location.replace('login.html?voltar=favoritos.html');
let lista = [];
const mostrar = () => grade.innerHTML = lista.length ? lista.map(cardHtml).join('') : '<p class="vazio">Você ainda não favoritou nenhum livro. <a href="index.html#catalogo"><strong>Explorar o catálogo</strong></a></p>';
grade.addEventListener('click', async e => {
  const b = e.target.closest('[data-fav]'); if (!b) return;
  const id = Number(b.dataset.fav);
  if (await alternarFavorito(id, aviso)) { lista = lista.filter(l => l.id !== id); mostrar(); mensagem(aviso, 'Livro removido dos favoritos.', 'ok'); }
});
carregarFavoritos().then(l => { lista = l; mostrar(); }).catch(e => mensagem(aviso, e.message, 'erro'));
