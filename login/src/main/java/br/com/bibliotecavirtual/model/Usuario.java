package br.com.bibliotecavirtual.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String nome;
    @Column(nullable = false, unique = true) private String email;
    private String telefone;
    @Column(nullable = false) private LocalDate nascimento;
    @Column(nullable = false) private String tipoUsuario;
    @Column(nullable = false) private String senha;
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public LocalDate getNascimento() { return nascimento; }
    public void setNascimento(LocalDate nascimento) { this.nascimento = nascimento; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
