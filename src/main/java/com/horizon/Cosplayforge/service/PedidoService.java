package com.horizon.Cosplayforge.service;

import com.horizon.Cosplayforge.dto.AtualizarStatusPedidoRequest;
import com.horizon.Cosplayforge.dto.CriarPedidoRequest;
import com.horizon.Cosplayforge.dto.PedidoResponse;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.*;
import com.horizon.Cosplayforge.repository.*;
import com.horizon.Cosplayforge.util.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private static final String FRETE_A_DEFINIR = "A DEFINIR";

    private static final Set<StatusPedido> STATUS_DO_VENDEDOR = EnumSet.of(
            StatusPedido.EM_PRODUCAO, StatusPedido.PRONTO, StatusPedido.ENVIADO, StatusPedido.ENTREGUE);

    private final PedidoRepository pedidoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoRepository produtoRepository;
    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LojaRepository lojaRepository;
    private final CarrinhoService carrinhoService;

    // Aba para os clientes

    @Transactional
    public List<PedidoResponse> criar(UUID usuarioId, CriarPedidoRequest req) {
        Endereco enderecoCliente = enderecoRepository.findByIdAndUsuarioId(req.enderecoId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Endereço de entrega não encontrado."));

        Carrinho carrinho = carrinhoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Seu carrinho está vazio."));
        List<ItemCarrinho> itens = itemCarrinhoRepository.findByCarrinhoId(carrinho.getId());
        if (itens.isEmpty()) {
            throw new RegraNegocioException("Seu carrinho está vazio.");
        }

        // caso o carrinho seja velho isso valida se o produto ainda esta ativo
        itens.forEach(i -> carrinhoService.validarItem(i.getProduto(), usuarioId, i.getQuantidade()));

        Usuario cliente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        List<Pedido> pedidos = new ArrayList<>();
        CarrinhoService.agruparPorLoja(itens).values().forEach(daLoja -> pedidos.add(montarPedido(cliente, daLoja)));

        // para clientes disputando o ultimo pacote

        itens.stream().sorted(Comparator.comparing(i -> i.getProduto().getId())).forEach(i -> {
            if (produtoRepository.baixarEstoque(i.getProduto().getId(), i.getQuantidade()) == 0) {
                throw new RegraNegocioException("Estoque insuficiente para \"" + i.getProduto().getNome()
                        + "\". Atualize seu carrinho.");
            }
        });

        // salvar o historico do endereco, se o cliente editar ou apagar o endereço dele depois.

        Endereco entrega = enderecoRepository.save(copiar(enderecoCliente));
        pedidos.forEach(p -> {
            p.setEnderecoEntrega(entrega);
            pedidoRepository.save(p);
        });

        //compra feita, esvazie o carrinho

        itemCarrinhoRepository.deleteByCarrinhoId(carrinho.getId());

        return pedidos.stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDoCliente(UUID usuarioId) {
        return pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(usuarioId).stream()
                .map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarDoCliente(UUID usuarioId, Long id) {
        return PedidoResponse.de(obterDoCliente(usuarioId, id));
    }

    // para recusar o pedido somente caso pagamento recusado ou pendente

    @Transactional
    public PedidoResponse cancelar(UUID usuarioId, Long id) {
        Pedido pedido = obterDoCliente(usuarioId, id);
        mudarStatus(pedido, StatusPedido.CANCELADO);
        pedido.getItens().forEach(i ->
                produtoRepository.devolverEstoque(i.getProduto().getId(), i.getQuantidade()));
        return PedidoResponse.de(pedido);
    }

    // VENDEDOR

    // Listar
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDaLoja(UUID usuarioId, StatusPedido status) {
        Long lojaId = lojaDoVendedor(usuarioId).getId();
        List<Pedido> pedidos = (status == null)
                ? pedidoRepository.findByLojaIdOrderByDataCriacaoDesc(lojaId)
                : pedidoRepository.findByLojaIdAndStatusOrderByDataCriacaoDesc(lojaId, status);
        return pedidos.stream().map(PedidoResponse::de).toList();
    }

    //buscar
    @Transactional(readOnly = true)
    public PedidoResponse buscarDaLoja(UUID usuarioId, Long id) {
        return PedidoResponse.de(obterDaLoja(usuarioId, id));
    }


    //atualizar
    @Transactional
    public PedidoResponse atualizarStatus(UUID usuarioId, Long id, AtualizarStatusPedidoRequest req) {
        Pedido pedido = obterDaLoja(usuarioId, id);
        StatusPedido novo = req.status();

        if (!STATUS_DO_VENDEDOR.contains(novo)) {
            throw new RegraNegocioException(
                    "O vendedor só pode definir EM_PRODUCAO, PRONTO, ENVIADO ou ENTREGUE.");
        }
        String rastreio = Textos.vazioParaNull(req.codigoRastreio());
        if (novo == StatusPedido.ENVIADO && rastreio == null) {
            throw new RegraNegocioException("Informe o código de rastreio para marcar como ENVIADO.");
        }

        mudarStatus(pedido, novo);
        if (novo == StatusPedido.ENVIADO) {
            pedido.setCodigoRastreio(rastreio);
        }
        return PedidoResponse.de(pedido);
    }

    // Metodos

    private Pedido montarPedido(Usuario cliente, List<ItemCarrinho> daLoja) {
        Loja loja = daLoja.get(0).getProduto().getLoja();

        BigDecimal valorProdutos = daLoja.stream()
                .map(i -> i.getProduto().getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .loja(loja)
                .valorProdutos(valorProdutos)

                // cotacao para superfrete
                .valorFrete(BigDecimal.ZERO)
                .valorTotal(valorProdutos)
                .freteServico(FRETE_A_DEFINIR)
                .fretePrazoDias(0)
                .build();

    // Preço não muda se a loja trocar depois do pedido
        daLoja.forEach(i -> pedido.getItens().add(ItemPedido.builder()
                .pedido(pedido).produto(i.getProduto()).quantidade(i.getQuantidade())
                .precoUnitario(i.getProduto().getPreco()).build()));
        return pedido;

    }



    private Endereco copiar(Endereco end) {
        Endereco cliente = new Endereco();
        cliente.setCep(end.getCep());
        cliente.setLogradouro(end.getLogradouro());
        cliente.setNumero(end.getNumero());
        cliente.setComplemento(end.getComplemento());
        cliente.setBairro(end.getBairro());
        cliente.setCidade(end.getCidade());
        cliente.setUf(end.getUf());
        return cliente;

    }

    private void mudarStatus(Pedido pedido, StatusPedido novo) {
        if (!pedido.getStatus().podeIrPara(novo)) {
            throw new RegraNegocioException(
                    "Transição inválida de status do pedido: " + pedido.getStatus() + " -> " + novo + ".");
        }
        pedido.setStatus(novo);
    }

    private Pedido obterDoCliente(UUID usuarioId, Long id) {
        return pedidoRepository.findByIdAndClienteId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    private Pedido obterDaLoja(UUID usuarioId, Long id) {
        return pedidoRepository.findByIdAndLojaId(id, lojaDoVendedor(usuarioId).getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado."));
    }

    private Loja lojaDoVendedor(UUID usuarioId) {
        return lojaRepository.findByProprietarioId(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Você ainda não possui uma loja."));
    }

}

