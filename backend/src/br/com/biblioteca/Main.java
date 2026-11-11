package br.com.biblioteca;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.controller.FavoritoController;
import br.com.biblioteca.controller.LivroController;
import br.com.biblioteca.controller.UsuarioController;
import br.com.biblioteca.dao.UsuarioDAO;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.service.LivroService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.mindrot.jbcrypt.BCrypt;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) throws Exception {

        // Prepara o banco de dados e os dados iniciais
        criarTabelas();
        criarAdministrador();
        criarLivrosIniciais();

        // Cria o servidor na porta 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Rota para verificar se a API está funcionando
        server.createContext("/api/status", exchange -> {

            String mensagem = "{\"status\":\"Biblioteca Virtual API online\"}";
            byte[] resposta = mensagem.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=UTF-8");

            exchange.sendResponseHeaders(200, resposta.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(resposta);
            }
        });

        // Configura as rotas da API
        UsuarioController usuarioController = new UsuarioController();

        server.createContext("/api/usuarios", usuarioController);
        server.createContext("/api/login", usuarioController);
        server.createContext("/api/livros", new LivroController());
        server.createContext("/api/favoritos", new FavoritoController());

        // Procura a pasta do site
        Path raiz = acharRaizDoSite();

        // Disponibiliza o site pelo próprio servidor
        if (raiz != null) {
            server.createContext("/", exchange -> servirSite(exchange, raiz));
        } else {
            System.out.println("Pasta frontend não encontrada: só a API está no ar.");
        }

        // Inicia o servidor
        server.start();

        System.out.println("Servidor iniciado: http://localhost:8080/api/status");

        if (raiz != null) {
            System.out.println("Site no ar: http://localhost:8080");
        }
    }

    // Procura a pasta do site na raiz do projeto ou na pasta anterior
    static Path acharRaizDoSite() {

        if (Files.exists(Path.of("frontend", "index.html"))) {
            return Path.of(".").toAbsolutePath().normalize();
        }

        if (Files.exists(Path.of("..", "frontend", "index.html"))) {
            return Path.of("..").toAbsolutePath().normalize();
        }

        return null;
    }

    // Envia os arquivos do site para o navegador
    static void servirSite(HttpExchange exchange, Path raiz) throws IOException {

        // Permite somente requisições GET
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String caminho = exchange.getRequestURI().getPath();

        // Redireciona a página inicial para o frontend
        if (caminho.equals("/")) {
            exchange.getResponseHeaders().set("Location", "/frontend/index.html");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        // Localiza o arquivo solicitado
        Path arquivo = raiz.resolve(caminho.substring(1)).normalize();

        String relativo = "";

        if (arquivo.startsWith(raiz)) {
            relativo = raiz.relativize(arquivo).toString().replace('\\', '/');
        }

        // Define quais arquivos podem ser acessados
        boolean permitido = relativo.equals("index.html")
                || relativo.equals("style.css")
                || relativo.startsWith("frontend/");

        // Verifica se o arquivo pode ser enviado
        if (!permitido || !Files.isRegularFile(arquivo)) {

            String mensagem = "{\"mensagem\":\"Página não encontrada.\"}";
            byte[] erro = mensagem.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=UTF-8");

            exchange.sendResponseHeaders(404, erro.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(erro);
            }

            return;
        }

        // Lê o conteúdo do arquivo
        byte[] conteudo = Files.readAllBytes(arquivo);

        // Informa o tipo do arquivo ao navegador
        exchange.getResponseHeaders().set(
                "Content-Type", tipoDoArquivo(relativo));

        exchange.sendResponseHeaders(200, conteudo.length);

        // Envia o arquivo
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(conteudo);
        }
    }

    // Identifica o tipo do arquivo pelo nome
    private static String tipoDoArquivo(String nome) {

        String arquivo = nome.toLowerCase();

        if (arquivo.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }
        if (arquivo.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (arquivo.endsWith(".js")) {
            return "text/javascript; charset=UTF-8";
        }
        if (arquivo.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (arquivo.endsWith(".png")) {
            return "image/png";
        }
        if (arquivo.endsWith(".jpg") || arquivo.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (arquivo.endsWith(".webp")) {
            return "image/webp";
        }
        if (arquivo.endsWith(".gif")) {
            return "image/gif";
        }
        if (arquivo.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (arquivo.endsWith(".ico")) {
            return "image/x-icon";
        }
        if (arquivo.endsWith(".woff2")) {
            return "font/woff2";
        }
        return "application/octet-stream";
    }

    // Cria as tabelas do banco de dados
    private static void criarTabelas() throws Exception {

        // Cria a pasta database caso ela não exista
        new File("database").mkdirs();

        try (Connection con = Conexao.conectar();
                Statement st = con.createStatement()) {

            // Tabela de usuários
            st.execute("""
                    CREATE TABLE IF NOT EXISTS usuario (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        nome VARCHAR(100) NOT NULL,
                        email VARCHAR(120) NOT NULL UNIQUE,
                        senha_hash VARCHAR(255) NOT NULL,
                        tipo VARCHAR(20) NOT NULL
                    )""");

            // Tabela de livros
            st.execute("""
                    CREATE TABLE IF NOT EXISTS livro (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        titulo VARCHAR(150) NOT NULL,
                        autor VARCHAR(100) NOT NULL,
                        genero VARCHAR(80),
                        descricao CLOB,
                        capa VARCHAR(255)
                    )""");

            // Tabela de favoritos
            st.execute("""
                    CREATE TABLE IF NOT EXISTS favorito (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        usuario_id INT NOT NULL,
                        livro_id INT NOT NULL,
                        CONSTRAINT fk_favorito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
                        CONSTRAINT fk_favorito_livro FOREIGN KEY (livro_id) REFERENCES livro(id),
                        CONSTRAINT uk_favorito UNIQUE (usuario_id, livro_id)
                    )""");
        }
    }

    // Cria o administrador na primeira execução
    private static void criarAdministrador() throws Exception {

        String email = valor("ADMIN_EMAIL", "admin@biblioteca.com");
        String senha = valor("ADMIN_SENHA", "admin12345");

        UsuarioDAO dao = new UsuarioDAO();

        // Verifica se o administrador já existe
        if (dao.buscarPorEmail(email) == null) {

            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            Usuario administrador = new Usuario(
                    0,
                    "Administrador",
                    email,
                    senhaHash,
                    "ADMIN");

            dao.cadastrar(administrador);

            System.out.println("Administrador criado: " + email);
        }
    }

    // Cadastra livros de exemplo quando o catálogo está vazio
    private static void criarLivrosIniciais() throws Exception {

        LivroService service = new LivroService();

        // Verifica se já existem livros cadastrados
        if (!service.listar().isEmpty()) {
            return;
        }

        // Dados dos livros: título, autor, gênero e descrição
        String[][] livros = {
                {
                        "Dom Casmurro",
                        "Machado de Assis",
                        "Romance",
                        "Já mais velho, Bentinho relembra a infância, o amor por Capitu e o ciúme que passou a consumi-lo ao suspeitar de uma traição com o amigo Escobar. Como a história é contada só por ele, o leitor fica na dúvida: Capitu traiu ou tudo foi imaginação de Bentinho?"
                },
                {
                        "Memórias Póstumas de Brás Cubas",
                        "Machado de Assis",
                        "Romance",
                        "O defunto autor Brás Cubas conta a própria vida depois de morto, com ironia e humor ácido. Entre amores frustrados, ambições e vaidades, o livro faz um retrato crítico da sociedade do século XIX e é um marco do Realismo brasileiro."
                },
                {
                        "Iracema",
                        "José de Alencar",
                        "Romance",
                        "Em uma narrativa poética, a indígena Iracema se apaixona pelo guerreiro português Martim. A história de amor, cheia de encontros e perdas, simboliza o contato entre os povos nativos e os colonizadores na formação do Brasil."
                },
                {
                        "O Cortiço",
                        "Aluísio Azevedo",
                        "Romance",
                        "A vida em um cortiço do Rio de Janeiro do século XIX, onde o ambicioso João Romão enriquece à custa dos moradores. O livro mostra como o meio, a pobreza e a ganância moldam o comportamento das pessoas, e é uma das obras mais importantes do Naturalismo."
                },
                {
                        "Vidas Secas",
                        "Graciliano Ramos",
                        "Romance",
                        "Fabiano, Sinhá Vitória, os dois filhos e a cachorra Baleia atravessam o sertão nordestino fugindo da seca e da miséria. Em uma linguagem direta e enxuta, o livro retrata a luta de uma família retirante por dignidade."
                },
                {
                        "A Moreninha",
                        "Joaquim Manuel de Macedo",
                        "Romance",
                        "O estudante de medicina Augusto aposta com os colegas que nunca vai se apaixonar por uma só mulher. Durante um fim de semana na ilha de Paquetá, ele conhece a alegre Carolina e descobre que o destino tinha outros planos."
                }
        };

        // Percorre a lista e cadastra cada livro
        for (String[] livro : livros) {

            Livro novoLivro = new Livro(
                    0,
                    livro[0],
                    livro[1],
                    livro[2],
                    livro[3],
                    null);

            service.cadastrar(novoLivro);
        }

        System.out.println("Livros de exemplo cadastrados: " + livros.length);
    }

    // Busca uma variável de ambiente ou utiliza o valor padrão
    private static String valor(String nome, String padrao) {

        String valor = System.getenv(nome);

        if (valor == null || valor.isBlank()) {
            return padrao;
        }

        return valor;
    }
}
