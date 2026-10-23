package com.horizon.Cosplayforge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.horizon.Cosplayforge.dto.AtualizarUsuarioRequest;
import com.horizon.Cosplayforge.dto.UsuarioResponse;
import com.horizon.Cosplayforge.exception.RecursoNaoEncontradoException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.PerfilUsuario;
import com.horizon.Cosplayforge.model.StatusPedido;
import com.horizon.Cosplayforge.model.Usuario;
import com.horizon.Cosplayforge.repository.LojaRepository;
import com.horizon.Cosplayforge.repository.PedidoRepository;
import com.horizon.Cosplayforge.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {
 
	//Pedidos já terminaram
	
	private static final List<StatusPedido> STATUS_FINAIS =
			List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO);
	
	private final UsuarioRepository usuarioRepository;
	private final PedidoRepository pedidoRepository;
	private final LojaRepository lojaRepository;
	private final PasswordEncoder passwordEncoder;
	
	// Para consultar
	@Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        return UsuarioResponse.de(obter(id));
    }
 
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(PerfilUsuario perfil) {
    	List<Usuario> usuarios = (perfil == null)
    			? usuarioRepository.findAll()
    			: usuarioRepository.findByPerfil(perfil);
    	return usuarios.stream().map(UsuarioResponse::de).toList();
    }
    
    // Alterar coisas
    @Transactional
    public UsuarioResponse atualizar(UUID id, AtualizarUsuarioRequest req) {
    Usuario usuario = obter(id);

    
    //conferir nome mudou ou não
    // para não acontecer de falar que tem 2 Nome de usuarios iguais quando mudar o telefone por exemplo.

    boolean mudou = !usuario.getNomeUsuario().equalsIgnoreCase(req.nomeUsuario());
    if (mudou && usuarioRepository.existsByNomeUsuario(req.nomeUsuario())) {
        throw new RegraNegocioException("Este nome de usuário já está em uso.");
    }
    
    usuario.setNome(req.nome().trim());
    usuario.setNomeUsuario(req.nomeUsuario());
    usuario.setTelefone(req.telefone());
    return UsuarioResponse.de(usuario);
    }

    //Aqui o Adm que muda o PERFIL DE ACESSO (Cliente,vendedor,adm)
    // ADM não pode mudar o Proprio Perfil apenas outros
    @Transactional
    public UsuarioResponse alterarPerfil(UUID alvoId, PerfilUsuario novoPerfil, UUID AdminId){
        if (alvoId.equals(AdminId)) {
            throw new RegraNegocioException("Você não pode alterar o seu próprio perfil.");
        }
        Usuario usuario = obter(alvoId);

        if (usuario.getPerfil() == novoPerfil) {
            throw new RegraNegocioException("O usuário já possui este perfil.");
        }
        // Quem tem loja precisa continuar sendo vendedor, senão a loja fica sem dono válido.
        if (novoPerfil != PerfilUsuario.VENDEDOR && lojaRepository.findByProprietarioId(alvoId).isPresent()) {
            throw new RegraNegocioException("O usuário possui uma loja cadastrada e deve permanecer VENDEDOR.");
        }

        usuario.setPerfil(novoPerfil);
        return UsuarioResponse.de(usuario);

    }// Fim alterar Perfil

    // Desativar Conta
    @Transactional
    public void desativarPropriaConta(UUID id, String senha) {
        Usuario usuario = obter(id);
        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new RegraNegocioException("Senha incorreta.");
        }
        desativar(usuario);
    }

    // administrador desativa a conta de outro usuário.
    @Transactional
    public void desativarPorAdmin(UUID alvoId) {
        desativar(obter(alvoId));
    }

    @Transactional
    public UsuarioResponse reativar(UUID id) {
        Usuario usuario = obter(id);
        if (!usuario.estaInativo()) {
            throw new RegraNegocioException("A conta não está inativa.");
        }
        usuario.setDataExclusao(null);
        return UsuarioResponse.de(usuario);
    }

    private void desativar(Usuario usuario) {
        if (usuario.estaInativo()) {
            throw new RegraNegocioException("A conta já está desativada.");
        }
        // Pode haver vários administradores e eles podem ser desativados, mas o
        // sistema nunca pode ficar sem nenhum administrador ativo.
        if (usuario.getPerfil() == PerfilUsuario.ADMINISTRADOR
                && usuarioRepository.countByPerfilAndDataExclusaoIsNull(PerfilUsuario.ADMINISTRADOR) <= 1) {
            throw new RegraNegocioException("Não é possível desativar o último administrador ativo do sistema.");
        }

        UUID id = usuario.getId();
        boolean temPedidoEmAberto = pedidoRepository.existePedidoAbertoCliente(id, STATUS_FINAIS)
                || pedidoRepository.existePedidoAbertoVendedor(id, STATUS_FINAIS);
        if (temPedidoEmAberto) {
            throw new RegraNegocioException("Não é possível desativar a conta: existem pedidos em andamento.");
        }

        usuario.setDataExclusao(LocalDateTime.now());
    }

    private Usuario obter(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

}
