package br.com.biblioteca.controller;

import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.service.UsuarioService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioController implements HttpHandler {

    private static final long DURACAO_SESSAO = 8L * 60 * 60 * 1000;
    private static final long DURACAO_CODIGO = 10L * 60 * 1000;
    private static final int MAX_TENTATIVAS = 5;
    private static final Map<String, Sessao> SESSOES = new ConcurrentHashMap<>();
    private static final Map<String, Recuperacao> RECUPERACOES = new ConcurrentHashMap<>();
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private final UsuarioService service = new UsuarioService();
    private final Gson gson = new Gson();
    private static class Sessao {
        Usuario usuario;
        long expiraEm;

        Sessao(Usuario usuario, long expiraEm) {
            this.usuario = usuario;
            this.expiraEm = expiraEm;
        }
    }

    private static class Recuperacao {
        String codigoHash;
        long expiraEm;
        int tentativas;

        Recuperacao(String hash, long expiraEm) {
            this.codigoHash = hash;
            this.expiraEm = expiraEm;
        }
    }

    public static Usuario usuarioLogado(HttpExchange ex) {
        Sessao sessao = SESSOES.get(token(ex));

        if (sessao == null) return null;

        if (sessao.expiraEm <= System.currentTimeMillis()) {
            SESSOES.remove(token(ex), sessao);
            return null;
        }

        return sessao.usuario;
    }

    public static boolean ehAdmin(Usuario usuario) {
        return usuario != null
                && "ADMIN".equalsIgnoreCase(usuario.getTipo());
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String caminho = ex.getRequestURI().getPath();

        try {
            switch (caminho) {
                case "/api/usuarios":
                    if (metodo(ex, "POST", "POST, OPTIONS")) cadastrar(ex);
                    break;

                case "/api/login":
                    if (metodo(ex, "POST", "POST, OPTIONS")) login(ex);
                    break;

                case "/api/logout":
                    if (metodo(ex, "POST", "POST, OPTIONS")) logout(ex);
                    break;

                case "/api/usuarios/perfil":
                    perfil(ex);
                    break;

                case "/api/recuperar-senha":
                    if (metodo(ex, "POST", "POST, OPTIONS")) recuperarSenha(ex);
                    break;

                case "/api/recuperar-senha/confirmar":
                    if (metodo(ex, "POST", "POST, OPTIONS")) confirmarSenha(ex);
                    break;

                default:
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

    // Cadastro único
    private void cadastrar(HttpExchange ex) throws Exception {
        JsonObject dados = lerJson(ex);

        service.cadastrar(
                campo(dados, "nome"),
                campo(dados, "email"),
                campo(dados, "senha"));

        enviar(ex, 201, mensagem("Conta criada com sucesso."));
    }

    // Login
    private void login(HttpExchange ex) throws Exception {
        JsonObject dados = lerJson(ex);

        Usuario usuario = service.autenticar(
                campo(dados, "email"), campo(dados, "senha"));

        if (usuario == null) {
            erro(ex, 401, "E-mail ou senha inválidos.");
            return;
        }

        Usuario seguro = new Usuario(
                usuario.getId(), usuario.getNome(),
                usuario.getEmail(), null, usuario.getTipo());

        String novoToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(tokenAleatorio());

        SESSOES.put(novoToken, new Sessao(
                seguro, System.currentTimeMillis() + DURACAO_SESSAO));

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("token", novoToken);
        resposta.put("usuario", dadosPublicos(seguro));

        enviar(ex, 200, resposta);
    }

    // Encerrar sessão
    private void logout(HttpExchange ex) throws IOException {
        if (SESSOES.remove(token(ex)) == null) {
            erro(ex, 401, "Sessão não encontrada ou já encerrada.");
            return;
        }

        enviar(ex, 200, mensagem("Sessão encerrada."));
    }

    // Consultar e alterar perfil
    private void perfil(HttpExchange ex) throws Exception {
        Usuario sessao = usuarioLogado(ex);

        if (sessao == null) {
            erro(ex, 401, "Faça login para acessar seu perfil.");
            return;
        }

        String metodoAtual = ex.getRequestMethod();

        if (metodoAtual.equals("GET")) {
            Usuario usuario = service.buscarPorId(sessao.getId());

            if (usuario == null) {
                erro(ex, 404, "Usuário não encontrado.");
            } else {
                enviar(ex, 200, dadosPublicos(usuario));
            }

        } else if (metodoAtual.equals("PUT")) {
            JsonObject dados = lerJson(ex);

            Usuario atualizado = service.atualizarPerfil(
                    sessao.getId(),
                    campo(dados, "nome"),
                    campo(dados, "email"));

            atualizarSessoes(atualizado);
            enviar(ex, 200, dadosPublicos(atualizado));

        } else {
            metodoNaoPermitido(ex, "GET, PUT, OPTIONS");
        }
    }

    // Solicitar recuperação de senha
    private void recuperarSenha(HttpExchange ex) throws Exception {
        String email = normalizarEmail(campo(lerJson(ex), "email"));

        if (email.isBlank()) {
            throw new IllegalArgumentException("Informe o e-mail.");
        }

        limparRecuperacoes();

        if (service.emailCadastrado(email)) {
            String codigo = String.format(
                    Locale.ROOT, "%06d", ALEATORIO.nextInt(1000000));

            RECUPERACOES.put(email, new Recuperacao(
                    BCrypt.hashpw(codigo, BCrypt.gensalt()),
                    System.currentTimeMillis() + DURACAO_CODIGO));

            System.out.println(
                    "Código de recuperação (teste local): " + codigo);
        }

        enviar(ex, 200, mensagem(
                "Se o e-mail estiver cadastrado, um código será disponibilizado."));
    }

    // Confirmar código e alterar senha
    private void confirmarSenha(HttpExchange ex) throws Exception {
        JsonObject dados = lerJson(ex);

        String email = normalizarEmail(campo(dados, "email"));
        String codigo = campo(dados, "codigo");
        String novaSenha = campo(dados, "novaSenha");

        Recuperacao rec = RECUPERACOES.get(email);

        if (rec == null || rec.expiraEm <= System.currentTimeMillis()) {
            RECUPERACOES.remove(email);
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }

        synchronized (rec) {
            if (++rec.tentativas > MAX_TENTATIVAS) {
                RECUPERACOES.remove(email);
                throw new IllegalArgumentException(
                        "Limite de tentativas atingido.");
            }

            if (codigo == null || !BCrypt.checkpw(codigo.trim(), rec.codigoHash)) {
                throw new IllegalArgumentException("Código inválido ou expirado.");
            }

            Usuario usuario = service.buscarPorEmail(email);

            if (usuario == null) {
                RECUPERACOES.remove(email);
                throw new IllegalArgumentException("Conta não encontrada.");
            }

            service.atualizarSenha(usuario.getId(), novaSenha);
            RECUPERACOES.remove(email);

            SESSOES.entrySet().removeIf(
                    item -> item.getValue().usuario.getId() == usuario.getId());
        }

        enviar(ex, 200, mensagem("Senha atualizada. Faça login novamente."));
    }

    // Atualizar os dados nas sessões
    private void atualizarSessoes(Usuario usuario) {
        for (Sessao sessao : SESSOES.values()) {
            if (sessao.usuario.getId() == usuario.getId()) {
                sessao.usuario.setNome(usuario.getNome());
                sessao.usuario.setEmail(usuario.getEmail());
            }
        }
    }

    private void limparRecuperacoes() {
        long agora = System.currentTimeMillis();

        RECUPERACOES.entrySet().removeIf(
                item -> item.getValue().expiraEm <= agora);
    }

    // Preparar dados públicos do usuário
    private Map<String, Object> dadosPublicos(Usuario usuario) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("id", usuario.getId());
        dados.put("nome", usuario.getNome());
        dados.put("email", usuario.getEmail());
        dados.put("tipo", usuario.getTipo().toLowerCase(Locale.ROOT));

        return dados;
    }

    private JsonObject lerJson(HttpExchange ex) throws IOException {
        return gson.fromJson(corpo(ex), JsonObject.class);
    }

    private String campo(JsonObject dados, String nome) {
        if (dados == null || !dados.has(nome)
                || dados.get(nome).isJsonNull()
                || !dados.get(nome).isJsonPrimitive()
                || !dados.get(nome).getAsJsonPrimitive().isString()) {
            return null;
        }

        return dados.get(nome).getAsString();
    }

    private Map<String, String> mensagem(String texto) {
        return Map.of("mensagem", texto);
    }

    private byte[] tokenAleatorio() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return bytes;
    }

    private static String token(HttpExchange ex) {
        String cabecalho = ex.getRequestHeaders().getFirst("Authorization");

        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) return "";

        return cabecalho.substring(7).trim();
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean metodo(
            HttpExchange ex, String esperado, String permitidos)
            throws IOException {
        if (ex.getRequestMethod().equals(esperado)) return true;

        metodoNaoPermitido(ex, permitidos);
        return false;
    }

    private void liberarCors(HttpExchange ex) {
        String origem = ex.getRequestHeaders().getFirst("Origin");

        Set<String> permitidas = Set.of(
                "http://localhost:8080",
                "http://127.0.0.1:8080",
                "http://localhost:5500",
                "http://127.0.0.1:5500");

        if (origem != null && permitidas.contains(origem)) {
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", origem);
            ex.getResponseHeaders().set("Vary", "Origin");
            ex.getResponseHeaders().set(
                    "Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            ex.getResponseHeaders().set(
                    "Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
    }

    private void metodoNaoPermitido(
            HttpExchange ex, String permitidos) throws IOException {
        ex.getResponseHeaders().set("Allow", permitidos);
        erro(ex, 405, "Método não permitido.");
    }

    private String corpo(HttpExchange ex) throws IOException {
        return new String(
                ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void enviar(HttpExchange ex, int codigo, Object objeto)
            throws IOException {
        byte[] bytes = gson.toJson(objeto).getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private void erro(HttpExchange ex, int codigo, String texto)
            throws IOException {
        enviar(ex, codigo, mensagem(
                texto == null ? "Dados inválidos." : texto));
    }
}
