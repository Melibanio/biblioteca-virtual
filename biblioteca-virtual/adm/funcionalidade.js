var CHAVE = "biblioteca_livros";
var CHAVE_CAPAS = "biblioteca_capas";

/* lista que aparece na primeira vez que o site abre */
var livrosIniciais = [
    {
        id: 1,
        titulo: "Dom Casmurro",
        autor: "Machado de Assis",
        genero: "Clássico",
        capa: "",
        sinopse: "Bentinho relembra a juventude e o casamento com Capitu, tentando provar uma traição que talvez só exista no ciúme dele. O narrador conta tudo do seu jeito, e cabe ao leitor decidir em quem acreditar."
    },
    {
        id: 2,
        titulo: "A Hora da Estrela",
        autor: "Clarice Lispector",
        genero: "Contemporâneo",
        capa: "",
        sinopse: "Macabéa é uma moça pobre do Nordeste que vive no Rio de Janeiro como datilógrafa. O narrador acompanha sua rotina simples e mostra como a sociedade ignora pessoas assim."
    },
    {
        id: 3,
        titulo: "Capitães da Areia",
        autor: "Jorge Amado",
        genero: "Regionalismo",
        capa: "",
        sinopse: "Um grupo de meninos abandonados vive em um trapiche em Salvador e sobrevive de pequenos furtos. O livro mostra a infância nas ruas e a dureza da desigualdade social."
    },
    {
        id: 4,
        titulo: "Grande Sertão: Veredas",
        autor: "Guimarães Rosa",
        genero: "Regionalismo",
        capa: "",
        sinopse: "O ex-jagunço Riobaldo conta sua vida no sertão, as batalhas entre bandos e a amizade confusa com Diadorim. No meio disso, questiona se realmente fez um pacto com o diabo."
    },
    {
        id: 5,
        titulo: "Memórias Póstumas de Brás Cubas",
        autor: "Machado de Assis",
        genero: "Clássico",
        capa: "",
        sinopse: "Brás Cubas conta a própria vida depois de morto, com muita ironia. Ele revisa amores, ambições e fracassos e conclui que não deixou herdeiros para a sua miséria."
    },
    {
        id: 6,
        titulo: "O Cortiço",
        autor: "Aluísio Azevedo",
        genero: "Clássico",
        capa: "",
        sinopse: "João Romão enriquece explorando os moradores de um cortiço no Rio de Janeiro. O livro trata o ambiente quase como um personagem que influencia todos que vivem ali."
    },
    {
        id: 7,
        titulo: "Vidas Secas",
        autor: "Graciliano Ramos",
        genero: "Regionalismo",
        capa: "",
        sinopse: "Fabiano, Sinhá Vitória, os dois filhos e a cachorra Baleia andam pelo sertão fugindo da seca. Em capítulos curtos, o livro mostra a fome e a falta de palavras dessa família."
    },
    {
        id: 8,
        titulo: "Iracema",
        autor: "José de Alencar",
        genero: "Romance",
        capa: "",
        sinopse: "A índia Iracema se apaixona pelo português Martim, no Ceará colonial. Desse encontro nasce Moacir, apresentado como símbolo do início do povo brasileiro."
    },
    {
        id: 9,
        titulo: "Quarto de Despejo",
        autor: "Carolina Maria de Jesus",
        genero: "Memórias",
        capa: "",
        sinopse: "Diário real de uma catadora de papel que morava na favela do Canindé, em São Paulo. Ela registra a fome, a criação dos filhos e o desejo de escrever."
    },
    {
        id: 10,
        titulo: "Macunaíma",
        autor: "Mário de Andrade",
        genero: "Modernismo",
        capa: "",
        sinopse: "Macunaíma nasce na floresta e viaja até São Paulo para recuperar um amuleto perdido. A história mistura lendas indígenas, folclore e crítica ao país."
    },
    {
        id: 11,
        titulo: "Torto Arado",
        autor: "Itamar Vieira Junior",
        genero: "Contemporâneo",
        capa: "",
        sinopse: "As irmãs Bibiana e Belonísia crescem em uma fazenda no interior da Bahia, onde as famílias trabalham sem ter a terra. Um acidente na infância marca a vida das duas para sempre."
    },
    {
        id: 12,
        titulo: "O Alienista",
        autor: "Machado de Assis",
        genero: "Clássico",
        capa: "",
        sinopse: "O médico Simão Bacamarte abre um hospício em Itaguaí e começa a internar quase toda a cidade. Aos poucos, fica difícil saber quem é louco e quem é normal."
    }
];

/* variaveis usadas na tela */
var livros = [];
var capasSalvas = {};   
var capasBuscando = {}; 
var generoAtivo = "Todos";
var textoBusca = "";

/* pegando os elementos do HTML */
var grid = document.getElementById("grid");
var faixaGeneros = document.getElementById("genre-strip");
var contador = document.getElementById("result-count");
var campoBusca = document.getElementById("search-input");
var botaoBusca = document.getElementById("search-btn");

var modal = document.getElementById("modal-sinopse");
var modalTitulo = document.getElementById("sinopse-titulo");
var modalAutor = document.getElementById("sinopse-autor");
var modalTexto = document.getElementById("sinopse-texto");
var botaoFechar = document.getElementById("fechar-sinopse");


/* =========================================================
LER OS DADOS SALVOS NO NAVEGADOR
======================================================== */
function carregarLivros() {
    var salvos = localStorage.getItem(CHAVE);

    if (salvos === null) {
        // primeira vez que o site abre: usa a lista inicial
        livros = livrosIniciais;
        localStorage.setItem(CHAVE, JSON.stringify(livros));
    } else {
        livros = JSON.parse(salvos);
    }

    var capas = localStorage.getItem(CHAVE_CAPAS);

    if (capas === null) {
        capasSalvas = {};
    } else {
        capasSalvas = JSON.parse(capas);
    }
}

function salvarCapas() {
    localStorage.setItem(CHAVE_CAPAS, JSON.stringify(capasSalvas));
}
function procurarCapa(livro) {

    // se o administrador cadastrou o endereco da capa, usa o dele
    if (livro.capa) {
        return;
    }

    // se ja procurou antes, nao procura de novo
    if (capasSalvas[livro.id] !== undefined) {
        return;
    }

    if (capasBuscando[livro.id] === true) {
        return;
    }

    capasBuscando[livro.id] = true;

    var busca = encodeURIComponent(livro.titulo + " " + livro.autor);
    var endereco = "https://openlibrary.org/search.json?q=" + busca + "&limit=1&fields=cover_i";

    fetch(endereco)
        .then(function (resposta) {
            return resposta.json();
        })
        .then(function (dados) {
            var primeiro = dados.docs[0];

            if (primeiro && primeiro.cover_i) {
                capasSalvas[livro.id] = "https://covers.openlibrary.org/b/id/" + primeiro.cover_i + "-M.jpg";
            } else {
                capasSalvas[livro.id] = ""; 
            }
            salvarCapas();
            capasBuscando[livro.id] = false;
            mostrarLivros();
        })
        .catch(function () {
            capasSalvas[livro.id] = "";
            capasBuscando[livro.id] = false;
            mostrarLivros();
        });
}

/* devolve o endereco da capa do livro, ou vazio se nao tiver */
function enderecoDaCapa(livro) {
    if (livro.capa) {
        return livro.capa;
    }

    if (capasSalvas[livro.id]) {
        return capasSalvas[livro.id];
    }

    return "";
}


/* =========================================================
MONTAR A CAPA QUE VAI APARECER NO CARD
   ========================================================= */
function montarCapa(livro) {
    var endereco = enderecoDaCapa(livro);
    if (endereco !== "") {
        return '<img src="' + endereco + '" alt="Capa de ' + livro.titulo + '" loading="lazy">';
    }
    if (capasBuscando[livro.id] === true) {
        return '<div class="cover-skeleton"></div>';
    }
    // nao achou capa: mostra a capa simples com o titulo
    return '<div class="cover-fallback">' + livro.titulo + '</div>';
}


/* =========================================================
MONTAR OS BOTOES DE GENERO
========================================================= */
function montarGeneros() {
    var generos = ["Todos"];

    for (var i = 0; i < livros.length; i++) {
        if (generos.indexOf(livros[i].genero) === -1) {
            generos.push(livros[i].genero);
        }
    }

    var html = "";

    for (var j = 0; j < generos.length; j++) {
        var classe = "genre-chip";

        if (generos[j] === generoAtivo) {
            classe = "genre-chip active";
        }

        html += '<button type="button" class="' + classe + '" data-genero="' + generos[j] + '">' + generos[j] + '</button>';
    }

    faixaGeneros.innerHTML = html;

    // liga o clique de cada botao de genero
    var botoes = faixaGeneros.querySelectorAll(".genre-chip");

    for (var k = 0; k < botoes.length; k++) {
        botoes[k].addEventListener("click", function () {
            generoAtivo = this.getAttribute("data-genero");
            mostrarLivros();
        });
    }
}


/* =========================================================
FILTRAR PELO GENERO E PELA BUSCA
========================================================= */
function filtrarLivros() {
    var resultado = [];
    var busca = textoBusca.trim().toLowerCase();
    for (var i = 0; i < livros.length; i++) {
        var livro = livros[i];
        var passouGenero = (generoAtivo === "Todos" || livro.genero === generoAtivo);
        var passouBusca = (busca === "" ||
            livro.titulo.toLowerCase().indexOf(busca) !== -1 ||
            livro.autor.toLowerCase().indexOf(busca) !== -1);
        if (passouGenero && passouBusca) {
            resultado.push(livro);
        }
    }
    return resultado;
}


/* =========================================================
MOSTRAR OS LIVROS NA TELA
========================================================= */
function mostrarLivros() {
    montarGeneros();

    var lista = filtrarLivros();

    if (lista.length === 1) {
        contador.textContent = "1 livro";
    } else {
        contador.textContent = lista.length + " livros";
    }

    if (lista.length === 0) {
        grid.innerHTML = '<div class="empty-state">Nenhum livro encontrado para essa busca.</div>';
        return;
    }

    var html = "";

    for (var i = 0; i < lista.length; i++) {
        var livro = lista[i];

        html += '<div class="card">';
        html += '   <div class="cover-wrap">' + montarCapa(livro) + '</div>';
        html += '   <div class="card-topo">';
        html += '       <span class="titulo">' + livro.titulo + '</span>';
        html += '       <button type="button" class="btn-sinopse" data-id="' + livro.id + '" title="Ver sinopse">i</button>';
        html += '   </div>';
        html += '   <div class="autor">' + livro.autor + '</div>';
        html += '   <span class="genero">' + livro.genero + '</span>';
        html += '</div>';
    }

    grid.innerHTML = html;

    // se alguma imagem nao carregar, troca pela capa simples
    var imagens = grid.querySelectorAll(".cover-wrap img");

    for (var x = 0; x < imagens.length; x++) {
        imagens[x].addEventListener("error", function () {
            var nome = this.getAttribute("alt").replace("Capa de ", "");
            this.parentNode.innerHTML = '<div class="cover-fallback">' + nome + '</div>';
        });
    }

    // sinopse
    var botoes = grid.querySelectorAll(".btn-sinopse");

    for (var j = 0; j < botoes.length; j++) {
        botoes[j].addEventListener("click", function () {
            abrirSinopse(Number(this.getAttribute("data-id")));
        });
    }
}


/* =========================================================
JANELA DA SINOPSE
======================================================== */
function abrirSinopse(id) {
    var livro = null;

    for (var i = 0; i < livros.length; i++) {
        if (livros[i].id === id) {
            livro = livros[i];
        }
    }

    if (livro === null) {
        return;
    }

    modalTitulo.textContent = livro.titulo;
    modalAutor.textContent = livro.autor + " · " + livro.genero;

    if (livro.sinopse) {
        modalTexto.textContent = livro.sinopse;
    } else {
        modalTexto.textContent = "Este livro ainda não tem sinopse cadastrada.";
    }

    modal.classList.add("aberto");
}

function fecharSinopse() {
    modal.classList.remove("aberto");
}

botaoFechar.addEventListener("click", fecharSinopse);

modal.addEventListener("click", function (evento) {
    if (evento.target === modal) {
        fecharSinopse();
    }
});

document.addEventListener("keydown", function (evento) {
    if (evento.key === "Escape") {
        fecharSinopse();
    }
});


/* =========================================================
BUSCA
========================================================= */
campoBusca.addEventListener("input", function () {
    textoBusca = campoBusca.value;
    mostrarLivros();
});

botaoBusca.addEventListener("click", function () {
    textoBusca = campoBusca.value;
    mostrarLivros();
});

carregarLivros();
for (var n = 0; n < livros.length; n++) {
    procurarCapa(livros[n]);
}

mostrarLivros();