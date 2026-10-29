package com.horizon.Cosplayforge.service;

import com.horizon.Cosplayforge.client.SuperFreteClient;
import com.horizon.Cosplayforge.client.SuperFreteCotacaoRequest;
import com.horizon.Cosplayforge.client.SuperFreteOpcao;
import com.horizon.Cosplayforge.dto.FreteResponse;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.Carrinho;
import com.horizon.Cosplayforge.model.Endereco;
import com.horizon.Cosplayforge.model.ItemCarrinho;
import com.horizon.Cosplayforge.model.Loja;
import com.horizon.Cosplayforge.repository.CarrinhoRepository;
import com.horizon.Cosplayforge.repository.EnderecoRepository;
import com.horizon.Cosplayforge.repository.ItemCarrinhoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FreteService {

    /* Serviços são
    PAC = 1
    Sedex = 2
    Mini Envios 17
    A api vai dizer qual disponivel para cada pacote
     */
    private static final String SERVICOS = "1,2,17";

    private final SuperFreteClient superFreteClient;
    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final EnderecoRepository enderecoRepository;

    @Transactional(readOnly = true)
    public FreteResponse cotarCarrinho(UUID usuarioId, Long enderecoId) {
        Endereco destino = enderecoRepository.findByIdAndUsuarioId(enderecoId, usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Endereço de entrega não encontrado."));

        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Seu carrinho está vazio."));
        List<ItemCarrinho> itens = itemCarrinhoRepository.findByCarrinhoId(carrinho.getId());
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Seu carrinho está vazio.");
        }

        List<FreteResponse.LojaFrete> lojas = CarrinhoService.agruparPorLoja(itens).values().stream()
                .map(daLoja -> {
                    Loja loja = daLoja.get(0).getProduto().getLoja();
                    List<FreteResponse.Opcao> opcoes = cotarLoja(destino, daLoja).stream()
                            .map(o -> new FreteResponse.Opcao(o.id(), o.name(), o.price(), o.deliveryTime()))
                            .toList();
                    return new FreteResponse.LojaFrete(loja.getId(), loja.getNome(), opcoes);
                })
                .toList();

        return new FreteResponse(lojas);
    }

    /*
     * Cotação de UMA loja (= um pedido), da mais barata para a mais cara.
     * O PedidoService vai reutilizar este método para COBRAR o frete.
     * Não abre transação própria: quem chama precisa estar numa (o produto e a loja são carregados sob demanda).
     */
    public List<SuperFreteOpcao> cotarLoja(Endereco destino, List<ItemCarrinho> daLoja) {
        Loja loja = daLoja.get(0).getProduto().getLoja();

        List<SuperFreteCotacaoRequest.Item> produtos = daLoja.stream()
                .map(i -> new SuperFreteCotacaoRequest.Item(
                        i.getQuantidade(),
                        i.getProduto().getAlturaCm(),
                        i.getProduto().getComprimentoCm(),
                        i.getProduto().getLarguraCm(),
                        i.getProduto().getPesoKg()))
                .toList();

        SuperFreteCotacaoRequest requisicao = new SuperFreteCotacaoRequest(
                new SuperFreteCotacaoRequest.Local(loja.getEndereco().getCep()),
                new SuperFreteCotacaoRequest.Local(destino.getCep()),
                SERVICOS,
                SuperFreteCotacaoRequest.Opcoes.padrao(),
                produtos);

        return superFreteClient.cotar(requisicao).stream()
                .sorted(Comparator.comparing(SuperFreteOpcao::price))
                .toList();
    }
}
