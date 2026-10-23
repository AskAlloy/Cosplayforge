package com.horizon.Cosplayforge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/home")
    public String inicio() {
        return "home/index";
    }

    @GetMapping("/")
    public String vitrine() {

        return "home/vitrine";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {

        return "cadastro";
    }

    @GetMapping("/admin/painel")
    public String painelAdmin() {

        return "admin/painel";
    }

    @GetMapping("/vendedor/painel")
    public String painelVendedor() {
        return "vendedor/painel";
    }


}
