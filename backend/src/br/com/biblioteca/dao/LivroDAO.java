
package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Livro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {

    // Lista todos os livros cadastrados
    public List<Livro> listar() throws SQLException {

        List<Livro> livros = new ArrayList<>();

        String sql = "SELECT id, titulo, autor, genero, descricao, capa FROM livro ORDER BY titulo";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                String autor = rs.getString("autor");
                String genero = rs.getString("genero");
                String descricao = rs.getString("descricao");
                String capa = rs.getString("capa");

                Livro livro = new Livro(
                        id, titulo, autor, genero, descricao, capa);

                livros.add(livro);
            }
        }

        return livros;
    }

    // Busca um livro pelo ID
    public Livro buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, titulo, autor, genero, descricao, capa FROM livro WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int livroId = rs.getInt("id");
                    String titulo = rs.getString("titulo");
                    String autor = rs.getString("autor");
                    String genero = rs.getString("genero");
                    String descricao = rs.getString("descricao");
                    String capa = rs.getString("capa");

                    Livro livro = new Livro(
                            livroId, titulo, autor, genero, descricao, capa);

                    return livro;
                }
            }
        }

        return null;
    }

    // Cadastra um livro no banco de dados
    public void cadastrar(Livro livro) throws SQLException {

        String sql = "INSERT INTO livro (titulo, autor, genero, descricao, capa) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getGenero());
            ps.setString(4, livro.getDescricao());
            ps.setString(5, livro.getCapa());

            ps.executeUpdate();
        }
    }

    // Atualiza os dados de um livro
    public boolean atualizar(Livro livro) throws SQLException {

        String sql = "UPDATE livro SET titulo = ?, autor = ?, genero = ?, descricao = ?, capa = ? WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getGenero());
            ps.setString(4, livro.getDescricao());
            ps.setString(5, livro.getCapa());
            ps.setInt(6, livro.getId());

            int resultado = ps.executeUpdate();

            return resultado > 0;
        }
    }

    // Exclui um livro pelo ID
    public boolean excluir(int id) throws SQLException {

        String sql = "DELETE FROM livro WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int resultado = ps.executeUpdate();

            return resultado > 0;
        }
    }
}
