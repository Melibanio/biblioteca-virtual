package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

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

    public Usuario buscarPorEmail(String email) throws SQLException {

        String sql = "SELECT id, nome, email, senha_hash, tipo "
                + "FROM usuario WHERE LOWER(email) = LOWER(?)";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarUsuario(rs);
                }
            }
        }

        return null;
    }

    public Usuario buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, nome, email, senha_hash, tipo FROM usuario WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarUsuario(rs);
                }
            }
        }

        return null;
    }

    public boolean atualizarPerfil(int id, String nome, String email)
            throws SQLException {

        String sql = "UPDATE usuario SET nome = ?, email = ? WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean atualizarSenha(int id, String senhaHash)
            throws SQLException {

        String sql = "UPDATE usuario SET senha_hash = ? WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, senhaHash);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;
        }
    }

    private Usuario montarUsuario(ResultSet rs) throws SQLException {

        return new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha_hash"),
                rs.getString("tipo"));
    }
}
