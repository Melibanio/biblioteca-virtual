package br.com.biblioteca.model;

public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private String genero;
    private String descricao;
    private String capa;

    public Livro() {
    }

    public Livro(int id, String titulo, String autor, String genero, String descricao, String capa) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.descricao = descricao;
        this.capa = capa;
    }

    // Retorna o ID do livro
    public int getId() {
        return id;
    }

    // Altera o ID do livro
    public void setId(int id) {
        this.id = id;
    }

    // Retorna o título do livro
    public String getTitulo() {
        return titulo;
    }

    // Altera o título do livro
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Retorna o autor do livro
    public String getAutor() {
        return autor;
    }

    // Altera o autor do livro
    public void setAutor(String autor) {
        this.autor = autor;
    }

    // Retorna o gênero do livro
    public String getGenero() {
        return genero;
    }

    // Altera o gênero do livro
    public void setGenero(String genero) {
        this.genero = genero;
    }

    // Retorna a descrição do livro
    public String getDescricao() {
        return descricao;
    }

    // Altera a descrição do livro
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // Retorna a capa do livro
    public String getCapa() {
        return capa;
    }

    // Altera a capa do livro
    public void setCapa(String capa) {
        this.capa = capa;
    }
}
