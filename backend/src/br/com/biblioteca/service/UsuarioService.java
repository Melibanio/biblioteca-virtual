<<<<<<< HEAD
package br.com.biblioteca.service;

import br.com.biblioteca.dao.UsuarioDAO;
import br.com.biblioteca.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class UsuarioService {

    private final UsuarioDAO dao = new UsuarioDAO();

    // Cadastra um novo usuário
    public void cadastrar(String nome, String email, String senha) throws SQLException {

        // Verifica se os dados foram preenchidos corretamente
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
    if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("Informe nome, e-mail válido e senha com pelo menos 8 caracteres.");
        }
        // Verifica se o e-mail já está cadastrado
        Usuario usuarioExistente = dao.buscarPorEmail(email);
        if (usuarioExistente != null) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        // Criptografa a senha antes de salvar
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
        // Cria o usuário com o tipo ESTUDANTE
        Usuario usuario = new Usuario(0, nome, email, hash, "ESTUDANTE");
        // Salva o usuário no banco de dados
        dao.cadastrar(usuario);
    }
    // Verifica os dados de login
    public Usuario autenticar(String email, String senha) throws SQLException {
        Usuario usuario = dao.buscarPorEmail(email);

        // Verifica se o usuário existe e se a senha está correta
        if (usuario == null) {
            return null;
        }

        if (!BCrypt.checkpw(senha, usuario.getSenhaHash())) {
            return null;
        }

        return usuario;
    }
=======
public class UsuarioService {
    
>>>>>>> 66d42127ecf7fa6c05a94acc7ba45d697b57f7d7
}
