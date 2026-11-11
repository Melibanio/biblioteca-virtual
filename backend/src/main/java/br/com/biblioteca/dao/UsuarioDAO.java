package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Usuario;
import java.sql.*;

public class UsuarioDAO {
    public void cadastrar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha_hash, tipo) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getSenhaHash());
            ps.setString(4, usuario.getTipo());
            ps.executeUpdate();
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id, nome, email, senha_hash, tipo FROM usuario WHERE email = ?";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Usuario(rs.getInt("id"), rs.getString("nome"),
                    rs.getString("email"), rs.getString("senha_hash"), rs.getString("tipo"));
            }
        }
        return null;
    }
}
