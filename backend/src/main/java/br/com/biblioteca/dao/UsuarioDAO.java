package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    // Cadastra um usuário no banco de dados
    public void cadastrar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha_hash, tipo) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenhaHash());
            ps.setString(4, usuario.getTipo());
            ps.executeUpdate();
        }
    }
    // Busca um usuário pelo e-mail
    public Usuario buscarPorEmail(String email) throws SQLException {

        String sql = "SELECT id, nome, email, senha_hash, tipo FROM usuario WHERE email = ?";
        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    String emailUsuario = rs.getString("email");
                    String senha = rs.getString("senha_hash");
                    String tipo = rs.getString("tipo");
                    Usuario usuario = new Usuario(
                            id,
                            nome,
                            emailUsuario,
                            senha,
                            tipo);
                    return usuario;
                }
            }
        }
        return null;
    }
}

