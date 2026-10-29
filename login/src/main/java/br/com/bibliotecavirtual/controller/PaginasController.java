package br.com.bibliotecavirtual.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class PaginasController {
    @GetMapping("/") public String raiz() { return "redirect:/login"; }
    @GetMapping("/login") public String login() { return "Login"; }
    @GetMapping("/cadastro") public String cadastro() { return "Cadastro"; }
    @GetMapping("/esqueci-senha") public String esqueciSenha() { return "EsqueciSenha"; }
}
