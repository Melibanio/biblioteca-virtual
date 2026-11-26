package br.com.biblioteca.controller;

import br.com.biblioteca.dao.FavoritoDAO;
import br.com.biblioteca.model.Favorito;
import br.com.biblioteca.model.Livro;
import br.com.biblioteca.model.Usuario;
import br.com.biblioteca.service.LivroService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FavoritoController implements HttpHandler {

    private static class DadosFavorito {
        Integer livroId;
    }

    private final FavoritoDAO favoritoDAO = new FavoritoDAO();
    private final LivroService livroService = new LivroService();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        try {
            Usuario usuario = UsuarioController.usuarioLogado(ex);

            if (usuario == null) {
                erro(ex, 401, "Faça login para continuar.");
                return;
            }

            switch (ex.getRequestMethod()) {
                case "GET":
                    listar(ex, usuario);
                    break;
                case "POST":
                    adicionar(ex, usuario);
                    break;
                case "DELETE":
                    remover(ex, usuario);
                    break;
                default:
                    ex.getResponseHeaders().set(
                            "Allow", "GET, POST, DELETE, OPTIONS");
                    erro(ex, 405, "Método não permitido.");
            }

        } catch (JsonSyntaxException e) {
            erro(ex, 400, "JSON inválido.");
        } catch (IllegalArgumentException e) {
            erro(ex, 400, e.getMessage());
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                erro(ex, 409, "Este livro já está nos favoritos ou não existe.");
            } else {
                e.printStackTrace();
                erro(ex, 500, "Erro ao acessar os favoritos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            erro(ex, 500, "Erro interno no servidor.");
        }
    }

    private void listar(HttpExchange ex, Usuario usuario) throws Exception {

        List<Livro> livros = new ArrayList<>();
        List<Favorito> favoritos = favoritoDAO.listarPorUsuario(usuario.getId());

        for (Favorito favorito : favoritos) {
            Livro livro = livroService.buscarPorId(favorito.getLivroId());
            if (livro != null) {
                livros.add(livro);
            }
        }

        enviar(ex, 200, livros);
    }

    private void adicionar(HttpExchange ex, Usuario usuario) throws Exception {

        DadosFavorito dados = gson.fromJson(corpo(ex), DadosFavorito.class);

        if (dados == null || dados.livroId == null || dados.livroId <= 0) {
            throw new IllegalArgumentException("Informe o livroId.");
        }

        Livro livro = livroService.buscarPorId(dados.livroId);

        if (livro == null) {
            erro(ex, 404, "Livro não encontrado.");
            return;
        }

        favoritoDAO.adicionar(usuario.getId(), dados.livroId);

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Livro adicionado aos favoritos.");
        enviar(ex, 201, resposta);
    }

    private void remover(HttpExchange ex, Usuario usuario) throws Exception {

        String caminho = ex.getRequestURI().getPath();
        String textoId = caminho.substring("/api/favoritos".length()).replace("/", "");

        if (textoId.isBlank()) {
            throw new IllegalArgumentException("Informe o ID do livro na URL.");
        }

        int livroId = lerId(textoId);
        boolean removido = favoritoDAO.remover(usuario.getId(), livroId);

        if (removido) {
            Map<String, String> resposta = new HashMap<>();
            resposta.put("mensagem", "Livro removido dos favoritos.");
            enviar(ex, 200, resposta);
        } else {
            erro(ex, 404, "Favorito não encontrado.");
        }
    }

    private int lerId(String texto) {
        try {
            int id = Integer.parseInt(texto);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido.");
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
            ex.getResponseHeaders().set(
                    "Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            ex.getResponseHeaders().set(
                    "Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
    }

    private String corpo(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", texto == null ? "Dados inválidos." : texto);
        enviar(ex, codigo, resposta);
    }
}
