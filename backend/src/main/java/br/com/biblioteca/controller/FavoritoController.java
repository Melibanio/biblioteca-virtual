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

public class FavoritoController implements HttpHandler {

    private static class DadosFavorito {
        Integer livroId;
    }

    private FavoritoDAO favoritoDAO = new FavoritoDAO();
    private LivroService livroService = new LivroService();
    private Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        try {
            String metodo = ex.getRequestMethod();
            if (metodo.equals("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            Usuario usuario = UsuarioController.usuarioLogado(ex);
            if (usuario == null) {
                erro(ex, 401, "Faça login para continuar.");
                return;
            }

            if (metodo.equals("GET")) {
                listar(ex, usuario);
            } else if (metodo.equals("POST")) {
                adicionar(ex, usuario);
            } else if (metodo.equals("DELETE")) {
                remover(ex, usuario);
            } else {
                erro(ex, 405, "Método não permitido.");
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

    // Lista os livros favoritos do usuário
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

    // Adiciona um livro aos favoritos
    private void adicionar(HttpExchange ex, Usuario usuario) throws Exception {

        String json = corpo(ex);

        DadosFavorito dados = gson.fromJson(json, DadosFavorito.class);

        if (dados == null || dados.livroId == null || dados.livroId <= 0) {
            throw new IllegalArgumentException("Informe o livroId.");
        }

        Livro livro = livroService.buscarPorId(dados.livroId);

        if (livro == null) {
            erro(ex, 404, "Livro não encontrado.");
            return;
        }

        try {
            favoritoDAO.adicionar(usuario.getId(), dados.livroId);

            Map<String, String> resposta = new HashMap<>();
            resposta.put("mensagem", "Livro adicionado aos favoritos.");

            enviar(ex, 201, resposta);

        } catch (SQLException e) {

            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                erro(ex, 409, "Este livro já está nos favoritos.");
            } else {
                throw e;
            }
        }
    }

    // Remove um livro dos favoritos
    private void remover(HttpExchange ex, Usuario usuario) throws Exception {

        String caminho = ex.getRequestURI().getPath();

        String id = caminho.substring("/api/favoritos".length());
        id = id.replace("/", "");

        if (id.isEmpty()) {
            throw new IllegalArgumentException("Informe o id do livro na URL.");
        }

        int livroId;

        try {
            livroId = Integer.parseInt(id);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido.");
        }

        boolean removido = favoritoDAO.remover(usuario.getId(), livroId);

        if (removido) {

            Map<String, String> resposta = new HashMap<>();
            resposta.put("mensagem", "Livro removido dos favoritos.");

            enviar(ex, 200, resposta);

        } else {
            erro(ex, 404, "Favorito não encontrado.");
        }
    }

    // Libera o acesso do frontend à API
    private void liberarCors(HttpExchange ex) {

        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        ex.getResponseHeaders().add(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        ex.getResponseHeaders().add(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization");
    }

    // Lê os dados enviados pelo frontend
    private String corpo(HttpExchange ex) throws IOException {

        return new String(
                ex.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    // Envia a resposta da API
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
