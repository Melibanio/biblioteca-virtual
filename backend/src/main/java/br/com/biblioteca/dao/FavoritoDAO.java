package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Favorito;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoritoDAO {

    // Adiciona um livro aos favoritos
    public void adicionar(int usuarioId, int livroId) throws SQLException {
        String sql = "INSERT INTO favorito (usuario_id, livro_id) VALUES (?, ?)";
        try (Connection con = Conexao.conectar()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);
            ps.executeUpdate();
            ps.close();
        }
    }

    // Remove um livro dos favoritos
    public boolean remover(int usuarioId, int livroId) throws SQLException {

        String sql = "DELETE FROM favorito WHERE usuario_id = ? AND livro_id = ?";
        try (Connection con = Conexao.conectar()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);
            int resultado = ps.executeUpdate();
            ps.close();
            return resultado > 0;
        }
    }

    // Lista os favoritos de um usuário
    public List<Favorito> listarPorUsuario(int usuarioId) throws SQLException {

        List<Favorito> favoritos = new ArrayList<>();

        String sql = "SELECT id, usuario_id, livro_id FROM favorito WHERE usuario_id = ?";
        try (Connection con = Conexao.conectar()) {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int usuario = rs.getInt("usuario_id");
                int livro = rs.getInt("livro_id");
                Favorito favorito = new Favorito(id, usuario, livro);
                favoritos.add(favorito);
            }

            rs.close();
            ps.close();
        }

        return favoritos;
    }

}
