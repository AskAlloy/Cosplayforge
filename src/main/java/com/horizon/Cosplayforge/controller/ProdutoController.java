package com.horizon.Cosplayforge.controller;

import com.horizon.Cosplayforge.dto.PaginaResponse;
import com.horizon.Cosplayforge.dto.ProdutoResponse;
import com.horizon.Cosplayforge.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public PaginaResponse<ProdutoResponse> pesquisar(
            @RequestParam(name = "termo", required = false) String termo,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "precoMin", required = false) BigDecimal precoMin,
            @RequestParam(name = "precoMax", required = false) BigDecimal precoMax,
            @RequestParam(name = "page", defaultValue = "0") int pagina,
            @RequestParam(name = "size", defaultValue = "12") int tamanho) {
        return produtoService.pesquisar(termo, categoriaId, precoMin, precoMax, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable("id") Long id) {
        return produtoService.buscarPublico(id);
    }
}
