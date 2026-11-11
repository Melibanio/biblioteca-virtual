package br.com.biblioteca.model;

public class Favorito {
    private int id;
    private int usuarioId;
    private int livroId;

    public Favorito() {}

    public Favorito(int id, int usuarioId, int livroId) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.livroId = livroId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public int getLivroId() { return livroId; }
    public void setLivroId(int livroId) { this.livroId = livroId; }
}
