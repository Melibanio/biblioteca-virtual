package br.com.biblioteca.service;

import br.com.biblioteca.dao.LivroDAO;
import br.com.biblioteca.model.Livro;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LivroService {

    private final LivroDAO dao = new LivroDAO();

    public List<Livro> listar() throws SQLException {
        return dao.listar();
    }

    public Livro buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.buscarPorId(id);
    }

    public void cadastrar(Livro livro) throws SQLException {
        validar(livro);
        dao.cadastrar(livro);
    }

    public boolean atualizar(Livro livro) throws SQLException {
        validar(livro);

        if (livro.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.atualizar(livro);
    }

    public boolean excluir(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.excluir(id);
    }

    public List<Map<String, Object>> listarHistorico(int usuarioId)
            throws SQLException {

        validarUsuario(usuarioId);
        return dao.listarHistorico(usuarioId);
    }

    public Map<String, Object> buscarHistorico(int usuarioId, int livroId)
            throws SQLException {

        validarUsuario(usuarioId);
        validarLivro(livroId);
        return dao.buscarHistorico(usuarioId, livroId);
    }

    public Map<String, Object> iniciarLeitura(int usuarioId, int livroId)
            throws SQLException {

        validarUsuario(usuarioId);
        validarLivroExiste(livroId);
        return dao.iniciarLeitura(usuarioId, livroId);
    }

    public Map<String, Object> atualizarProgresso(
            int usuarioId, int livroId, int progresso) throws SQLException {

        validarUsuario(usuarioId);
        validarLivroExiste(livroId);

        if (progresso < 0 || progresso > 100) {
            throw new IllegalArgumentException(
                    "O progresso deve ficar entre 0 e 100.");
        }

        if (dao.buscarHistorico(usuarioId, livroId) == null) {
            dao.iniciarLeitura(usuarioId, livroId);
        }

        dao.atualizarProgresso(usuarioId, livroId, progresso);
        return dao.buscarHistorico(usuarioId, livroId);
    }

    private void validar(Livro livro) {

        if (livro == null) {
            throw new IllegalArgumentException("Informe os dados do livro.");
        }

        if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }

        if (livro.getAutor() == null || livro.getAutor().trim().isEmpty()) {
            throw new IllegalArgumentException("O autor é obrigatório.");
        }

        if (livro.getTitulo().trim().length() > 150) {
            throw new IllegalArgumentException("O título deve ter até 150 caracteres.");
        }

        if (livro.getAutor().trim().length() > 100) {
            throw new IllegalArgumentException("O autor deve ter até 100 caracteres.");
        }

        if (livro.getGenero() != null && livro.getGenero().length() > 80) {
            throw new IllegalArgumentException("O gênero deve ter até 80 caracteres.");
        }

        if (livro.getCapa() != null && livro.getCapa().length() > 255) {
            throw new IllegalArgumentException("O endereço da capa deve ter até 255 caracteres.");
        }

        livro.setTitulo(livro.getTitulo().trim());
        livro.setAutor(livro.getAutor().trim());

        if (livro.getGenero() != null) {
            livro.setGenero(livro.getGenero().trim());
        }
    }

    private void validarUsuario(int usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
    }

    private void validarLivro(int livroId) {
        if (livroId <= 0) {
            throw new IllegalArgumentException("Livro inválido.");
        }
    }

    private void validarLivroExiste(int livroId) throws SQLException {

        validarLivro(livroId);

        if (dao.buscarPorId(livroId) == null) {
            throw new IllegalArgumentException("Livro não encontrado.");
        }
    }
}
