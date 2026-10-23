package com.horizon.Cosplayforge.service;

import com.horizon.Cosplayforge.dto.AdicionarItemRequest;
import com.horizon.Cosplayforge.dto.CarrinhoResponse;
import com.horizon.Cosplayforge.dto.CarrinhoResponse.*;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.*;
import com.horizon.Cosplayforge.repository.CarrinhoRepository;
import com.horizon.Cosplayforge.repository.ItemCarrinhoRepository;
import com.horizon.Cosplayforge.repository.ProdutoRepository;
import com.horizon.Cosplayforge.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CarrinhoResponse ver(UUID usuarioId){
        return montar(obterCarrinho(usuarioId));
    }

    @Transactional
    public CarrinhoResponse adicionar(UUID usuarioId, AdicionarItemRequest req) {
        Carrinho carrinho = obterCarrinho(usuarioId);
        Produto produto = produtoRepository.findById(req.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));

        // Se o produto já está no carrinho, SOMA a quantidade; senão cria o item com 0 e soma.
        ItemCarrinho item = itemCarrinhoRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produto.getId())
                .orElseGet(() -> ItemCarrinho.builder().carrinho(carrinho).produto(produto).quantidade(0).build());

        int novaQuantidade = item.getQuantidade() + req.quantidade();
        validarItem(produto, usuarioId, novaQuantidade);

        item.setQuantidade(novaQuantidade);
        itemCarrinhoRepository.save(item);
        return montar(carrinho);
    }

    //Alterar
    @Transactional
    public CarrinhoResponse alterarQuantidade(UUID usuarioId, Long produtoId, int quantidade) {
        Carrinho carrinho = obterCarrinho(usuarioId);
        ItemCarrinho item = obterItem(carrinho, produtoId);

        if (quantidade == 0) {
            itemCarrinhoRepository.delete(item);
            return montar(carrinho);
        }

        validarItem(item.getProduto(), usuarioId, quantidade);
        item.setQuantidade(quantidade);
        return montar(carrinho);
    }

    // remover

    @Transactional
    public CarrinhoResponse remover(UUID usuarioId, Long produtoId) {
        Carrinho carrinho = obterCarrinho(usuarioId);
        itemCarrinhoRepository.delete(obterItem(carrinho, produtoId));
        return montar(carrinho);
    }

    @Transactional
    public CarrinhoResponse limpar(UUID usuarioId) {
        Carrinho carrinho = obterCarrinho(usuarioId);
        itemCarrinhoRepository.deleteByCarrinhoId(carrinho.getId());
        return montar(carrinho);
    }

    public void validarItem(Produto produto, UUID usuarioId, int quantidade) {
        if (produto.getStatus() != StatusProduto.ATIVO || produto.getLoja().getStatus() != StatusLoja.ATIVA) {
            throw new RegraNegocioException("O produto \"" + produto.getNome() + "\" não está disponível no momento.");
        }
        if (produto.getLoja().getProprietario().getId().equals(usuarioId)) {
            throw new RegraNegocioException("Você não pode comprar produtos da sua própria loja.");
        }
        if (quantidade > produto.getQuantidadeEstoque()) {
            throw new RegraNegocioException("Estoque insuficiente para \"" + produto.getNome()
                    + "\": disponível " + produto.getQuantidadeEstoque() + ".");
        }
    }

    /** Separa os itens por loja, mantendo a ordem em que apareceram.
     * A chave é o id da loja. */
    public static Map<Long, List<ItemCarrinho>> agruparPorLoja(List<ItemCarrinho> itens) {
        return itens.stream().collect(Collectors.groupingBy(
                i -> i.getProduto().getLoja().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    // Metodos

    private Carrinho obterCarrinho(UUID usuarioId) {
        return carrinhoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> carrinhoRepository.save(
                        Carrinho.builder().usuario(usuarioRepository.getReferenceById(usuarioId)).build()));
    }

    private ItemCarrinho obterItem(Carrinho carrinho, Long produtoId) {
        return itemCarrinhoRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado no carrinho."));
    }

    private CarrinhoResponse montar(Carrinho carrinho) {
        List<GrupoLoja> grupos = agruparPorLoja(itemCarrinhoRepository.findByCarrinhoId(carrinho.getId()))
                .values().stream().map(lista -> {
                    List<Item> itens = lista.stream().map(this::paraItem).toList();
                    Loja loja = lista.get(0).getProduto().getLoja();
                    return new GrupoLoja(loja.getId(), loja.getNome(), itens, somar(itens));
                }).toList();

        BigDecimal total = grupos.stream().map(GrupoLoja::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarrinhoResponse(grupos, total);
    }

    private Item paraItem(ItemCarrinho i) {
        Produto p = i.getProduto();
        boolean disponivel = p.getStatus() == StatusProduto.ATIVO
                && p.getLoja().getStatus() == StatusLoja.ATIVA
                && i.getQuantidade() <= p.getQuantidadeEstoque();
        return new CarrinhoResponse.Item(p.getId(), p.getNome(), p.getImagem(), p.getPreco(), i.getQuantidade(),
                p.getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())), disponivel, p.getQuantidadeEstoque());
    }

    private BigDecimal somar(List<Item> itens) {
        return itens.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
