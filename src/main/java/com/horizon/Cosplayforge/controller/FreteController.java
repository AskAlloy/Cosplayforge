package com.horizon.Cosplayforge.controller;

import com.horizon.Cosplayforge.dto.FreteResponse;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.FreteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/frete")
@RequiredArgsConstructor
public class FreteController {

    private final FreteService freteService;

    @GetMapping("/cotacao")
    public FreteResponse cotar (@AuthenticationPrincipal UsuarioLogado usuario,
                                @RequestParam("enderecoId") Long enderecoId){
        return freteService.cotarCarrinho(usuario.id(),enderecoId);
    }
}
