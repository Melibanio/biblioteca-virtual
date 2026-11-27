package br.com.biblioteca;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.controller.FavoritoController;
import br.com.biblioteca.controller.LivroController;
import br.com.biblioteca.controller.NewsletterController;
import br.com.biblioteca.controller.UsuarioController;
import br.com.biblioteca.service.LivroService;
import br.com.biblioteca.service.UsuarioService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORTA = Integer.getInteger("biblioteca.porta", 8080);

    public static void main(String[] args) {

        try {
            Path pastaWeb = localizarPastaWeb();

            // Confere o banco antes de iniciar o servidor.
            try (Connection conexao = Conexao.conectar();
                    Statement consulta = conexao.createStatement()) {
                consulta.execute("SELECT 1");
            }

            UsuarioService usuarioService = new UsuarioService();
            usuarioService.configurarAdministradorPadrao();
            new LivroService().popularAcervoInicial();

            HttpServer servidor = HttpServer.create(
                    new InetSocketAddress(
                            InetAddress.getByName("127.0.0.1"), PORTA), 0);

            UsuarioController usuarioController = new UsuarioController();
            LivroController livroController = new LivroController();

            servidor.createContext("/api/status", new StatusController());
            servidor.createContext("/api/livros", livroController);
            servidor.createContext("/api/leitura", livroController);
            servidor.createContext("/api/usuarios", usuarioController);
            servidor.createContext("/api/login", usuarioController);
            servidor.createContext("/api/logout", usuarioController);
            servidor.createContext("/api/recuperar-senha", usuarioController);
            servidor.createContext("/api/favoritos", new FavoritoController());
            servidor.createContext("/api/newsletter", new NewsletterController());
            servidor.createContext("/", new ArquivosController(pastaWeb));

            ExecutorService executor = Executors.newFixedThreadPool(10);
            servidor.setExecutor(executor);
            servidor.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                servidor.stop(0);
                executor.shutdown();
            }));

            System.out.println("Biblioteca Virtual iniciada com sucesso.");
            System.out.println("Servidor: http://localhost:" + PORTA);
            System.out.println("API: http://localhost:" + PORTA + "/api/status");
            System.out.println("Frontend: " + pastaWeb.toAbsolutePath());
            System.out.println("Banco H2 conectado.");
            System.out.println("Conta administrativa conferida.");

        } catch (Exception e) {
            System.out.println("Não foi possível iniciar a Biblioteca Virtual.");
            System.out.println("Confira a porta e a conexão com o banco H2.");
            e.printStackTrace();
        }
    }

    private static Path localizarPastaWeb() throws IOException {

        Path atual = Paths.get("").toAbsolutePath().normalize();

        while (atual != null) {
            Path pastaBackend = atual.resolve("src/main/webapp");

            if (Files.isRegularFile(pastaBackend.resolve("index.html"))) {
                return pastaBackend.toRealPath();
            }

            Path pastaProjeto = atual.resolve("backend/src/main/webapp");

            if (Files.isRegularFile(pastaProjeto.resolve("index.html"))) {
                return pastaProjeto.toRealPath();
            }

            atual = atual.getParent();
        }

        throw new IOException(
                "Não foi possível localizar backend/src/main/webapp/index.html.");
    }

    private static class StatusController implements HttpHandler {

        @Override
        public void handle(HttpExchange ex) throws IOException {

            liberarCors(ex);

            if (ex.getRequestMethod().equals("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            if (!ex.getRequestMethod().equals("GET")) {
                enviar(ex, 405, "{\"mensagem\":\"Método não permitido.\"}");
                return;
            }

            try (Connection conexao = Conexao.conectar();
                    Statement consulta = conexao.createStatement()) {

                String[] tabelas = {
                    "livro", "usuario", "favorito", "historico_leitura"
                };

                for (String tabela : tabelas) {
                    try (ResultSet rs = consulta.executeQuery(
                            "SELECT COUNT(*) FROM " + tabela)) {
                        rs.next();
                    }
                }

                enviar(ex, 200,
                        "{\"status\":\"online\",\"banco\":\"conectado\"}");

            } catch (Exception e) {
                e.printStackTrace();
                enviar(ex, 503,
                        "{\"status\":\"erro\",\"mensagem\":\"Não foi possível conectar ao banco H2.\"}");
            }
        }

        private void liberarCors(HttpExchange ex) {

            String origem = ex.getRequestHeaders().getFirst("Origin");
            Set<String> permitidas = Set.of(
                    "http://localhost:8080", "http://127.0.0.1:8080",
                    "http://localhost:5500", "http://127.0.0.1:5500");

            if (origem != null && permitidas.contains(origem)) {
                ex.getResponseHeaders().set("Access-Control-Allow-Origin", origem);
                ex.getResponseHeaders().set("Vary", "Origin");
                ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
                ex.getResponseHeaders().set("Access-Control-Allow-Headers",
                        "Content-Type, Authorization");
            }
        }

        private void enviar(HttpExchange ex, int codigo, String resposta)
                throws IOException {

            byte[] dados = resposta.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8);
            ex.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=UTF-8");
            ex.sendResponseHeaders(codigo, dados.length);

            try (OutputStream saida = ex.getResponseBody()) {
                saida.write(dados);
            }
        }
    }

    private static class ArquivosController implements HttpHandler {

        private final Path pastaWeb;

        public ArquivosController(Path pastaWeb) {
            this.pastaWeb = pastaWeb;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {

            String metodo = ex.getRequestMethod();

            switch (metodo) {
                case "GET":
                case "HEAD":
                    servirArquivo(ex, metodo.equals("HEAD"));
                    break;
                default:
                    ex.getResponseHeaders().set("Allow", "GET, HEAD");
                    enviarTexto(ex, 405, "Método não permitido.");
            }
        }

        private void servirArquivo(HttpExchange ex, boolean somenteCabecalho)
                throws IOException {

            String caminho = ex.getRequestURI().getPath();

            if (caminho == null || caminho.equals("/")) {
                caminho = "/index.html";
            }

            if (caminho.startsWith("/WEB-INF") || caminho.contains("\\")) {
                enviarTexto(ex, 404, "Página não encontrada.");
                return;
            }

            Path arquivo = pastaWeb.resolve(caminho.substring(1)).normalize();

            if (!arquivo.startsWith(pastaWeb) || !Files.isRegularFile(arquivo)) {
                enviarTexto(ex, 404, "Página não encontrada.");
                return;
            }

            ex.getResponseHeaders().set("Content-Type", tipoConteudo(arquivo));
            ex.getResponseHeaders().set("Cache-Control", "no-cache");
            ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");

            long tamanho = Files.size(arquivo);

            if (somenteCabecalho) {
                ex.getResponseHeaders().set("Content-Length", String.valueOf(tamanho));
                ex.sendResponseHeaders(200, -1);
                return;
            }

            ex.sendResponseHeaders(200, tamanho);

            try (OutputStream saida = ex.getResponseBody()) {
                Files.copy(arquivo, saida);
            }
        }

        private String tipoConteudo(Path arquivo) {

            String nome = arquivo.getFileName().toString().toLowerCase(Locale.ROOT);
            int ponto = nome.lastIndexOf('.');
            String extensao = ponto >= 0 ? nome.substring(ponto + 1) : "";

            switch (extensao) {
                case "html": return "text/html; charset=UTF-8";
                case "css": return "text/css; charset=UTF-8";
                case "js": return "application/javascript; charset=UTF-8";
                case "json": return "application/json; charset=UTF-8";
                case "png": return "image/png";
                case "jpg":
                case "jpeg": return "image/jpeg";
                case "gif": return "image/gif";
                case "svg": return "image/svg+xml";
                case "webp": return "image/webp";
                case "ico": return "image/x-icon";
                case "pdf": return "application/pdf";
                default: return "application/octet-stream";
            }
        }

        private void enviarTexto(HttpExchange ex, int codigo, String texto)
                throws IOException {

            byte[] dados = texto.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8);
            ex.getResponseHeaders().set(
                    "Content-Type", "text/plain; charset=UTF-8");
            ex.sendResponseHeaders(codigo, dados.length);

            try (OutputStream saida = ex.getResponseBody()) {
                saida.write(dados);
            }
        }
    }
}
