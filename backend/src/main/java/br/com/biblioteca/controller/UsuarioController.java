package br.com.biblioteca.controller;

import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.service.UsuarioService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class UsuarioController implements HttpHandler {

    // Tempo de duração da sessão: 8 horas
    private static final long DURACAO_SESSAO = 8L * 60 * 60 * 1000;

    // Guarda as sessões dos usuários
    private static final Map<String, Sessao> SESSOES = new ConcurrentHashMap<>();

    private static final SecureRandom ALEATORIO = new SecureRandom();

    // Dados da sessão
    private static class Sessao {

        Usuario usuario;
        long expiraEm;

        public Sessao(Usuario usuario, long expiraEm) {
            this.usuario = usuario;
            this.expiraEm = expiraEm;
        }
    }

    // Dados recebidos no cadastro
    private static class DadosCadastro {
        String nome;
        String email;
        String senha;
    }

    // Dados recebidos no login
    private static class DadosLogin {
        String email;
        String senha;
    }

    private UsuarioService service = new UsuarioService();
    private Gson gson = new Gson();

    // Verifica se o usuário está logado
    public static Usuario usuarioLogado(HttpExchange ex) {

        String cabecalho = ex.getRequestHeaders().getFirst("Authorization");

        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }

        String token = cabecalho.substring(7).trim();

        Sessao sessao = SESSOES.get(token);

        if (sessao == null) {
            return null;
        }

        // Verifica se a sessão expirou
        if (sessao.expiraEm < System.currentTimeMillis()) {
            SESSOES.remove(token);
            return null;
        }

        return sessao.usuario;
    }

    // Verifica se o usuário é administrador
    public static boolean ehAdmin(Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        return "ADMIN".equalsIgnoreCase(usuario.getTipo());
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        try {
            String metodo = ex.getRequestMethod();

            if (metodo.equals("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            if (!metodo.equals("POST")) {
                erro(ex, 405, "Método não permitido.");
                return;
            }

            String caminho = ex.getRequestURI().getPath();

            // Remove as barras do final da URL
            while (caminho.endsWith("/") && caminho.length() > 1) {
                caminho = caminho.substring(0, caminho.length() - 1);
            }

            if (caminho.equals("/api/usuarios")) {

                cadastrar(ex);

            } else if (caminho.equals("/api/login")) {

                login(ex);

            } else {
                erro(ex, 404, "Rota não encontrada.");
            }

        } catch (JsonSyntaxException e) {
            erro(ex, 400, "JSON inválido.");

        } catch (IllegalArgumentException e) {
            erro(ex, 400, e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            erro(ex, 500, "Erro interno no servidor.");
        }
    }

    // Cadastra um novo usuário
    private void cadastrar(HttpExchange ex) throws Exception {

        String json = corpo(ex);

        DadosCadastro dados = gson.fromJson(json, DadosCadastro.class);

        if (dados == null) {
            throw new IllegalArgumentException("Dados não enviados.");
        }

        String nome = dados.nome;
        String email = dados.email;

        if (nome != null) {
            nome = nome.trim();
        }

        if (email != null) {
            email = email.trim().toLowerCase();
        }

        // Todo cadastro recebe o perfil de estudante
        service.cadastrar(nome, email, dados.senha);

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Conta criada com sucesso.");

        enviar(ex, 201, resposta);
    }

    // Realiza o login do usuário
    private void login(HttpExchange ex) throws Exception {

        String json = corpo(ex);

        DadosLogin dados = gson.fromJson(json, DadosLogin.class);

        if (dados == null || dados.email == null || dados.senha == null) {
            throw new IllegalArgumentException("Informe e-mail e senha.");
        }

        String email = dados.email.trim().toLowerCase();

        Usuario usuario = service.autenticar(email, dados.senha);

        if (usuario == null) {
            erro(ex, 401, "E-mail ou senha inválidos.");
            return;
        }

        // O hash da senha não será enviado ao frontend
        usuario.setSenhaHash(null);

        // Cria o token de acesso
        String token = novoToken();

        long expiracao = System.currentTimeMillis() + DURACAO_SESSAO;

        Sessao sessao = new Sessao(usuario, expiracao);

        SESSOES.put(token, sessao);

        // Prepara os dados do usuário
        Map<String, Object> dadosUsuario = new HashMap<>();

        dadosUsuario.put("id", usuario.getId());
        dadosUsuario.put("nome", usuario.getNome());
        dadosUsuario.put("email", usuario.getEmail());
        dadosUsuario.put("tipo", usuario.getTipo().toLowerCase());

        // Prepara a resposta do login
        Map<String, Object> resposta = new HashMap<>();

        resposta.put("token", token);
        resposta.put("usuario", dadosUsuario);

        enviar(ex, 200, resposta);
    }

    // Gera um token de acesso
    private String novoToken() {

        byte[] bytes = new byte[32];

        ALEATORIO.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // Libera a comunicação com o frontend
    private void liberarCors(HttpExchange ex) {

        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        ex.getResponseHeaders().add(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        ex.getResponseHeaders().add(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization");
    }

    // Recebe os dados enviados pelo frontend
    private String corpo(HttpExchange ex) throws IOException {

        return new String(
                ex.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    // Envia uma resposta
    private void enviar(HttpExchange ex, int codigo, Object objeto)
            throws IOException {

        String json = gson.toJson(objeto);

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8");

        ex.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(bytes);
        }
    }

    // Envia mensagens de erro
    private void erro(HttpExchange ex, int codigo, String mensagem)
            throws IOException {

        Map<String, String> resposta = new HashMap<>();

        if (mensagem == null) {
            mensagem = "Dados inválidos.";
        }

        resposta.put("mensagem", mensagem);

        enviar(ex, codigo, resposta);
    }
}
