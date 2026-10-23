package com.horizon.Cosplayforge.controller.vendedor;

import com.horizon.Cosplayforge.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.ProdutoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendedor/produtos")
@RequiredArgsConstructor
public class VendedorProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public PaginaResponse<ProdutoResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario,
                                                  @RequestParam(name = "page", defaultValue = "0") int pagina,
                                                  @RequestParam(name = "size", defaultValue = "12") int tamanho) {
        return produtoService.listarMeus(usuario.id(), pagina, tamanho);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id) {
        return produtoService.buscarMeu(usuario.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@AuthenticationPrincipal UsuarioLogado usuario,
                                 @Valid @RequestBody ProdutoRequest requisicao) {
        return produtoService.criar(usuario.id(), requisicao);
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id,
                                     @Valid @RequestBody ProdutoRequest requisicao) {
        return produtoService.atualizar(usuario.id(), id, requisicao);
    }

    // PUT /api/vendedor/produtos/{id}/status  {"status": "INATIVO"}
    @PutMapping("/{id}/status")
    public ProdutoResponse alterarStatus(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id,
                                         @Valid @RequestBody AlterarStatusProdutoRequest requisicao) {
        return produtoService.alterarStatus(usuario.id(), id, requisicao.status());
    }

    // PUT /api/vendedor/produtos/{id}/estoque  {"quantidade": 25}
    @PutMapping("/{id}/estoque")
    public ProdutoResponse ajustarEstoque(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id,
                                          @Valid @RequestBody QuantidadeRequest requisicao) {
        return produtoService.ajustarEstoque(usuario.id(), id, requisicao.quantidade());
    }

    @DeleteMapping("/{id}")
    public MensagemResponse excluir(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id) {
        produtoService.excluir(usuario.id(), id);
        return new MensagemResponse("Produto excluído com sucesso.");
    }
}
