package br.com.biblioteca.controller;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LivroController implements HttpHandler {

    private final LivroService service = new LivroService();
    private final Gson gson = new Gson();

    private static class DadosLeitura {
        Integer livroId;
        Integer progresso;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String caminho = ex.getRequestURI().getPath();
        String metodo = ex.getRequestMethod();

        try {
            boolean leitura = caminho.equals("/api/leitura")
                    || caminho.startsWith("/api/leitura/");

            String base = leitura ? "/api/leitura" : "/api/livros";

            if (!caminho.equals(base) && !caminho.startsWith(base + "/")) {
                erro(ex, 404, "Rota não encontrada.");
                return;
            }

            String resto = caminho.substring(base.length()).replace("/", "");
            Integer id = resto.isEmpty() ? null : lerId(resto);

            if (leitura) {
                tratarLeitura(ex, metodo, id);
            } else {
                tratarLivros(ex, metodo, id);
            }

        } catch (JsonSyntaxException e) {
            erro(ex, 400, "JSON inválido.");

        } catch (IllegalArgumentException e) {
            erro(ex, 400, e.getMessage());

        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                erro(ex, 409, "Existem registros relacionados a este livro.");
            } else {
                e.printStackTrace();
                erro(ex, 500, "Erro ao acessar o banco de dados.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            erro(ex, 500, "Erro interno no servidor.");
        }
    }

    // Operações dos livros

    private void tratarLivros(HttpExchange ex, String metodo, Integer id)
            throws Exception {

        if (metodo.equals("GET")) {

            if (id == null) {
                enviar(ex, 200, service.listar());
            } else {
                Livro livro = service.buscarPorId(id);

                if (livro == null) {
                    erro(ex, 404, "Livro não encontrado.");
                } else {
                    enviar(ex, 200, livro);
                }
            }
            return;
        }

        Usuario usuario = UsuarioController.usuarioLogado(ex);

        if (usuario == null) {
            erro(ex, 401, "Faça login para continuar.");
            return;
        }

        if (!UsuarioController.ehAdmin(usuario)) {
            erro(ex, 403, "Acesso restrito ao administrador.");
            return;
        }

        switch (metodo) {
            case "POST":
                if (id != null) {
                    throw new IllegalArgumentException(
                            "Utilize a rota principal para cadastrar.");
                }
                cadastrar(ex);
                break;

            case "PUT":
                if (id == null) {
                    throw new IllegalArgumentException(
                            "Informe o ID do livro.");
                }
                atualizar(ex, id);
                break;

            case "DELETE":
                if (id == null) {
                    throw new IllegalArgumentException(
                            "Informe o ID do livro.");
                }
                excluir(ex, id);
                break;

            default:
                ex.getResponseHeaders().set(
                        "Allow", "GET, POST, PUT, DELETE, OPTIONS");
                erro(ex, 405, "Método não permitido.");
        }
    }

    private void cadastrar(HttpExchange ex) throws Exception {

        Livro livro = gson.fromJson(corpo(ex), Livro.class);

        if (livro == null) {
            throw new IllegalArgumentException("Informe os dados do livro.");
        }

        service.cadastrar(livro);
        enviar(ex, 201, mensagem("Livro cadastrado com sucesso."));
    }

    private void atualizar(HttpExchange ex, int id) throws Exception {

        Livro livro = gson.fromJson(corpo(ex), Livro.class);

        if (livro == null) {
            throw new IllegalArgumentException("Informe os dados do livro.");
        }

        livro.setId(id);

        if (service.atualizar(livro)) {
            enviar(ex, 200, mensagem("Livro atualizado com sucesso."));
        } else {
            erro(ex, 404, "Livro não encontrado.");
        }
    }

    private void excluir(HttpExchange ex, int id) throws Exception {

        if (service.excluir(id)) {
            enviar(ex, 200, mensagem("Livro excluído com sucesso."));
        } else {
            erro(ex, 404, "Livro não encontrado.");
        }
    }

    // Histórico de leitura

    private void tratarLeitura(HttpExchange ex, String metodo, Integer id)
            throws Exception {

        Usuario usuario = UsuarioController.usuarioLogado(ex);

        if (usuario == null) {
            erro(ex, 401, "Faça login para acompanhar sua leitura.");
            return;
        }

        switch (metodo) {
            case "GET":
                if (id == null) {
                    enviar(ex, 200, service.listarHistorico(usuario.getId()));
                } else {
                    Map<String, Object> historico =
                            service.buscarHistorico(usuario.getId(), id);

                    if (historico == null) {
                        erro(ex, 404, "Histórico não encontrado.");
                    } else {
                        enviar(ex, 200, historico);
                    }
                }
                break;

            case "POST":
                DadosLeitura inicio = gson.fromJson(
                        corpo(ex), DadosLeitura.class);

                if (id != null || inicio == null || inicio.livroId == null) {
                    throw new IllegalArgumentException(
                            "Informe um livro válido.");
                }

                enviar(ex, 201, service.iniciarLeitura(
                        usuario.getId(), inicio.livroId));
                break;

            case "PUT":
                DadosLeitura dados = gson.fromJson(
                        corpo(ex), DadosLeitura.class);

                if (id == null || dados == null || dados.progresso == null) {
                    throw new IllegalArgumentException(
                            "Informe o livro e o progresso.");
                }

                enviar(ex, 200, service.atualizarProgresso(
                        usuario.getId(), id, dados.progresso));
                break;

            default:
                ex.getResponseHeaders().set(
                        "Allow", "GET, POST, PUT, OPTIONS");
                erro(ex, 405, "Método não permitido.");
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


    private Map<String, String> mensagem(String texto) {
        return Map.of("mensagem", texto);
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
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
            ex.getResponseHeaders().set(
                    "Access-Control-Allow-Headers",
                    "Content-Type, Authorization");
        }
    }

    private String corpo(HttpExchange ex) throws IOException {
        return new String(
                ex.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    private void enviar(HttpExchange ex, int codigo, Object objeto)
            throws IOException {

        byte[] dados = gson.toJson(objeto)
                .getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        ex.sendResponseHeaders(codigo, dados.length);

        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(dados);
        }
    }

    private void erro(HttpExchange ex, int codigo, String texto)
            throws IOException {

        enviar(ex, codigo, mensagem(texto));
    }
}
