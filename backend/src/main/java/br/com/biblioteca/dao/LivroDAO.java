package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Livro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {
    public List<Livro> listar() throws SQLException {
        String sql = "SELECT id, titulo, autor, genero, descricao, capa FROM livro ORDER BY titulo";
        List<Livro> livros = new ArrayList<>();
        try (Connection con = Conexao.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                livros.add(new Livro(rs.getInt("id"), rs.getString("titulo"),
                    rs.getString("autor"), rs.getString("genero"),
                    rs.getString("descricao"), rs.getString("capa")));
            }
        }
        return livros;
    }

    public Livro buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, titulo, autor, genero, descricao, capa FROM livro WHERE id = ?";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Livro(rs.getInt("id"), rs.getString("titulo"),
                    rs.getString("autor"), rs.getString("genero"), rs.getString("descricao"), rs.getString("capa"));
            }
        }
        return null;
    }

    public void cadastrar(Livro livro) throws SQLException {
        String sql = "INSERT INTO livro (titulo, autor, genero, descricao, capa) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getGenero());
            ps.setString(4, livro.getDescricao());
            ps.setString(5, livro.getCapa());
            ps.executeUpdate();
        }
    }

    public boolean atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livro SET titulo=?, autor=?, genero=?, descricao=?, capa=? WHERE id=?";
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getGenero());
            ps.setString(4, livro.getDescricao());
            ps.setString(5, livro.getCapa());
            ps.setInt(6, livro.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(int id) throws SQLException {
        try (Connection con = Conexao.conectar(); PreparedStatement ps = con.prepareStatement("DELETE FROM livro WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
