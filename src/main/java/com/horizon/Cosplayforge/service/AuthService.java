package com.horizon.Cosplayforge.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import com.horizon.Cosplayforge.dto.CadastroRequest;
import com.horizon.Cosplayforge.dto.LoginRequest;
import com.horizon.Cosplayforge.dto.LoginResponse;
import com.horizon.Cosplayforge.dto.UsuarioResponse;
import com.horizon.Cosplayforge.exception.AcessoNegadoException;
import com.horizon.Cosplayforge.exception.CredenciaisInvalidasException;
import com.horizon.Cosplayforge.exception.RegraNegocioException;
import com.horizon.Cosplayforge.model.PerfilUsuario;
import com.horizon.Cosplayforge.model.Usuario;
import com.horizon.Cosplayforge.repository.UsuarioRepository;
import com.horizon.Cosplayforge.security.JwtService;
 
import lombok.RequiredArgsConstructor;
 
// Realizar Cadastro | Efetuar Login.
@Service
@RequiredArgsConstructor
public class AuthService {
 
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
 
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
 
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest req) {
        // O administrador nunca se cadastra sozinho: é criado pela carga inicial.
        if (req.perfil() == PerfilUsuario.ADMINISTRADOR) {
            throw new RegraNegocioException("Perfil inválido para cadastro. Escolha CLIENTE ou VENDEDOR.");
        }
        if (!req.senha().equals(req.confirmarSenha())) {
            throw new RegraNegocioException("A senha e a confirmação de senha não coincidem.");
        }
 
        String email = req.email().trim().toLowerCase();
 
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Este e-mail já está cadastrado.");
        }
        if (usuarioRepository.existsByNomeUsuario(req.nomeUsuario())) {
            throw new RegraNegocioException("Este nome de usuário já está em uso.");
        }
        if (usuarioRepository.existsByCpf(req.cpf())) {
            throw new RegraNegocioException("Este CPF já está cadastrado.");
        }
 
        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setNomeUsuario(req.nomeUsuario());
        usuario.setEmail(email);
        usuario.setCpf(req.cpf());
        usuario.setTelefone(req.telefone());
        usuario.setSenhaHash(passwordEncoder.encode(req.senha()));
        usuario.setPerfil(req.perfil());
        usuario.setEmailConfirmado(false);
        usuario.setTokenConfirmacaoEmail(UUID.randomUUID().toString());
 
        usuarioRepository.save(usuario);
 
        // SIMULAÇÃO: em vez de enviar e-mail, o link aparece no console.
        // Para o envio real, é só trocar este log por um serviço de e-mail.
        log.info("[SIMULAÇÃO DE E-MAIL] Confirme o cadastro de {}: {}/api/auth/confirmar-email?token={}",
                email, baseUrl, usuario.getTokenConfirmacaoEmail());
 
        return UsuarioResponse.de(usuario);
    }
 
    @Transactional
    public void confirmarEmail(String token) {
        Usuario usuario = usuarioRepository.findByTokenConfirmacaoEmail(token)
                .orElseThrow(() -> new RegraNegocioException("Link de confirmação inválido ou já utilizado."));
 
        usuario.setEmailConfirmado(true);
        usuario.setTokenConfirmacaoEmail(null); // o link só vale uma vez
    }
 
    //
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        // Usuário inexistente e senha errada geram a MESMA resposta!!!!
        Usuario usuario = usuarioRepository.buscarParaLogin(req.login().trim())
                .orElseThrow(CredenciaisInvalidasException::new);
 
        if (!passwordEncoder.matches(req.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
 
        // Só depois de provar a senha é seguro explicar o motivo do bloqueio.
        if (usuario.estaInativo()) {
            throw new AcessoNegadoException("Conta inativa. Entre em contato com o suporte.");
        }
        if (!Boolean.TRUE.equals(usuario.getEmailConfirmado())) {
            throw new AcessoNegadoException("Confirme seu e-mail antes de entrar.");
        }
 
        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(token, "Bearer", jwtService.getExpiracaoMinutos(), UsuarioResponse.de(usuario));
    }
}