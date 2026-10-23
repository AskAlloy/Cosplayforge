package com.horizon.Cosplayforge.service;


import java.math.BigDecimal;
import java.util.UUID;

import com.horizon.Cosplayforge.model.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.horizon.Cosplayforge.dto.PaginaResponse;
import com.horizon.Cosplayforge.dto.ProdutoRequest;
import com.horizon.Cosplayforge.dto.ProdutoResponse;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.repository.CategoriaRepository;
import com.horizon.Cosplayforge.repository.ItemCarrinhoRepository;
import com.horizon.Cosplayforge.repository.ItemPedidoRepository;
import com.horizon.Cosplayforge.repository.LojaRepository;
import com.horizon.Cosplayforge.repository.ProdutoRepository;
import com.horizon.Cosplayforge.util.Textos;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {
    private static final int TAMANHO_MAXIMO_PAGINA = 50;

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LojaRepository lojaRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    // catalogo publico

    @Transactional
    public PaginaResponse<ProdutoResponse> pesquisar(String termo, Long categoriaId,
                                                     BigDecimal precoMin, BigDecimal precoMax, int pagina, int tamanho)
    {
        return PaginaResponse.de(
                produtoRepository.pesquisar(Textos.vazioParaNull(termo), categoriaId,precoMin,precoMax,
                paginar(pagina, tamanho)),
                ProdutoResponse::de);
    }
    @Transactional(readOnly = true)
    public ProdutoResponse buscarPublico(Long id) {
        Produto produto = produtoRepository.findById(id)
                .filter(p -> p.getStatus() == StatusProduto.ATIVO
                        && p.getLoja().getStatus() == StatusLoja.ATIVA)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        return ProdutoResponse.de(produto);
    }

    // para vendedor Buscar Produtos

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> listarMeus(UUID usuarioId, int pagina, int tamanho) {
        Loja loja = lojaDoVendedor(usuarioId);
        return PaginaResponse.de(produtoRepository.findByLojaId(loja.getId(), paginar(pagina, tamanho)),
                ProdutoResponse::de);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarMeu(UUID usuarioId, Long id) {
        return ProdutoResponse.de(obterDoVendedor(usuarioId, id));
    }

    @Transactional
    public ProdutoResponse criar(UUID usuarioId, ProdutoRequest req) {
        Loja loja = lojaDoVendedor(usuarioId);

        if (loja.getStatus() != StatusLoja.ATIVA) {
            throw new RegraNegocioException("Sua loja precisa estar ATIVA para cadastrar produtos.");
        }
        Produto produto = Produto.builder().loja(loja).build();
        preencher(produto, req);

        produto.setQuantidadeEstoque(req.quantidadeEstoque() == null ? 0 : req.quantidadeEstoque());
        return ProdutoResponse.de(produtoRepository.save(produto));
        }

    @Transactional
    public ProdutoResponse atualizar(UUID usuarioId, Long id, ProdutoRequest req) {
        Produto produto = obterDoVendedor(usuarioId, id);
        preencher(produto, req);
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse alterarStatus(UUID usuarioId, Long id, StatusProduto status) {
        Produto produto = obterDoVendedor(usuarioId, id);
        produto.setStatus(status);
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse ajustarEstoque(UUID usuarioId, Long id, int quantidade) {
        Produto produto = obterDoVendedor(usuarioId, id);
        produto.setQuantidadeEstoque(quantidade);
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public void excluir(UUID usuarioId, Long id) {
        Produto produto = obterDoVendedor(usuarioId, id);

        if (itemPedidoRepository.existsByProdutoId(id)) {
            throw new RegraNegocioException(
                    "Este produto já possui pedidos e não pode ser excluído. Desative-o.");
        }
        itemCarrinhoRepository.deleteByProdutoId(id); // tira dos carrinhos
        produtoRepository.delete(produto);
    }

    //
    private void preencher(Produto produto, ProdutoRequest req) {
        produto.setCategoria(categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada.")));
        produto.setNome(req.nome().trim());
        produto.setDescricao(req.descricao().trim());
        produto.setPreco(req.preco());
        produto.setImagem(Textos.vazioParaNull(req.imagem()));
        produto.setPersonalizavel(Boolean.TRUE.equals(req.personalizavel()));
        produto.setPesoKg(req.pesoKg());
        produto.setAlturaCm(req.alturaCm());
        produto.setLarguraCm(req.larguraCm());
        produto.setComprimentoCm(req.comprimentoCm());
    }
    private Loja lojaDoVendedor(UUID usuarioId) {
        return lojaRepository.findByProprietarioId(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Cadastre sua loja antes de gerenciar produtos."));
    }
    // Produto de outras lojas não aparecem so da mesma

    private Produto obterDoVendedor(UUID usuarioId, Long id) {
        Loja loja = lojaDoVendedor(usuarioId);
        return produtoRepository.findByIdAndLojaId(id, loja.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }

    private Pageable paginar(int pagina, int tamanho) {
        return PageRequest.of(Math.max(pagina, 0),
                Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA), Sort.by("nome"));
    }

}// final do Service
