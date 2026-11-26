package br.com.biblioteca.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexao {

    private static final String USUARIO = "sa";
    private static final String SENHA = "";
    private Conexao() {}

    public static Connection conectar() throws SQLException {

        Path banco = localizarBanco();

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do H2 não encontrado. Confira a dependência no pom.xml.", e);
        }

        String caminho = banco.toAbsolutePath().normalize().toString().replace("\\", "/");
        String url = "jdbc:h2:file:" + caminho + ";IFEXISTS=TRUE";

        return DriverManager.getConnection(url, USUARIO, SENHA);
    }

    private static Path localizarBanco() throws SQLException {

        String caminhoConfigurado = System.getProperty("biblioteca.db.path");

        if (caminhoConfigurado != null && !caminhoConfigurado.isBlank()) {
            Path banco = Paths.get(caminhoConfigurado.trim());
            String nome = banco.getFileName().toString();

            if (nome.endsWith(".mv.db")) {
                banco = banco.resolveSibling(nome.substring(0, nome.length() - 6));
            }

            if (Files.isRegularFile(Paths.get(banco.toString() + ".mv.db"))) {
                return banco;
            }

            throw new SQLException("O banco informado em biblioteca.db.path não foi encontrado.");
        }

        Path pastaAtual = Paths.get("").toAbsolutePath().normalize();

        while (pastaAtual != null) {

            Path banco = pastaAtual.resolve("database").resolve("biblioteca");
            Path arquivoBanco = Paths.get(banco.toString() + ".mv.db");

            if (Files.isRegularFile(arquivoBanco)) {
                return banco;
            }

            pastaAtual = pastaAtual.getParent();
        }

        throw new SQLException(
                "Não foi encontrado database/biblioteca.mv.db. "
                + "Abra o projeto pela pasta correta ou confira se o arquivo do banco está presente.");
    }
}
