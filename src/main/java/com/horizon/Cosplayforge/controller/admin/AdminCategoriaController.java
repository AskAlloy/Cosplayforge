package com.horizon.Cosplayforge.controller.admin;

import com.horizon.Cosplayforge.dto.CategoriaRequest;
import com.horizon.Cosplayforge.dto.CategoriaResponse;
import com.horizon.Cosplayforge.dto.MensagemResponse;
import com.horizon.Cosplayforge.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categorias")
public class AdminCategoriaController {

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    private final CategoriaService categoriaService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest requisicao) {
        return categoriaService.criar(requisicao);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable("id") Long id,
                                       @Valid @RequestBody CategoriaRequest requisicao) {
        return categoriaService.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    public MensagemResponse excluir(@PathVariable("id") Long id) {
        categoriaService.excluir(id);
        return new MensagemResponse("Categoria excluída com sucesso.");
    }

}
