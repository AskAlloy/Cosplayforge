package com.horizon.Cosplayforge.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.horizon.Cosplayforge.dto.LojaRequest;
import com.horizon.Cosplayforge.dto.LojaResponse;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.Endereco;
import com.horizon.Cosplayforge.model.Loja;
import com.horizon.Cosplayforge.model.StatusLoja;
import com.horizon.Cosplayforge.model.StatusPedido;
import com.horizon.Cosplayforge.repository.LojaRepository;
import com.horizon.Cosplayforge.repository.PedidoRepository;
import com.horizon.Cosplayforge.repository.UsuarioRepository;
import com.horizon.Cosplayforge.util.Textos;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository lojaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final MontadorEndereco montadorEndereco;

    // VENDEDOR

    @Transactional(readOnly = true)
    public LojaResponse buscarMinha(UUID usuarioId) {
        return LojaResponse.de(obterDoVendedor(usuarioId));
    }

    public LojaResponse criar(UUID usuarioId, LojaRequest req) {
        if (lojaRepository.findByProprietarioId(usuarioId).isPresent()) {
            throw new RegraNegocioException("Você já possui uma loja.");
        }

        Loja loja = Loja.builder()
                .proprietario(usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado.")))
                .endereco(new Endereco()) // sem usuario: é endereço de loja, não de cliente
                .build();               // status nasce EM_ANALISE (valor padrão da entidade)

        preencher(loja, req);
        return LojaResponse.de(lojaRepository.save(loja));
    }

    public LojaResponse atualizarMinha(UUID usuarioId, LojaRequest req) {
        Loja loja = obterDoVendedor(usuarioId);

        if (loja.getStatus() == StatusLoja.CANCELADA) {
            throw new RegraNegocioException("Loja cancelada não pode ser alterada.");
        }

        preencher(loja, req);
        // save() faz o UPDATE da loja e, por causa do cascade = ALL na entidade,
        // também do endereço dela.
        return LojaResponse.de(lojaRepository.save(loja));
    }

    // ADMINISTRADOR

    @Transactional(readOnly = true)
    public List<LojaResponse> listar(StatusLoja status) {
        Sort ordem = Sort.by("nome");
        List<Loja> lojas = (status == null)
                ? lojaRepository.findAll(ordem)
                : lojaRepository.findByStatus(status, ordem);
        return lojas.stream().map(LojaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public LojaResponse buscar(Long id) {
        return LojaResponse.de(obter(id));
    }

    @Transactional
    public LojaResponse alterarStatus(Long id, StatusLoja novoStatus) {
        Loja loja = obter(id);

        // A regra das transições mora no enum (StatusLoja.podeIrPara).
        if (!loja.getStatus().podeIrPara(novoStatus)) {
            throw new RegraNegocioException(
                    "Transição inválida de status da loja: " + loja.getStatus() + " -> " + novoStatus + ".");
        }

        // Cancelar é definitivo: não pode haver venda ainda em andamento.
        if (novoStatus == StatusLoja.CANCELADA
                && pedidoRepository.existePedidoAbertoVendedor(
                loja.getProprietario().getId(), StatusPedido.FINAIS)) {
            throw new RegraNegocioException("Não é possível cancelar a loja: existem pedidos em andamento.");
        }

        loja.setStatus(novoStatus);
        return LojaResponse.de(loja);
    }

    //  AUXILIARES

    private void preencher(Loja loja, LojaRequest req) {
        String cnpj = Textos.vazioParaNull(req.cnpj());
        validarCnpjUnico(cnpj, loja.getId());

        // Consulta o ViaCEP e valida o endereço ANTES de alterar o resto: se falhar,
        // a loja não fica meio atualizada.
        montadorEndereco.preencher(loja.getEndereco(), req.endereco());

        loja.setNome(req.nome().trim());
        loja.setDescricao(Textos.vazioParaNull(req.descricao()));
        loja.setCnpj(cnpj);
        loja.setEmailContato(req.emailContato().trim());
        loja.setTelefone(Textos.vazioParaNull(req.telefone()));
    }

    // idAtual é null ao criar (a loja ainda não tem id) e o id da própria loja ao
    // atualizar, para ela não "colidir" consigo mesma.
    private void validarCnpjUnico(String cnpj, Long idAtual) {
        if (cnpj == null) {
            return; // CNPJ é opcional
        }
        boolean emUso = (idAtual == null)
                ? lojaRepository.existsByCnpj(cnpj)
                : lojaRepository.existsByCnpjAndIdNot(cnpj, idAtual);
        if (emUso) {
            throw new RegraNegocioException("Já existe uma loja com este CNPJ.");
        }
    }

    private Loja obterDoVendedor(UUID usuarioId) {
        return lojaRepository.findByProprietarioId(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você ainda não possui uma loja."));
    }

    private Loja obter(Long id) {
        return lojaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Loja não encontrada."));
    }
}