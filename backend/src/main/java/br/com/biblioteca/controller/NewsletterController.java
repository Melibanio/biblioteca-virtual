package br.com.biblioteca.controller;

import br.com.biblioteca.config.Conexao;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NewsletterController implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        liberarCors(ex);

        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        if (!ex.getRequestMethod().equals("POST")) {
            ex.getResponseHeaders().set("Allow", "POST, OPTIONS");
            enviar(ex, 405, "Método não permitido.");
            return;
        }

        try {
            JsonObject dados = gson.fromJson(corpo(ex), JsonObject.class);

            String email = campo(dados, "email").trim().toLowerCase(Locale.ROOT);
            String nome = campo(dados, "nome").trim();

            if (email.isEmpty() || email.length() > 120
                    || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                enviar(ex, 400, "Informe um e-mail válido.");
                return;
            }

            if (nome.length() > 100) {
                enviar(ex, 400, "O nome deve ter até 100 caracteres.");
                return;
            }

            if (assinar(nome, email)) {
                enviar(ex, 201, "Assinatura realizada! Você receberá o Jornal da Biblioteca.");
            } else {
                enviar(ex, 200, "Este e-mail já está inscrito no Jornal da Biblioteca.");
            }

        } catch (JsonSyntaxException e) {
            enviar(ex, 400, "JSON inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            enviar(ex, 500, "Erro ao registrar a assinatura.");
        }
    }

    // Retorna false se o e-mail já estava cadastrado.
    private boolean assinar(String nome, String email) throws SQLException {

        try (Connection con = Conexao.conectar()) {

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM newsletter WHERE email = ?")) {
                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO newsletter (nome, email) VALUES (?, ?)")) {
                ps.setString(1, nome.isEmpty() ? null : nome);
                ps.setString(2, email);
                ps.executeUpdate();
                return true;

            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                    return false;
                }
                throw e;
            }
        }
    }

    private String campo(JsonObject dados, String nome) {

        if (dados == null || !dados.has(nome) || dados.get(nome).isJsonNull()
                || !dados.get(nome).isJsonPrimitive()) {
            return "";
        }

        return dados.get(nome).getAsString();
    }

    private void liberarCors(HttpExchange ex) {

        String origem = ex.getRequestHeaders().getFirst("Origin");
        Set<String> permitidas = Set.of(
                "http://localhost:8080", "http://127.0.0.1:8080",
                "http://localhost:5500", "http://127.0.0.1:5500");

        if (origem != null && permitidas.contains(origem)) {
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", origem);
            ex.getResponseHeaders().set("Vary", "Origin");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
    }

    private String corpo(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void enviar(HttpExchange ex, int codigo, String texto) throws IOException {

        byte[] bytes = gson.toJson(Map.of("mensagem", texto)).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(bytes);
        }
    }
}
