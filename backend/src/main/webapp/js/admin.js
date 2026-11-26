// Administração do acervo: listar, cadastrar, editar e excluir livros.
// Depende de api.js (api, sessao, esc, mensagem) e layout.js.
// Rotas usadas: GET /livros | POST /livros | PUT /livros/{id} | DELETE /livros/{id}
// O backend valida o perfil de administrador; a verificação abaixo é só de interface.

const form = document.getElementById('form-livro');
const aviso = document.getElementById('aviso');
const campoId = document.getElementById('id');
const lista = document.getElementById('lista-admin');
const total = document.getElementById('total');
const tituloForm = document.getElementById('titulo-form');
const btnSalvar = document.getElementById('salvar');
const btnCancelar = document.getElementById('cancelar');

const campo = nome => form.elements[nome];

let livros = [];

// ---- Acesso: exige login e perfil de administrador
const usuario = sessao.usuario;
<<<<<<< HEAD
const autorizado = !!(sessao.token && usuario && usuario.tipo === 'admin');
=======
const autorizado = !!(sessao.token && usuario && String(usuario.tipo).toLowerCase() === 'admin');
>>>>>>> master

if (!autorizado) {
  document.querySelector('.admin').hidden = true;
  if (!sessao.token) {
    location.replace('login.html?voltar=admin.html');
  } else {
    mensagem(aviso, 'Acesso restrito ao administrador.', 'erro');
    setTimeout(() => location.replace('index.html'), 1500);
  }
} else {
  carregar();
}

// ---- Listagem
async function carregar() {
  lista.innerHTML = '<tr><td colspan="5">Carregando...</td></tr>';
  try {
    livros = await api('/livros');
    desenhar();
  } catch (e) {
    lista.innerHTML = '';
    total.textContent = '';
    tratarErro(e);
  }
}

function desenhar() {
  total.textContent = `(${livros.length})`;

  if (!livros.length) {
    lista.innerHTML = '<tr><td colspan="5">Nenhum livro cadastrado.</td></tr>';
    return;
  }

  lista.innerHTML = livros.map(l => `
    <tr>
      <td>${miniCapa(l)}</td>
      <td>${esc(l.titulo)}</td>
      <td>${esc(l.autor)}</td>
      <td>${esc(l.genero)}</td>
      <td>
        <button type="button" class="acao" data-editar="${l.id}">Editar</button>
        <button type="button" class="acao excluir" data-excluir="${l.id}">Excluir</button>
      </td>
    </tr>`).join('');

  // Capa quebrada: troca a imagem pelo título (o evento "error" não propaga).
  lista.querySelectorAll('td img').forEach(img =>
    img.addEventListener('error', () => {
      const div = document.createElement('div');
      div.className = 'mini';
      div.textContent = img.alt.replace(/^Capa de /, '');
      img.replaceWith(div);
    }, { once: true }));
}

const miniCapa = l => l.capa
  ? `<img src="${esc(l.capa)}" alt="Capa de ${esc(l.titulo)}">`
  : `<div class="mini">${esc(l.titulo)}</div>`;

// ---- Ações da tabela (delegação de eventos)
lista.addEventListener('click', e => {
  const editar = e.target.closest('[data-editar]');
  const excluir = e.target.closest('[data-excluir]');
  if (editar) iniciarEdicao(Number(editar.dataset.editar));
  if (excluir) excluirLivro(Number(excluir.dataset.excluir), excluir);
});

// ---- Formulário: cadastrar / editar
form.addEventListener('submit', async e => {
  e.preventDefault();

  const dados = lerFormulario();
  const erro = validarLivro(dados);
  if (erro) return mensagem(aviso, erro, 'erro');

  const id = campoId.value;
  btnSalvar.disabled = true;

  try {
    if (id) {
      await api('/livros/' + id, { metodo: 'PUT', corpo: dados });
    } else {
      await api('/livros', { metodo: 'POST', corpo: dados });
    }
    mensagem(aviso, id ? 'Livro atualizado com sucesso.' : 'Livro cadastrado com sucesso.', 'ok');
    limparFormulario();
    await carregar();
  } catch (err) {
    tratarErro(err);
  } finally {
    btnSalvar.disabled = false;
  }
});

btnCancelar.addEventListener('click', () => {
  limparFormulario();
  aviso.textContent = '';
  aviso.className = 'msg';
});

function lerFormulario() {
  const capa = campo('capa').value.trim();
  return {
    titulo: campo('titulo').value.trim(),
    autor: campo('autor').value.trim(),
    genero: campo('genero').value.trim(),
    descricao: campo('descricao').value.trim(),
    capa: capa || null
  };
}

// Mesmos limites do backend (LivroService) e do SQL.
function validarLivro(d) {
  limparInvalidos();
  const falha = (nome, texto) => {
    campo(nome).setAttribute('aria-invalid', 'true');
    campo(nome).focus();
    return texto;
  };

  if (!d.titulo) return falha('titulo', 'Preencha o campo "Título".');
  if (d.titulo.length > 150) return falha('titulo', 'O título deve ter até 150 caracteres.');
  if (!d.autor) return falha('autor', 'Preencha o campo "Autor".');
  if (d.autor.length > 100) return falha('autor', 'O autor deve ter até 100 caracteres.');
  if (!d.genero) return falha('genero', 'Preencha o campo "Categoria".');
  if (d.genero.length > 80) return falha('genero', 'A categoria deve ter até 80 caracteres.');

  if (d.capa) {
    if (d.capa.length > 255) return falha('capa', 'O endereço da capa deve ter até 255 caracteres.');
    try {
      const url = new URL(d.capa);
      if (url.protocol !== 'http:' && url.protocol !== 'https:') throw new Error();
    } catch {
      return falha('capa', 'Informe uma URL de capa válida, começando com http:// ou https://.');
    }
  }
  return '';
}

function limparInvalidos() {
  for (const el of form.elements) el.removeAttribute('aria-invalid');
}

function iniciarEdicao(id) {
  const livro = livros.find(l => l.id === id);
  if (!livro) return;

  campoId.value = livro.id;
  campo('titulo').value = livro.titulo ?? '';
  campo('autor').value = livro.autor ?? '';
  campo('genero').value = livro.genero ?? '';
  campo('capa').value = livro.capa ?? '';
  campo('descricao').value = livro.descricao ?? '';

  tituloForm.textContent = 'Editar livro';
  btnSalvar.textContent = 'Salvar alterações';
  btnCancelar.hidden = false;
  limparInvalidos();
  aviso.textContent = '';
  aviso.className = 'msg';

  form.scrollIntoView({ behavior: 'smooth', block: 'start' });
  campo('titulo').focus();
}

function limparFormulario() {
  form.reset();
  campoId.value = '';
  tituloForm.textContent = 'Adicionar livro';
  btnSalvar.textContent = 'Cadastrar livro';
  btnCancelar.hidden = true;
  limparInvalidos();
}

// ---- Exclusão
async function excluirLivro(id, botao) {
  const livro = livros.find(l => l.id === id);
  if (!livro) return;

  const ok = confirm(
    `Excluir "${livro.titulo}"?\n\n` +
    'Ele também será removido dos favoritos e do histórico de leitura dos usuários. ' +
    'Esta ação não pode ser desfeita.');
  if (!ok) return;

  botao.disabled = true;

  try {
    await api('/livros/' + id, { metodo: 'DELETE' });
    if (campoId.value === String(id)) limparFormulario();
    mensagem(aviso, `"${livro.titulo}" foi excluído.`, 'ok');
    await carregar();
  } catch (err) {
    botao.disabled = false;
    tratarErro(err);
  }
}

// ---- Erros: sessão expirada volta ao login; o restante aparece no aviso.
// (api() já limpa a sessão local quando o servidor responde 401.)
function tratarErro(err) {
  if (!sessao.token) {
    location.replace('login.html?voltar=admin.html');
    return;
  }
  mensagem(aviso, err.message, 'erro');
}
