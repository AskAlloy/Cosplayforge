package com.horizon.Cosplayforge.controller;

import com.horizon.Cosplayforge.dto.AdicionarItemRequest;
import com.horizon.Cosplayforge.dto.CarrinhoResponse;
import com.horizon.Cosplayforge.dto.QuantidadeRequest;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.CarrinhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @GetMapping
    public CarrinhoResponse ver(@AuthenticationPrincipal UsuarioLogado usuario) {
        return carrinhoService.ver(usuario.id());
    }

    @PostMapping("/itens")
    public CarrinhoResponse adicionar(@AuthenticationPrincipal UsuarioLogado usuario,
                                      @Valid @RequestBody AdicionarItemRequest requisicao) {
        return carrinhoService.adicionar(usuario.id(), requisicao);
    }

    @PutMapping("/itens/{produtoId}")
    public CarrinhoResponse alterarQuantidade(@AuthenticationPrincipal UsuarioLogado usuario,
                                              @PathVariable("produtoId") Long produtoId, @Valid @RequestBody QuantidadeRequest requisicao) {
        return carrinhoService.alterarQuantidade(usuario.id(), produtoId, requisicao.quantidade());
    }

    @DeleteMapping("/itens/{produtoId}")
    public CarrinhoResponse remover(@AuthenticationPrincipal UsuarioLogado usuario,
                                    @PathVariable("produtoId") Long produtoId) {
        return carrinhoService.remover(usuario.id(), produtoId);
    }

    @DeleteMapping
    public CarrinhoResponse limpar(@AuthenticationPrincipal UsuarioLogado usuario) {
        return carrinhoService.limpar(usuario.id());
    }


}// final controller
