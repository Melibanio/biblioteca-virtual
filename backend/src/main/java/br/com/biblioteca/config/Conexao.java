package br.com.biblioteca.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    private static final String CAMINHO_BANCO = configuracao(
            "biblioteca.db", "BIBLIOTECA_DB", caminhoPadrao());

    private static final String URL = "jdbc:h2:file:" + CAMINHO_BANCO
            + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

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

    // Procura a pasta "database" do projeto, mesmo que o programa seja
    // iniciado de dentro da pasta "backend" ou de outra subpasta.
    private static String caminhoPadrao() {

        Path atual = Paths.get("").toAbsolutePath().normalize();

        while (atual != null) {
            Path pasta = atual.resolve("database");

            if (Files.isRegularFile(pasta.resolve("biblioteca.sql"))) {
                return pasta.resolve("biblioteca").toString().replace('\\', '/');
            }

            atual = atual.getParent();
        }

        return "./database/biblioteca";
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
