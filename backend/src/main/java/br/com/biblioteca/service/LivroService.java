package br.com.biblioteca.service;

import br.com.biblioteca.dao.LivroDAO;
import br.com.biblioteca.model.Livro;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LivroService {

    private final LivroDAO dao = new LivroDAO();


    // Acervo inicial: obras de domínio público com capas em images/capas.
    private static final String[][] ACERVO_INICIAL = {
        {"Dom Casmurro", "Machado de Assis", "Romance",
            "Bentinho, já maduro, narra suas memórias: o amor por Capitu, a vida no Rio de Janeiro do século XIX e a dúvida que marcou sua existência, a suspeita de traição.",
            "images/capas/dom-casmurro.svg"},
        {"Memórias Póstumas de Brás Cubas", "Machado de Assis", "Romance",
            "Um defunto autor conta sua vida com ironia e humor ácido, criticando a sociedade carioca e as vaidades do seu tempo.",
            "images/capas/memorias-postumas.svg"},
        {"O Alienista", "Machado de Assis", "Conto",
            "O médico Simão Bacamarte funda a Casa Verde em Itaguaí e começa a internar quem foge ao seu conceito de normalidade.",
            "images/capas/o-alienista.svg"},
        {"Iracema", "José de Alencar", "Romance",
            "Romance indianista que une a jovem tabajara Iracema ao colonizador português Martim, nas terras do Ceará.",
            "images/capas/iracema.svg"},
        {"O Guarani", "José de Alencar", "Romance",
            "O índio goitacá Peri dedica sua vida a proteger Ceci, filha do fidalgo português Dom Antônio de Mariz.",
            "images/capas/o-guarani.svg"},
        {"Triste Fim de Policarpo Quaresma", "Lima Barreto", "Romance",
            "Funcionário público de patriotismo ingênuo, Policarpo tenta transformar o país com seus ideais e acaba desiludido.",
            "images/capas/policarpo-quaresma.svg"},
        {"O Cortiço", "Aluísio Azevedo", "Romance",
            "A vida em um cortiço do Rio de Janeiro mostra a ambição de João Romão e a força do meio sobre as pessoas, no Naturalismo.",
            "images/capas/o-cortico.svg"},
        {"A Escrava Isaura", "Bernardo Guimarães", "Romance",
            "A história de Isaura e de sua luta pela liberdade, em um romance que ajudou a levar a causa abolicionista ao público.",
            "images/capas/a-escrava-isaura.svg"},
        {"O Ateneu", "Raul Pompeia", "Romance",
            "Sérgio relembra seus anos em um colégio interno e revela as relações de poder e as aparências da escola e da sociedade.",
            "images/capas/o-ateneu.svg"},
        {"Vidas Secas", "Graciliano Ramos", "Romance",
            "Fabiano, Sinhá Vitória, os filhos e a cachorra Baleia enfrentam a seca e a miséria na travessia pelo sertão nordestino.",
            "images/capas/vidas-secas.svg"},
        {"Espumas Flutuantes", "Castro Alves", "Poesia",
            "Único livro publicado em vida pelo poeta, reúne versos líricos e poemas de forte voz social do Romantismo brasileiro.",
            "images/capas/espumas-flutuantes.svg"}
    };

    // Cadastra o acervo inicial se o banco estiver vazio e coloca capa
    // nos livros já cadastrados que tenham o mesmo título e estejam sem capa.
    public void popularAcervoInicial() throws SQLException {

        if (dao.contar() == 0) {

            for (String[] item : ACERVO_INICIAL) {
                dao.cadastrar(new Livro(0, item[0], item[1], item[2], item[3], item[4]));
            }

            System.out.println("Acervo inicial cadastrado (" + ACERVO_INICIAL.length + " livros).");
            return;
        }

        for (String[] item : ACERVO_INICIAL) {
            dao.definirCapaSeVazia(item[0], item[4]);
        }
    }

    public List<Livro> listar() throws SQLException {
        return dao.listar();
    }

    public Livro buscarPorId(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.buscarPorId(id);
    }

    public void cadastrar(Livro livro) throws SQLException {
        validar(livro);
        dao.cadastrar(livro);
    }

    public boolean atualizar(Livro livro) throws SQLException {
        validar(livro);

        if (livro.getId() <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.atualizar(livro);
    }

    public boolean excluir(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return dao.excluir(id);
    }

    public List<Map<String, Object>> listarHistorico(int usuarioId)
            throws SQLException {

        validarUsuario(usuarioId);
        return dao.listarHistorico(usuarioId);
    }

    public Map<String, Object> buscarHistorico(int usuarioId, int livroId)
            throws SQLException {

        validarUsuario(usuarioId);
        validarLivro(livroId);
        return dao.buscarHistorico(usuarioId, livroId);
    }

    public Map<String, Object> iniciarLeitura(int usuarioId, int livroId)
            throws SQLException {

        validarUsuario(usuarioId);
        validarLivroExiste(livroId);
        return dao.iniciarLeitura(usuarioId, livroId);
    }

    public Map<String, Object> atualizarProgresso(
            int usuarioId, int livroId, int progresso) throws SQLException {

        validarUsuario(usuarioId);
        validarLivroExiste(livroId);

        if (progresso < 0 || progresso > 100) {
            throw new IllegalArgumentException(
                    "O progresso deve ficar entre 0 e 100.");
        }

        if (dao.buscarHistorico(usuarioId, livroId) == null) {
            dao.iniciarLeitura(usuarioId, livroId);
        }

        dao.atualizarProgresso(usuarioId, livroId, progresso);
        return dao.buscarHistorico(usuarioId, livroId);
    }

    private void validar(Livro livro) {

        if (livro == null) {
            throw new IllegalArgumentException("Informe os dados do livro.");
        }

        if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título é obrigatório.");
        }

        if (livro.getAutor() == null || livro.getAutor().trim().isEmpty()) {
            throw new IllegalArgumentException("O autor é obrigatório.");
        }

        if (livro.getGenero() == null || livro.getGenero().trim().isEmpty()) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }

        if (livro.getTitulo().trim().length() > 150) {
            throw new IllegalArgumentException("O título deve ter até 150 caracteres.");
        }

        if (livro.getAutor().trim().length() > 100) {
            throw new IllegalArgumentException("O autor deve ter até 100 caracteres.");
        }

        if (livro.getGenero() != null && livro.getGenero().length() > 80) {
            throw new IllegalArgumentException("O gênero deve ter até 80 caracteres.");
        }

        if (livro.getCapa() != null && livro.getCapa().length() > 255) {
            throw new IllegalArgumentException("O endereço da capa deve ter até 255 caracteres.");
        }

        livro.setTitulo(livro.getTitulo().trim());
        livro.setAutor(livro.getAutor().trim());

        livro.setGenero(livro.getGenero().trim());

        if (livro.getDescricao() != null) {
            livro.setDescricao(livro.getDescricao().trim());
        }

        if (livro.getCapa() != null) {
            livro.setCapa(livro.getCapa().trim());

            if (livro.getCapa().isEmpty()) {
                livro.setCapa(null);
            }
        }
    }

    private void validarUsuario(int usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
    }

    private void validarLivro(int livroId) {
        if (livroId <= 0) {
            throw new IllegalArgumentException("Livro inválido.");
        }
    }

    private void validarLivroExiste(int livroId) throws SQLException {

        validarLivro(livroId);

        if (dao.buscarPorId(livroId) == null) {
            throw new IllegalArgumentException("Livro não encontrado.");
        }
    }
}
