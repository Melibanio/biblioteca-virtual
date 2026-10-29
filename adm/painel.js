/* =========================================================
   ENDEREÇO DA API JAVA: servlet LivroServlet mapeada em /livros
   ========================================================= */
var enderecoApi = "../livros";
var formulario = document.getElementById("form-livro");
var tabela = document.getElementById("lista-livros");
var totalLivros = document.getElementById("total-livros");
var aviso = document.getElementById("aviso");
var tituloFormulario = document.getElementById("titulo-formulario");
var botaoSalvar = document.getElementById("salvar");
var botaoCancelar = document.getElementById("cancelar");
var filtro = document.getElementById("filtro-livros");
var livros = [];
var temporizadorAviso;

function mostrarAviso(mensagem, tipo) {
    aviso.textContent = mensagem;
    aviso.className = "aviso " + tipo;
    clearTimeout(temporizadorAviso);
    temporizadorAviso = setTimeout(function () { aviso.className = "aviso"; }, 4500);
}

function escaparHtml(valor) {
    return String(valor == null ? "" : valor).replace(/&/g, "&amp;").replace(/</g, "&lt;")
        .replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

function carregarLivros() {
    fetch(enderecoApi)
        .then(function (resposta) {
            if (!resposta.ok) throw new Error("Não foi possível consultar o banco de dados.");
            return resposta.json();
        })
        .then(function (dados) {
            livros = dados;
            mostrarLivros();
        })
        .catch(function () {
            tabela.innerHTML = '<tr><td colspan="5" class="vazio">Não foi possível carregar o acervo. Confira se a aplicação Java está executando pelo Maven.</td></tr>';
            totalLivros.textContent = "Sem conexão";
            mostrarAviso("A API Java não respondeu. Inicie o projeto com mvn tomcat7:run.", "erro");
        });
}

function mostrarLivros() {
    var termo = filtro.value.trim().toLowerCase();
    var encontrados = [];
    for (var i = 0; i < livros.length; i++) {
        var livro = livros[i];
        var texto = (livro.titulo + " " + livro.autor + " " + livro.genero).toLowerCase();
        if (texto.indexOf(termo) !== -1) encontrados.push(livro);
    }
    totalLivros.textContent = encontrados.length + (encontrados.length === 1 ? " livro" : " livros");
    if (encontrados.length === 0) {
        tabela.innerHTML = '<tr><td colspan="5" class="vazio">Nenhum livro encontrado.</td></tr>';
        return;
    }
    var html = "";
    for (var j = 0; j < encontrados.length; j++) {
        var item = encontrados[j];
        html += "<tr>";
        html += "<td>" + item.id + "</td>";
        html += "<td><strong>" + escaparHtml(item.titulo) + "</strong></td>";
        html += "<td>" + escaparHtml(item.autor) + "</td>";
        html += "<td>" + escaparHtml(item.genero) + "</td>";
        html += '<td><div class="acoes-linha"><button type="button" class="botao-pequeno editar" data-acao="editar" data-id="' + item.id + '">Editar</button>';
        html += '<button type="button" class="botao-pequeno excluir" data-acao="excluir" data-id="' + item.id + '">Excluir</button></div></td></tr>';
    }
    tabela.innerHTML = html;
}

function limparFormulario() {
    formulario.reset();
    document.getElementById("id").value = "";
    tituloFormulario.textContent = "Adicionar livro";
    botaoSalvar.textContent = "Cadastrar livro";
    botaoCancelar.hidden = true;
}

function editarLivro(id) {
    var livro = null;
    for (var i = 0; i < livros.length; i++) if (livros[i].id === id) livro = livros[i];
    if (!livro) return;
    document.getElementById("id").value = livro.id;
    document.getElementById("titulo").value = livro.titulo || "";
    document.getElementById("autor").value = livro.autor || "";
    document.getElementById("genero").value = livro.genero || "";
    document.getElementById("capa").value = livro.capa || "";
    document.getElementById("sinopse").value = livro.sinopse || "";
    tituloFormulario.textContent = "Editar livro #" + livro.id;
    botaoSalvar.textContent = "Salvar alterações";
    botaoCancelar.hidden = false;
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function enviarFormulario(dados) {
    return fetch(enderecoApi, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body: new URLSearchParams(dados).toString()
    }).then(function (resposta) {
        return resposta.json().then(function (conteudo) {
            if (!resposta.ok) throw new Error(conteudo.mensagem || "Não foi possível concluir a operação.");
            return conteudo;
        });
    });
}

formulario.addEventListener("submit", function (evento) {
    evento.preventDefault();
    var id = document.getElementById("id").value;
    var dados = {
        acao: id ? "atualizar" : "cadastrar",
        id: id,
        titulo: document.getElementById("titulo").value,
        autor: document.getElementById("autor").value,
        genero: document.getElementById("genero").value,
        capa: document.getElementById("capa").value,
        sinopse: document.getElementById("sinopse").value
    };
    botaoSalvar.disabled = true;
    enviarFormulario(dados).then(function (resultado) {
        mostrarAviso(resultado.mensagem, "sucesso");
        limparFormulario();
        carregarLivros();
    }).catch(function (erro) {
        mostrarAviso(erro.message, "erro");
    }).finally(function () { botaoSalvar.disabled = false; });
});

tabela.addEventListener("click", function (evento) {
    var botao = evento.target.closest("button[data-acao]");
    if (!botao) return;
    var id = Number(botao.getAttribute("data-id"));
    if (botao.getAttribute("data-acao") === "editar") {
        editarLivro(id);
        return;
    }
    var livro = null;
    for (var i = 0; i < livros.length; i++) if (livros[i].id === id) livro = livros[i];
    if (!livro || !window.confirm('Deseja realmente excluir "' + livro.titulo + '" do acervo?')) return;
    enviarFormulario({ acao: "excluir", id: id }).then(function (resultado) {
        mostrarAviso(resultado.mensagem, "sucesso");
        if (document.getElementById("id").value === String(id)) limparFormulario();
        carregarLivros();
    }).catch(function (erro) { mostrarAviso(erro.message, "erro"); });
});

botaoCancelar.addEventListener("click", limparFormulario);
filtro.addEventListener("input", mostrarLivros);
carregarLivros();
