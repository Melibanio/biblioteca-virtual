package br.com.biblioteca.model;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private String tipo;
    
    public Usuario() {
    }

    public Usuario(int id, String nome, String email, String senhaHash, String tipo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
    }

    // Retorna o ID do usuário
    public int getId() {
        return id;
    }

    // Altera o ID do usuário
    public void setId(int id) {
        this.id = id;
    }

    // Retorna o nome do usuário
    public String getNome() {
        return nome;
    }

    // Altera o nome do usuário
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Retorna o e-mail do usuário
    public String getEmail() {
        return email;
    }

    // Altera o e-mail do usuário
    public void setEmail(String email) {
        this.email = email;
    }

    // Retorna a senha criptografada
    public String getSenhaHash() {
        return senhaHash;
    }

    // Altera a senha criptografada
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    // Retorna o tipo de usuário
    public String getTipo() {
        return tipo;
    }

    // Altera o tipo de usuário
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
