package br.com.biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexao {
    private static final String URL = "jdbc:h2:file:./database/biblioteca";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private Conexao() {}

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}