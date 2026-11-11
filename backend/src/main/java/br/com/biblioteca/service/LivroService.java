package br.com.biblioteca.service;

import br.com.biblioteca.dao.LivroDAO;
import br.com.biblioteca.model.Livro;
import java.sql.SQLException;
import java.util.List;

public class LivroService {
    private final LivroDAO dao = new LivroDAO();

    public List<Livro> listar() throws SQLException { return dao.listar(); }
    public Livro buscarPorId(int id) throws SQLException { return dao.buscarPorId(id); }

    public void cadastrar(Livro livro) throws SQLException {
        validar(livro);
        dao.cadastrar(livro);
    }

    public boolean atualizar(Livro livro) throws SQLException {
        validar(livro);
        if (livro.getId() <= 0) throw new IllegalArgumentException("ID inválido.");
        return dao.atualizar(livro);
    }

    public boolean excluir(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("ID inválido.");
        return dao.excluir(id);
    }

    private void validar(Livro livro) {
        if (livro == null || livro.getTitulo() == null || livro.getTitulo().isBlank())
            throw new IllegalArgumentException("O título é obrigatório.");
        if (livro.getAutor() == null || livro.getAutor().isBlank())
            throw new IllegalArgumentException("O autor é obrigatório.");
    }
}
