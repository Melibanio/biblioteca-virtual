
}

package br.com.biblioteca.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;

public final class Conexao {

    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private static boolean inicializado = false;

    private Conexao() {
    }

    public static synchronized Connection conectar() throws SQLException {

        Path pastaProjeto = localizarPastaProjeto();
        Path banco = localizarBanco(pastaProjeto);

        try {
            Class.forName("org.h2.Driver");

            if (banco.getParent() != null) {
                Files.createDirectories(banco.getParent());
            }

            String caminho = banco.toAbsolutePath()
                    .normalize()
                    .toString()
                    .replace("\\", "/");

            String url = "jdbc:h2:file:" + caminho;

            Connection conexao = DriverManager.getConnection(
                    url, USUARIO, SENHA);

            if (!inicializado) {

                Path arquivoSQL = pastaProjeto.resolve(
                        "database/biblioteca.sql");

                try (Reader leitor = Files.newBufferedReader(
                        arquivoSQL, StandardCharsets.UTF_8)) {

                    RunScript.execute(conexao, leitor);

                    inicializado = true;

                    System.out.println(
                            "Banco de dados inicializado com sucesso!");

                } catch (IOException | SQLException e) {

                    conexao.close();

                    throw new SQLException(
                            "Erro ao inicializar as tabelas do banco.", e);
                }
            }

            return conexao;

        } catch (ClassNotFoundException e) {

            throw new SQLException(
                    "Driver do H2 não encontrado.", e);

        } catch (IOException e) {

            throw new SQLException(
                    "Não foi possível localizar o arquivo SQL.", e);
        }
    }

    private static Path localizarPastaProjeto() throws SQLException {

        Path atual = Paths.get("")
                .toAbsolutePath()
                .normalize();

        while (atual != null) {

            Path arquivoSQL = atual.resolve(
                    "database/biblioteca.sql");

            if (Files.isRegularFile(arquivoSQL)) {
                return atual;
            }

            atual = atual.getParent();
        }

        throw new SQLException(
                "Não foi possível encontrar database/biblioteca.sql.");
    }

    private static Path localizarBanco(Path pastaProjeto) {

        String caminhoConfigurado = System.getProperty(
                "biblioteca.db.path");

        if (caminhoConfigurado != null
                && !caminhoConfigurado.isBlank()) {

            Path banco = Paths.get(caminhoConfigurado.trim());

            String nome = banco.getFileName().toString();

            if (nome.endsWith(".mv.db")) {
                banco = banco.resolveSibling(
                        nome.substring(0, nome.length() - 6));
            }

            return banco;
        }

        return pastaProjeto.resolve("database/biblioteca");
    }
}
