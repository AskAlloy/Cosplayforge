package com.horizon.Cosplayforge.controller;

import com.horizon.Cosplayforge.dto.EnderecoRequest;
import com.horizon.Cosplayforge.dto.EnderecoResponse;
import com.horizon.Cosplayforge.dto.MensagemResponse;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping
    public List<EnderecoResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario) {
        return enderecoService.listar(usuario.id());
    }


    @GetMapping("/{id}")
    public EnderecoResponse buscar(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal UsuarioLogado usuario) {
        return enderecoService.buscar(id, usuario.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnderecoResponse criar(@AuthenticationPrincipal UsuarioLogado usuario,
                                  @Valid @RequestBody EnderecoRequest requisicao) {
        return enderecoService.criar(usuario.id(), requisicao);
    }

    // PUT /api/enderecos/{id} -> altera todos os campos do endereço.
    @PutMapping("/{id}")
    public EnderecoResponse atualizar(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal UsuarioLogado usuario,
                                      @Valid @RequestBody EnderecoRequest requisicao) {
        return enderecoService.atualizar(id, usuario.id(), requisicao);
    }

    // DELETE /api/enderecos/{id} -> exclui (se não estiver em nenhum pedido).
    @DeleteMapping("/{id}")
    public MensagemResponse excluir(@PathVariable("id") Long id,
                                    @AuthenticationPrincipal UsuarioLogado usuario) {
        enderecoService.excluir(id, usuario.id());
        return new MensagemResponse("Endereço excluído com sucesso.");
    }
}
