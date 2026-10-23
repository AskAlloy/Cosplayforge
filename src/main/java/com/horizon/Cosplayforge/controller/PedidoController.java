package com.horizon.Cosplayforge.controller;

import com.horizon.Cosplayforge.dto.CriarPedidoRequest;
import com.horizon.Cosplayforge.dto.PedidoResponse;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<PedidoResponse> criar(@AuthenticationPrincipal UsuarioLogado usuario,
                                      @Valid @RequestBody CriarPedidoRequest requisicao){
        return pedidoService.criar(usuario.id(),requisicao);
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario) {
        return pedidoService.listarDoCliente(usuario.id());
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id) {
        return pedidoService.buscarDoCliente(usuario.id(), id);
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id) {
        return pedidoService.cancelar(usuario.id(), id);
    }

}
