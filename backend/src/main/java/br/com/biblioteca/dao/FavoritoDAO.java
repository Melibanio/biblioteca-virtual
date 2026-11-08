package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Favorito;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritoDAO {
    public void adicionar(int usuarioId, int livroId) throws SQLException {
        String sql = "INSERT INTO favorito (usuario_id, livro_id) VALUES (?, ?)";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);
            ps.executeUpdate();
        }
    }

    public boolean remover(int usuarioId, int livroId) throws SQLException {
        String sql = "DELETE FROM favorito WHERE usuario_id=? AND livro_id=?";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Favorito> listarPorUsuario(int usuarioId) throws SQLException {
        List<Favorito> favoritos = new ArrayList<>();
        String sql = "SELECT id, usuario_id, livro_id FROM favorito WHERE usuario_id=?";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) favoritos.add(new Favorito(rs.getInt("id"),
                    rs.getInt("usuario_id"), rs.getInt("livro_id")));
            }
        }
        return favoritos;
    }
}
