package br.com.bibliotecavirtual.servlet;

import br.com.bibliotecavirtual.dao.LivroDao;
import br.com.bibliotecavirtual.model.Livro;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class LivroServlet extends HttpServlet {
    private final LivroDao livroDao = new LivroDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        try {
            List<Livro> livros = livroDao.listarTodos();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < livros.size(); i++) {
                if (i > 0) json.append(',');
                Livro livro = livros.get(i);
                json.append("{\"id\":").append(livro.getId())
                    .append(",\"titulo\":\"").append(escapar(livro.getTitulo()))
                    .append("\",\"autor\":\"").append(escapar(livro.getAutor()))
                    .append("\",\"genero\":\"").append(escapar(livro.getGenero()))
                    .append("\",\"capa\":\"").append(escapar(livro.getCapa()))
                    .append("\",\"sinopse\":\"").append(escapar(livro.getSinopse())).append("\"}");
            }
            resp.getWriter().write(json.append(']').toString());
        } catch (Exception erro) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"erro\":\"Não foi possível listar os livros.\"}");
            erro.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String acao = req.getParameter("acao");
        try {
            if ("excluir".equals(acao)) {
                int id = Integer.parseInt(req.getParameter("id"));
                if (!livroDao.excluir(id)) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"mensagem\":\"Livro não encontrado.\"}");
                    return;
                }
                resp.getWriter().write("{\"mensagem\":\"Livro excluído com sucesso.\"}");
                return;
            }

            String titulo = limpar(req.getParameter("titulo"));
            String autor = limpar(req.getParameter("autor"));
            String genero = limpar(req.getParameter("genero"));
            String capa = limpar(req.getParameter("capa"));
            String sinopse = limpar(req.getParameter("sinopse"));
            if (titulo.isEmpty() || autor.isEmpty() || genero.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"mensagem\":\"Preencha título, autor e gênero.\"}");
                return;
            }

            Livro livro = new Livro(titulo, autor, genero, capa, sinopse);
            if ("atualizar".equals(acao)) {
                livro.setId(Integer.parseInt(req.getParameter("id")));
                if (!livroDao.atualizar(livro)) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"mensagem\":\"Livro não encontrado para edição.\"}");
                    return;
                }
                resp.getWriter().write("{\"mensagem\":\"Livro atualizado com sucesso.\"}");
            } else if ("cadastrar".equals(acao)) {
                livroDao.inserir(livro);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write("{\"mensagem\":\"Livro cadastrado com sucesso.\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"mensagem\":\"Ação inválida.\"}");
            }
        } catch (NumberFormatException erro) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"mensagem\":\"Identificador de livro inválido.\"}");
        } catch (Exception erro) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"mensagem\":\"Ocorreu um erro ao processar o livro.\"}");
            erro.printStackTrace();
        }
    }

    private String limpar(String valor) { return valor == null ? "" : valor.trim(); }

    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
    }
}
