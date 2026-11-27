package br.com.biblioteca.dao;

import br.com.biblioteca.config.Conexao;
import br.com.biblioteca.model.Livro;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LivroDAO {

    public List<Livro> listar() throws SQLException {

        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT id, titulo, autor, genero, descricao, capa "
                + "FROM livro ORDER BY titulo";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                livros.add(montarLivro(rs));
            }
        }

        return livros;
    }

    public int contar() throws SQLException {

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM livro");
                ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }

    // Coloca a capa em um livro já cadastrado que ainda não tem capa.
    public boolean definirCapaSeVazia(String titulo, String capa)
            throws SQLException {

        String sql = "UPDATE livro SET capa = ? "
                + "WHERE LOWER(titulo) = LOWER(?) AND (capa IS NULL OR capa = '')";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, capa);
            ps.setString(2, titulo);
            return ps.executeUpdate() > 0;
        }
    }

    public Livro buscarPorId(int id) throws SQLException {

        String sql = "SELECT id, titulo, autor, genero, descricao, capa "
                + "FROM livro WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarLivro(rs);
                }
            }
        }

        return null;
    }

    public void cadastrar(Livro livro) throws SQLException {

        String sql = "INSERT INTO livro "
                + "(titulo, autor, genero, descricao, capa) VALUES (?, ?, ?, ?, ?)";

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

    public boolean atualizar(Livro livro) throws SQLException {

        String sql = "UPDATE livro SET titulo = ?, autor = ?, genero = ?, "
                + "descricao = ?, capa = ? WHERE id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

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

        // Remove antes os registros que dependem do livro (chaves estrangeiras).
        String[] dependentes = {
            "DELETE FROM favorito WHERE livro_id = ?",
            "DELETE FROM historico_leitura WHERE livro_id = ?",
            "DELETE FROM avaliacao WHERE livro_id = ?",
            "DELETE FROM livro_categoria WHERE livro_id = ?"
        };

        try (Connection con = Conexao.conectar()) {

            con.setAutoCommit(false);

            try {
                for (String sql : dependentes) {
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }

                boolean excluido;

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM livro WHERE id = ?")) {
                    ps.setInt(1, id);
                    excluido = ps.executeUpdate() > 0;
                }

                con.commit();
                return excluido;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Histórico de leitura, utilizando a tabela que já existe no SQL.

    public List<Map<String, Object>> listarHistorico(int usuarioId)
            throws SQLException {

        List<Map<String, Object>> historicos = new ArrayList<>();

        String sql = "SELECT h.id, h.livro_id, l.titulo, l.autor, "
                + "h.progresso, h.status, h.data_inicio, h.data_conclusao "
                + "FROM historico_leitura h "
                + "INNER JOIN livro l ON l.id = h.livro_id "
                + "WHERE h.usuario_id = ? ORDER BY h.id DESC";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historicos.add(montarHistorico(rs));
                }
            }
        }

        return historicos;
    }

    public Map<String, Object> buscarHistorico(int usuarioId, int livroId)
            throws SQLException {

        String sql = "SELECT h.id, h.livro_id, l.titulo, l.autor, "
                + "h.progresso, h.status, h.data_inicio, h.data_conclusao "
                + "FROM historico_leitura h "
                + "INNER JOIN livro l ON l.id = h.livro_id "
                + "WHERE h.usuario_id = ? AND h.livro_id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarHistorico(rs);
                }
            }
        }

        return null;
    }

    public Map<String, Object> iniciarLeitura(int usuarioId, int livroId)
            throws SQLException {

        Map<String, Object> existente = buscarHistorico(usuarioId, livroId);

        if (existente != null) {
            return existente;
        }

        String sql = "INSERT INTO historico_leitura "
                + "(usuario_id, livro_id, progresso, status) "
                + "VALUES (?, ?, 0, 'NAO_INICIADO')";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, livroId);
            ps.executeUpdate();

        } catch (SQLException e) {
            // Se duas solicitações tentarem iniciar ao mesmo tempo,
            // consulta novamente o registro que já foi criado.
            if (e.getSQLState() == null || !e.getSQLState().startsWith("23")) {
                throw e;
            }
        }

        return buscarHistorico(usuarioId, livroId);
    }

    public boolean atualizarProgresso(int usuarioId, int livroId, int progresso)
            throws SQLException {

        String sql = "UPDATE historico_leitura SET progresso = ?, "
                + "status = CASE WHEN ? = 100 THEN 'CONCLUIDO' "
                + "WHEN ? = 0 THEN 'NAO_INICIADO' "
                + "ELSE 'EM_ANDAMENTO' END, "
                + "data_inicio = CASE WHEN ? > 0 AND data_inicio IS NULL "
                + "THEN CURRENT_DATE ELSE data_inicio END, "
                + "data_conclusao = CASE WHEN ? = 100 "
                + "THEN CURRENT_DATE ELSE NULL END "
                + "WHERE usuario_id = ? AND livro_id = ?";

        try (Connection con = Conexao.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, progresso);
            ps.setInt(2, progresso);
            ps.setInt(3, progresso);
            ps.setInt(4, progresso);
            ps.setInt(5, progresso);
            ps.setInt(6, usuarioId);
            ps.setInt(7, livroId);

            return ps.executeUpdate() > 0;
        }
    }

    private Livro montarLivro(ResultSet rs) throws SQLException {

        return new Livro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getString("genero"),
                rs.getString("descricao"),
                rs.getString("capa"));
    }

    private Map<String, Object> montarHistorico(ResultSet rs)
            throws SQLException {

        Map<String, Object> dados = new HashMap<>();
        Date inicio = rs.getDate("data_inicio");
        Date conclusao = rs.getDate("data_conclusao");

        dados.put("id", rs.getInt("id"));
        dados.put("livroId", rs.getInt("livro_id"));
        dados.put("titulo", rs.getString("titulo"));
        dados.put("autor", rs.getString("autor"));
        dados.put("progresso", rs.getInt("progresso"));
        dados.put("status", rs.getString("status"));
        dados.put("dataInicio", inicio == null ? null : inicio.toString());
        dados.put("dataConclusao",
                conclusao == null ? null : conclusao.toString());

        return dados;
    }
}
