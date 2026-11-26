package br.com.biblioteca.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    private static final String CAMINHO_BANCO = configuracao(
            "biblioteca.db", "BIBLIOTECA_DB", "./dados/biblioteca");

    private static final String URL = "jdbc:h2:file:" + CAMINHO_BANCO
            + ";DB_CLOSE_ON_EXIT=FALSE";

    private static boolean inicializado = false;

    private Conexao() {
    }

    public static Connection conectar() throws SQLException {

        Connection con = DriverManager.getConnection(URL, "sa", "");
        inicializar(con);
        return con;
    }

    // Executa o schema.sql uma única vez por execução da aplicação.
    private static synchronized void inicializar(Connection con)
            throws SQLException {

        if (inicializado) {
            return;
        }

        if (Conexao.class.getResource("/schema.sql") == null) {
            throw new SQLException(
                    "schema.sql não encontrado em src/main/resources.");
        }

        try (Statement st = con.createStatement()) {
            st.execute("RUNSCRIPT FROM 'classpath:/schema.sql' CHARSET 'UTF-8'");
        }

        inicializado = true;
    }

    private static String configuracao(
            String propriedade, String variavel, String padrao) {

        String valor = System.getProperty(propriedade);

        if (valor == null || valor.isBlank()) {
            valor = System.getenv(variavel);
        }

        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }
}
