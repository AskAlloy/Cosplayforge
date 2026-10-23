package com.horizon.Cosplayforge.controller.vendedor;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.horizon.Cosplayforge.dto.LojaRequest;
import com.horizon.Cosplayforge.dto.LojaResponse;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.LojaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendedor/loja")
@RequiredArgsConstructor
public class VendedorLojaController {

    private final LojaService lojaService;

    @GetMapping
    public LojaResponse minhaLoja(@AuthenticationPrincipal UsuarioLogado usuario) {
        return lojaService.buscarMinha(usuario.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LojaResponse criar(@AuthenticationPrincipal UsuarioLogado usuario,
                              @Valid @RequestBody LojaRequest requisicao) {
        return lojaService.criar(usuario.id(), requisicao);
    }

    @PutMapping
    public LojaResponse atualizar(@AuthenticationPrincipal UsuarioLogado usuario,
                                  @Valid @RequestBody LojaRequest requisicao) {
        return lojaService.atualizarMinha(usuario.id(), requisicao);
    }
}
