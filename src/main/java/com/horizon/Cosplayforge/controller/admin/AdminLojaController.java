package com.horizon.Cosplayforge.controller.admin;

import com.horizon.Cosplayforge.dto.AlterarStatusLojaRequest;
import com.horizon.Cosplayforge.dto.LojaResponse;
import com.horizon.Cosplayforge.model.StatusLoja;
import com.horizon.Cosplayforge.service.LojaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lojas")
@RequiredArgsConstructor
public class AdminLojaController {

    private final LojaService lojaService;

    @GetMapping
    public List<LojaResponse> listar(
            @RequestParam(name = "status", required = false) StatusLoja status) {
        return lojaService.listar(status);
    }
    @GetMapping("/{id}")
    public LojaResponse buscar(@PathVariable("id") Long id) {
        return lojaService.buscar(id);
    }

    @PutMapping("/{id}/status")
    public LojaResponse alterarStatus(@PathVariable("id") Long id,
                                      @Valid @RequestBody AlterarStatusLojaRequest requisicao) {
        return lojaService.alterarStatus(id, requisicao.status());
    }
}
