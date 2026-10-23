package com.horizon.Cosplayforge.dto;

import com.horizon.Cosplayforge.model.Categoria;

public record CategoriaResponse(Long id, String nome, String descricao) {
    public static CategoriaResponse de(Categoria categoria){
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao());
    }
}
