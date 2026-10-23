package com.horizon.Cosplayforge.controller.admin;

import java.util.List;
import java.util.UUID;

import com.horizon.Cosplayforge.dto.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.horizon.Cosplayforge.model.PerfilUsuario;
import com.horizon.Cosplayforge.security.UsuarioLogado;
import com.horizon.Cosplayforge.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

	private final UsuarioService usuarioService;

	@GetMapping
	public List<UsuarioResponse> listar(@RequestParam(name = "perfil", required = false) PerfilUsuario perfil) {
		return usuarioService.listar(perfil);
	}

	@GetMapping("/{id}")
	public UsuarioResponse buscar(@PathVariable("id") UUID id) {
		return usuarioService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public UsuarioResponse atualizar(@PathVariable("id") UUID id,
									 @Valid @RequestBody AtualizarUsuarioRequest requisicao) {
		return usuarioService.atualizar(id, requisicao);
	}

	@PutMapping("/{id}/perfil")
	public UsuarioResponse alterarPerfil(@PathVariable("id") UUID id,
										 @Valid @RequestBody AlterarPerfilRequest requisicao,
										 @AuthenticationPrincipal UsuarioLogado admin) {
		return usuarioService.alterarPerfil(id, requisicao.perfil(), admin.id());
	}

	@DeleteMapping("/{id}")
	public MensagemResponse inativar(@PathVariable("id") UUID id) {
		usuarioService.desativarPorAdmin(id);
		return new MensagemResponse("Conta inativada com sucesso.");
	}

	@PostMapping("/{id}/reativar")
	public UsuarioResponse reativar(@PathVariable("id") UUID id) {
		return usuarioService.reativar(id);
	}
}
