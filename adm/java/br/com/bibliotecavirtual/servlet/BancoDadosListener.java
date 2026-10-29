package br.com.bibliotecavirtual.servlet;

import br.com.bibliotecavirtual.dao.LivroDao;
import br.com.bibliotecavirtual.config.ConnectionPoolConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class BancoDadosListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try {
            new LivroDao().criarTabelaEInserirDadosIniciais();
            System.out.println("Banco da Biblioteca Virtual preparado com sucesso.");
        } catch (Exception erro) {
            System.err.println("Não foi possível preparar o banco da Biblioteca Virtual: " + erro.getMessage());
            erro.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        try {
            ConnectionPoolConfig.fecharPool();
        } catch (Exception erro) {
            System.err.println("Erro ao fechar o pool de conexões: " + erro.getMessage());
        }
    }
}
