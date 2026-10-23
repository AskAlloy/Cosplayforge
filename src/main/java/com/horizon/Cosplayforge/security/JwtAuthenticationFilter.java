package com.horizon.Cosplayforge.security;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
 
import com.horizon.Cosplayforge.repository.UsuarioRepository;
 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
/*
* Toda Requisição vai ter, ele valida usando o Authorization
* se o token tiver vencido devolve erro 401
* */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    
    private static final String PREFIXO = "Bearer ";
 
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
 
    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }
 
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
 
        String cabecalho = request.getHeader("Authorization");
 
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            String token = cabecalho.substring(PREFIXO.length());
 
            jwtService.validarEExtrairId(token)
                    .flatMap(usuarioRepository::findById)
                    // conta inativada ou sem e-mail confirmado não autentica, mesmo com token válido
                    .filter(u -> !u.estaInativo() && Boolean.TRUE.equals(u.getEmailConfirmado()))
                    .ifPresent(u -> {
                        UsuarioLogado principal = new UsuarioLogado(u.getId(), u.getNomeUsuario(), u.getPerfil());
                        UsernamePasswordAuthenticationToken autenticacao =
                                new UsernamePasswordAuthenticationToken(principal, null, principal.autoridades());
                        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(autenticacao);
                    });
        }
 
        filterChain.doFilter(request, response);
    }
}
