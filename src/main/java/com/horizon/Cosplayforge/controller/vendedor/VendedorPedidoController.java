package com.horizon.Cosplayforge.controller.vendedor;

import com.horizon.Cosplayforge.dto.AtualizarStatusPedidoRequest;
import com.horizon.Cosplayforge.dto.PedidoResponse;
import com.horizon.Cosplayforge.model.StatusPedido;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendedor/pedidos")
public class VendedorPedidoController {

    private final PedidoService pedidoService;

    public VendedorPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal UsuarioLogado usuario,
                                       @RequestParam(name = "status", required = false) StatusPedido status) {
        return pedidoService.listarDaLoja(usuario.id(), status);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@AuthenticationPrincipal UsuarioLogado usuario, @PathVariable("id") Long id) {
        return pedidoService.buscarDaLoja(usuario.id(), id);
    }

    @PutMapping("/{id}/status")
    public PedidoResponse atualizarStatus(@AuthenticationPrincipal UsuarioLogado usuario,
                                          @PathVariable("id") Long id, @Valid @RequestBody AtualizarStatusPedidoRequest requisicao) {
        return pedidoService.atualizarStatus(usuario.id(), id, requisicao);
    }
}
