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
        return "vitrine";
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

    @GetMapping("/carrinho")
    public String carrinho(){
        return "carrinho";
    }

    @GetMapping("/pedidos")
    public String pedidos(){
        return "pedidos";
    }

    @GetMapping("/enderecos")
    public String enderecos(){
        return "enderecos";
    }
}
