<<<<<<< HEAD

package br.com.biblioteca.service;

import br.com.biblioteca.dao.LivroDAO;
import br.com.biblioteca.model.Livro;
import java.sql.SQLException;
import java.util.List;

public class LivroService {

    private final LivroDAO dao = new LivroDAO();

    // Lista todos os livros
    public List<Livro> listar() throws SQLException {
        return dao.listar();
    }

    // Busca um livro pelo ID
    public Livro buscarPorId(int id) throws SQLException {
        return dao.buscarPorId(id);
    }

    // Cadastra um novo livro
    public void cadastrar(Livro livro) throws SQLException {
        validar(livro);
        dao.cadastrar(livro);
    }

    // Atualiza os dados de um livro
    public boolean atualizar(Livro livro) throws SQLException {
        validar(livro);

        if (livro.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.atualizar(livro);
    }

    // Exclui um livro pelo ID
    public boolean excluir(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.excluir(id);
    }

    // Verifica os dados obrigatórios
    private void validar(Livro livro) {

        if (livro == null || livro.getTitulo() == null
                || livro.getTitulo().isBlank()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }

        if (livro.getAutor() == null || livro.getAutor().isBlank()) {
            throw new IllegalArgumentException("O autor é obrigatório.");
        }
    }
=======
public class LivroService {
    
>>>>>>> 66d42127ecf7fa6c05a94acc7ba45d697b57f7d7
}
