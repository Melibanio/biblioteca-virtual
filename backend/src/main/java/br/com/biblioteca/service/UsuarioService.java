package br.com.biblioteca.service;

import br.com.biblioteca.dao.UsuarioDAO;
import br.com.biblioteca.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

public class UsuarioService {

    private final UsuarioDAO dao = new UsuarioDAO();

    private static final String EMAIL_ADMIN = configuracao(
            "BIBLIOTECA_ADMIN_EMAIL", "admin@biblioteca.com");

    private static final String SENHA_ADMIN = configuracao(
            "BIBLIOTECA_ADMIN_SENHA", "Biblioteca@2026");

    public void cadastrar(String nome, String email, String senha)
            throws SQLException {

        nome = nome == null ? "" : nome.trim();
        email = normalizarEmail(email);

        validarNome(nome);
        validarEmail(email);
        validarSenha(senha);

        if (email.equals(normalizarEmail(EMAIL_ADMIN))) {
            throw new IllegalArgumentException(
                    "Este e-mail é reservado ao administrador.");
        }

        if (dao.buscarPorEmail(email) != null) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
        Usuario usuario = new Usuario(0, nome, email, hash, "USUARIO");
        dao.cadastrar(usuario);
    }

    public void configurarAdministradorPadrao() throws SQLException {

        String email = normalizarEmail(EMAIL_ADMIN);
        validarEmail(email);
        validarSenha(SENHA_ADMIN);

        Usuario administrador = dao.buscarPorEmail(email);

        if (administrador == null) {
            String hash = BCrypt.hashpw(SENHA_ADMIN, BCrypt.gensalt());
            Usuario novoAdmin = new Usuario(
                    0, "Administrador", email, hash, "ADMIN");
            dao.cadastrar(novoAdmin);
            System.out.println("Conta administrativa criada.");

        } else if (!"ADMIN".equalsIgnoreCase(administrador.getTipo())) {
            throw new SQLException(
                    "O e-mail configurado para administrador pertence a uma conta comum.");
        }
    }

    public Usuario autenticar(String email, String senha) throws SQLException {

        if (email == null || senha == null) {
            return null;
        }

        Usuario usuario = dao.buscarPorEmail(normalizarEmail(email));

        if (usuario == null || usuario.getSenhaHash() == null) {
            return null;
        }

        try {
            return BCrypt.checkpw(senha, usuario.getSenhaHash()) ? usuario : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.buscarPorId(id);
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        return dao.buscarPorEmail(normalizarEmail(email));
    }

    public boolean emailCadastrado(String email) throws SQLException {
        return dao.buscarPorEmail(normalizarEmail(email)) != null;
    }

    public Usuario atualizarPerfil(int id, String nome, String email)
            throws SQLException {

        nome = nome == null ? "" : nome.trim();
        email = normalizarEmail(email);

        validarNome(nome);
        validarEmail(email);

        Usuario atual = dao.buscarPorId(id);

        if (atual == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        Usuario outro = dao.buscarPorEmail(email);

        if (outro != null && outro.getId() != id) {
            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado.");
        }

        if (!dao.atualizarPerfil(id, nome, email)) {
            throw new SQLException("Não foi possível atualizar o perfil.");
        }

        return dao.buscarPorId(id);
    }

    public void atualizarSenha(int id, String novaSenha) throws SQLException {

        validarSenha(novaSenha);

        Usuario usuario = dao.buscarPorId(id);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        String hash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());

        if (!dao.atualizarSenha(id, hash)) {
            throw new SQLException("Não foi possível atualizar a senha.");
        }
    }

    public void alterarSenha(int id, String senhaAtual, String novaSenha)
            throws SQLException {

        if (senhaAtual == null || senhaAtual.isEmpty()) {
            throw new IllegalArgumentException("Informe a senha atual.");
        }

        validarSenha(novaSenha);

        Usuario usuario = dao.buscarPorId(id);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        boolean confere;

        try {
            confere = BCrypt.checkpw(senhaAtual, usuario.getSenhaHash());
        } catch (IllegalArgumentException e) {
            confere = false;
        }

        if (!confere) {
            throw new IllegalArgumentException("A senha atual está incorreta.");
        }

        if (senhaAtual.equals(novaSenha)) {
            throw new IllegalArgumentException(
                    "A nova senha deve ser diferente da atual.");
        }

        if (!dao.atualizarSenha(id, BCrypt.hashpw(novaSenha, BCrypt.gensalt()))) {
            throw new SQLException("Não foi possível atualizar a senha.");
        }
    }

    private void validarNome(String nome) {

        if (nome.isBlank() || nome.length() > 100) {
            throw new IllegalArgumentException(
                    "Informe um nome com até 100 caracteres.");
        }
    }

    private void validarEmail(String email) {

        if (email.isBlank() || email.length() > 120
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
    }

    private void validarSenha(String senha) {

        if (senha == null || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres e respeitar o limite do BCrypt.");
        }
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String configuracao(String nome, String padrao) {

        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }
}
