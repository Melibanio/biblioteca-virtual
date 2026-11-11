package br.com.biblioteca.service;

import br.com.biblioteca.dao.UsuarioDAO;
import br.com.biblioteca.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class UsuarioService {
    private final UsuarioDAO dao = new UsuarioDAO();

    public void cadastrar(String nome, String email, String senha) throws SQLException {
        if (nome == null || nome.isBlank() || email == null || !email.contains("@")
                || senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
        if (dao.buscarPorEmail(email) != null) throw new IllegalArgumentException("E-mail já cadastrado.");
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
        dao.cadastrar(new Usuario(0, nome, email, hash, "ESTUDANTE"));
    }

    public Usuario autenticar(String email, String senha) throws SQLException {
        Usuario usuario = dao.buscarPorEmail(email);
        if (usuario == null || !BCrypt.checkpw(senha, usuario.getSenhaHash())) return null;
        return usuario;
    }
}
