package com.horizon.Cosplayforge.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.horizon.Cosplayforge.dto.EnderecoRequest;
import com.horizon.Cosplayforge.dto.EnderecoResponse;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.model.Endereco;
import com.horizon.Cosplayforge.repository.EnderecoRepository;
import com.horizon.Cosplayforge.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MontadorEndereco montadorEndereco;

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(UUID usuarioId) {
        return enderecoRepository.findByUsuarioId(usuarioId).stream()
                .map(EnderecoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EnderecoResponse buscar(Long id, UUID usuarioId) {
        return EnderecoResponse.de(obterDoUsuario(id, usuarioId));
    }

    public EnderecoResponse criar(UUID usuarioId, EnderecoRequest req) {
        Endereco endereco = new Endereco();
        // getReferenceById liga o endereço ao usuário só pelo id, sem SELECT.
        endereco.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        montadorEndereco.preencher(endereco, req);
        return EnderecoResponse.de(enderecoRepository.save(endereco));
    }

    public EnderecoResponse atualizar(Long id, UUID usuarioId, EnderecoRequest req) {
        Endereco endereco = obterDoUsuario(id, usuarioId);
        montadorEndereco.preencher(endereco, req);
        return EnderecoResponse.de(enderecoRepository.save(endereco));
    }

    @Transactional
    public void excluir(Long id, UUID usuarioId) {
        enderecoRepository.delete(obterDoUsuario(id, usuarioId));
    }

    // Endereço de outro usuário responde 404 (e não 403), para não revelar que o id existe.
    private Endereco obterDoUsuario(Long id, UUID usuarioId) {
        return enderecoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado."));
    }
}