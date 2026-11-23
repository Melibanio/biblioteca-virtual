package br.com.biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.h2.tools.RunScript;

public final class Conexao {

    private static final String URL = "jdbc:h2:file:./database/biblioteca";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private static boolean inicializado = false;

    private Conexao() {}

    public static synchronized Connection conectar() throws SQLException {

        Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA);

        if (!inicializado) {

            InputStream arquivo = Conexao.class
                    .getClassLoader()
                    .getResourceAsStream("biblioteca.sql");

            if (arquivo == null) {
                conexao.close();
                throw new SQLException("Arquivo biblioteca.sql não encontrado!");
            }

            try (
                InputStream entrada = arquivo;
                InputStreamReader leitor = new InputStreamReader(
                    entrada, StandardCharsets.UTF_8
                )
            ) {

                RunScript.execute(conexao, leitor);
                inicializado = true;

                System.out.println("Banco de dados inicializado!");

            } catch (IOException | SQLException e) {

                conexao.close();
                throw new SQLException("Erro ao inicializar o banco.", e);
            }
        }

        return conexao;
    }
}
