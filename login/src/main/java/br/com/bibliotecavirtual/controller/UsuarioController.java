package br.com.bibliotecavirtual.controller;

import br.com.bibliotecavirtual.model.Usuario;
import br.com.bibliotecavirtual.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class UsuarioController {
    private final UsuarioRepository usuarioRepository;
    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/usuarios/cadastrar")
    public String cadastrar(@RequestParam String nome, @RequestParam String email,
            @RequestParam(required = false, defaultValue = "") String telefone,
            @RequestParam LocalDate nascimento, @RequestParam String tipoUsuario,
            @RequestParam String senha, @RequestParam String confirmarSenha,
            RedirectAttributes redirect) {
        if (!senha.equals(confirmarSenha)) {
            redirect.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/cadastro";
        }
        if (senha.length() < 8) {
            redirect.addFlashAttribute("erro", "A senha deve ter pelo menos 8 caracteres.");
            return "redirect:/cadastro";
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email.trim())) {
            redirect.addFlashAttribute("erro", "Já existe uma conta cadastrada com esse e-mail.");
            return "redirect:/cadastro";
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setTelefone(telefone.trim());
        usuario.setNascimento(nascimento);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setSenha(senha);
        usuarioRepository.save(usuario);
        redirect.addFlashAttribute("sucesso", "Cadastro realizado! Agora você pode entrar.");
        return "redirect:/login";
    }

    @PostMapping("/usuarios/entrar")
    public String entrar(@RequestParam String email, @RequestParam String senha,
            HttpSession session, RedirectAttributes redirect) {
        Optional<Usuario> resultado = usuarioRepository.findByEmailIgnoreCase(email.trim());
        if (resultado.isEmpty() || !senha.equals(resultado.get().getSenha())) {
            redirect.addFlashAttribute("erro", "E-mail ou senha incorretos.");
            return "redirect:/login";
        }
        session.setAttribute("usuarioId", resultado.get().getId());
        return "redirect:/perfil";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model, RedirectAttributes redirect) {
        Object id = session.getAttribute("usuarioId");
        if (id == null) {
            redirect.addFlashAttribute("erro", "Entre na sua conta para acessar o perfil.");
            return "redirect:/login";
        }
        Optional<Usuario> resultado = usuarioRepository.findById((Long) id);
        if (resultado.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        model.addAttribute("usuario", resultado.get());
        return "Perfil";
    }

    @PostMapping("/usuarios/recuperar-senha")
    public String recuperarSenha(@RequestParam String email, @RequestParam String novaSenha,
            @RequestParam String confirmarSenha, RedirectAttributes redirect) {
        if (!novaSenha.equals(confirmarSenha)) {
            redirect.addFlashAttribute("erro", "As senhas não coincidem.");
            return "redirect:/esqueci-senha";
        }
        if (novaSenha.length() < 8) {
            redirect.addFlashAttribute("erro", "A senha deve ter pelo menos 8 caracteres.");
            return "redirect:/esqueci-senha";
        }
        Optional<Usuario> resultado = usuarioRepository.findByEmailIgnoreCase(email.trim());
        if (resultado.isEmpty()) {
            redirect.addFlashAttribute("erro", "Não encontramos uma conta com esse e-mail.");
            return "redirect:/esqueci-senha";
        }
        Usuario usuario = resultado.get();
        usuario.setSenha(novaSenha);
        usuarioRepository.save(usuario);
        redirect.addFlashAttribute("sucesso", "Senha atualizada. Você já pode entrar.");
        return "redirect:/login";
    }

    @PostMapping("/sair")
    public String sair(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
