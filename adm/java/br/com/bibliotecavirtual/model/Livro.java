package br.com.bibliotecavirtual.model;

/** Modelo que representa um livro do acervo da Biblioteca Virtual. */
public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private String genero;
    private String capa;
    private String sinopse;

    public Livro() { }

    public Livro(int id, String titulo, String autor, String genero, String capa, String sinopse) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.capa = capa;
        this.sinopse = sinopse;
    }

    public Livro(String titulo, String autor, String genero, String capa, String sinopse) {
        this(0, titulo, autor, genero, capa, sinopse);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getCapa() { return capa; }
    public void setCapa(String capa) { this.capa = capa; }
    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
}
