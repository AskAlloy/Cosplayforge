package com.horizon.Cosplayforge.security;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.horizon.Cosplayforge.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletResponse;

/**
* Regras de acesso
*/
@Configuration
@EnableWebSecurity

public class SecurityConfig {

   // senhas com criptografia
   @Bean
   public PasswordEncoder passwordEncoder() {
       return new BCryptPasswordEncoder();
   }

   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http,
                                          JwtService jwtService,
                                          UsuarioRepository usuarioRepository) throws Exception {
       http
               // CSRF protege sessões com cookie. Aqui não há sessão nem cookie:
               // o token vai no cabeçalho, então o ataque não se aplica.

               .csrf(csrf -> csrf.disable())
               .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authorizeHttpRequests(rotas -> rotas
                       // catálogo público qualquer visitante consulta
                       .requestMatchers("/error").permitAll()
                       .requestMatchers("/api/auth/**").permitAll()
                       .requestMatchers("/","/index.html","/favicon.ico").permitAll()
                       .requestMatchers("/", "/login", "/cadastro", "/css/**", "/js/**", "/img/**",
                               "/admin/painel", "/vendedor/painel", "/vitrine.html").permitAll()
                       .requestMatchers("/home").permitAll()
                       .requestMatchers(HttpMethod.GET, "/api/produtos/**", "/api/categorias/**").permitAll()

                       //Adms e Vendedor rotas exclusivas
                       .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                       .requestMatchers("/api/vendedor/**").hasRole("VENDEDOR")
                       .anyRequest().authenticated())
               .exceptionHandling(erros -> erros
                       .authenticationEntryPoint((req, res, ex) ->
                               escreverErro(res, 401, "Não autenticado. Informe um token válido."))
                       .accessDeniedHandler((req, res, ex) ->
                               escreverErro(res, 403, "Você não tem permissão para acessar este recurso.")))
               .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarioRepository),
                       UsernamePasswordAuthenticationFilter.class);

       return http.build();
   }

   // Erros de segurança acontecem antes do Spring MVC, então o
   // GlobalExceptionHandler não os alcança: o JSON é escrito aqui.

   private void escreverErro(HttpServletResponse resposta, int status, String mensagem) throws IOException {
       resposta.setStatus(status);
       resposta.setContentType("application/json;charset=UTF-8");
       resposta.getWriter().write("{\"status\":" + status + ",\"mensagem\":\"" + mensagem + "\"}");
   }
}