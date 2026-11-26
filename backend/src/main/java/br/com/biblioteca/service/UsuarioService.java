package br.com.biblioteca.service;

import br.com.biblioteca.dao.UsuarioDAO;
import br.com.biblioteca.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class UsuarioService {
    private final UsuarioDAO dao = new UsuarioDAO();
    private static final String EMAIL_ADMIN = "admin@biblioteca.com";
    private static final String SENHA_ADMIN = "Biblioteca@2026";

    public void cadastrar(String nome, String email, String senha) throws SQLException {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
        Usuario usuarioExistente = dao.buscarPorEmail(email);
        if (usuarioExistente != null) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
        Usuario usuario = new Usuario(0, nome, email, hash, "USUARIO");
        dao.cadastrar(usuario);
    }
    public void configurarAdministradorPadrao() throws SQLException {

        Usuario administrador = dao.buscarPorEmail(EMAIL_ADMIN);
        boolean senhaConfigurada = false;

        if (administrador != null && administrador.getSenhaHash() != null) {
            try {
                senhaConfigurada = BCrypt.checkpw(SENHA_ADMIN, administrador.getSenhaHash());
            } catch (IllegalArgumentException e) {
                senhaConfigurada = false;
            }
        }

        if (administrador == null || !senhaConfigurada
                || !"ADMIN".equalsIgnoreCase(administrador.getTipo())) {

            String hash = BCrypt.hashpw(SENHA_ADMIN, BCrypt.gensalt());
            dao.salvarAdministrador("Administrador", EMAIL_ADMIN, hash);
        }
    }

    public Usuario autenticar(String email, String senha) throws SQLException {
        Usuario usuario = dao.buscarPorEmail(email);

        if (usuario == null) {
            return null;
        }

        if (!BCrypt.checkpw(senha, usuario.getSenhaHash())) {
            return null;
        }

        return usuario;
    }

}
