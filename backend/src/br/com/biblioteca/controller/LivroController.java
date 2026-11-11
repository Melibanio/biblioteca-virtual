<<<<<<< HEAD
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

public class LivroController implements HttpHandler {
    private final LivroService service = new LivroService();
    private final Gson gson = new Gson();
    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        try {
            String metodo = ex.getRequestMethod();
            // Trata as requisições OPTIONS
            if (metodo.equals("OPTIONS")) {
                ex.sendResponseHeaders(204, -1);
                return;
            }

            String caminho = ex.getRequestURI().getPath();
            String resto = caminho.substring("/api/livros".length());
            resto = resto.replace("/", "");
            Integer id = null;
            if (!resto.isEmpty()) {
                id = lerId(resto);
            }

            // Consulta os livros
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

            // Verifica se o usuário está logado
            Usuario usuario = UsuarioController.usuarioLogado(ex);
            if (usuario == null) {
                erro(ex, 401, "Faça login para continuar.");
                return;
            }
            // Verifica se o usuário é administrador
            if (!UsuarioController.ehAdmin(usuario)) {
                erro(ex, 403, "Acesso restrito ao administrador.");
                return;
            }
            // Cadastra um livro
            if (metodo.equals("POST")) {
                String dados = corpo(ex);
                Livro livro = gson.fromJson(dados, Livro.class);
                if (livro == null) {
                    throw new IllegalArgumentException("Dados do livro não enviados.");
                }
                service.cadastrar(livro);
                Map<String, String> resposta = new HashMap<>();
                resposta.put("mensagem", "Livro cadastrado com sucesso.");
                enviar(ex, 201, resposta);
                // Atualiza um livro
            } else if (metodo.equals("PUT")) {

                if (id == null) {
                    throw new IllegalArgumentException("Informe o id do livro na URL.");
                }

                Livro livro = gson.fromJson(corpo(ex), Livro.class);

                if (livro == null) {
                    throw new IllegalArgumentException("Dados do livro não enviados.");
                }

                livro.setId(id);

                if (service.atualizar(livro)) {

                    Map<String, String> resposta = new HashMap<>();
                    resposta.put("mensagem", "Livro atualizado com sucesso.");

                    enviar(ex, 200, resposta);

                } else {
                    erro(ex, 404, "Livro não encontrado.");
                }

                // Exclui um livro
            } else if (metodo.equals("DELETE")) {

                if (id == null) {
                    throw new IllegalArgumentException("Informe o id do livro na URL.");
                }

                excluir(ex, id);

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

    // Exclui um livro
    private void excluir(HttpExchange ex, int id) throws Exception {

        try {
            boolean excluido = service.excluir(id);
            if (excluido) {
                Map<String, String> resposta = new HashMap<>();
                resposta.put("mensagem", "Livro excluído com sucesso.");
                enviar(ex, 200, resposta);
            } else {
                erro(ex, 404, "Livro não encontrado.");
            }
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                erro(ex, 409,
        "Não é possível excluir: o livro está nos favoritos de algum usuário.");
            } else {
                throw e;
            }
        }
    }

    // Converte o ID para número
    private int lerId(String texto) {

        try {
            return Integer.parseInt(texto);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID inválido.");
        }
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
=======
public u {
    
>>>>>>> 66d42127ecf7fa6c05a94acc7ba45d697b57f7d7
}
