package com.horizon.Cosplayforge.service;


// Regras de negocio ficam aqui

import com.horizon.Cosplayforge.dto.CategoriaRequest;
import com.horizon.Cosplayforge.dto.CategoriaResponse;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.Categoria;
import com.horizon.Cosplayforge.repository.CategoriaRepository;
import com.horizon.Cosplayforge.repository.ProdutoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    // CRUD
    @Transactional(readOnly = true) // aqui garante que é só pra ler
    public List<CategoriaResponse> listar(){
        return categoriaRepository.findAll(Sort.by("nome"))
                .stream()
                .map(CategoriaResponse::de)
                .toList();
    }
    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.de(obter(id));
    }


    // Transactional grava no banco. Se qualquer exceção acontecer
    // no meio, TUDO é desfeito (rollback), nada fica pela metade
    // TEORICAMENTE

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        String nome = req.nome().trim();
        // trim vai tirar os espaços nas postas " Carros " == "Carros"

        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Já existe uma categoria com este nome.");
        }
        Categoria categoria = Categoria.builder()
                .nome(nome)
                .descricao(limpar(req.descricao()))
                .build();

        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Categoria categoria = obter(id);
        String novoNome = req.nome().trim();

        boolean nomeMudou = !categoria.getNome().equalsIgnoreCase(novoNome);
        if (nomeMudou && categoriaRepository.existsByNomeIgnoreCase(novoNome)) {
            throw new RegraNegocioException("Já existe uma categoria com este nome.");
        }

        categoria.setNome(novoNome);
        categoria.setDescricao(limpar(req.descricao()));

        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = obter(id);

        // não se exclui categoria que ainda tem produto.
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir: existem produtos nesta categoria.");
        }

        categoriaRepository.delete(categoria);
    }





    private String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        } else {
            return texto.trim();
        }
    }

    private Categoria obter(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(()-> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }
}
