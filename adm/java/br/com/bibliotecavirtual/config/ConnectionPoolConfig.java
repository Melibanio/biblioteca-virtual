package br.com.bibliotecavirtual.config;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionPoolConfig {
    private static BasicDataSource dataSource;

    private ConnectionPoolConfig() { }

    public static synchronized BasicDataSource getDataSource() {
        if (dataSource == null) {
            BasicDataSource pool = new BasicDataSource();
            pool.setDriverClassName("org.h2.Driver");
            pool.setUrl("jdbc:h2:~/biblioteca_virtual;DB_CLOSE_ON_EXIT=FALSE");
            pool.setUsername("sa");
            pool.setPassword("sa");
            pool.setMinIdle(2);
            pool.setMaxIdle(5);
            pool.setMaxTotal(20);
            dataSource = pool;
            System.out.println("Pool de conexões da Biblioteca Virtual criado com sucesso.");
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized void fecharPool() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
