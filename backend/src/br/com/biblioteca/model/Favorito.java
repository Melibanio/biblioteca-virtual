package br.com.biblioteca.model;

public class Favorito {

    private int id;
    private int usuarioId;
    private int livroId;

    // Construtor vazio
    public Favorito() {
    }

    // Construtor com todos os atributos
    public Favorito(int id, int usuarioId, int livroId) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.livroId = livroId;
    }

    // Retorna o ID do favorito
    public int getId() {
        return id;
    }

    // Altera o ID do favorito
    public void setId(int id) {
        this.id = id;
    }

    // Retorna o ID do usuário
    public int getUsuarioId() {
        return usuarioId;
    }

    // Altera o ID do usuário
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Retorna o ID do livro
    public int getLivroId() {
        return livroId;
    }

    // Altera o ID do livro
    public void setLivroId(int livroId) {
        this.livroId = livroId;
    }
}
