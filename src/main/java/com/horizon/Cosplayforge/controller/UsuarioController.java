package com.horizon.Cosplayforge.controller;

import com.horizon.Cosplayforge.dto.AtualizarUsuarioRequest;
import com.horizon.Cosplayforge.dto.ConfirmarSenhaRequest;
import com.horizon.Cosplayforge.dto.MensagemResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.horizon.Cosplayforge.dto.UsuarioResponse;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.UsuarioService;
 
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
 
    private final UsuarioService usuarioService;
 
    // O id vem do token, nunca da URL: ninguém consulta os dados de outro usuário por aqui.
    @GetMapping("/me")
    public UsuarioResponse meusDados(@AuthenticationPrincipal UsuarioLogado usuario) {
        return usuarioService.buscarPorId(usuario.id());
    }

    @PutMapping("/me")
    public UsuarioResponse atualizarMeusDados(@AuthenticationPrincipal UsuarioLogado usuario,
                                              @Valid @RequestBody AtualizarUsuarioRequest requisicao) {
        return usuarioService.atualizar(usuario.id(), requisicao);
    }

    @DeleteMapping("/me")
    public MensagemResponse inativarMinhaConta(@AuthenticationPrincipal UsuarioLogado usuario,
                                               @Valid @RequestBody ConfirmarSenhaRequest requisicao) {
        usuarioService.desativarPropriaConta(usuario.id(), requisicao.senha());
        return new MensagemResponse("Conta desativada com sucesso.");
    }

}
