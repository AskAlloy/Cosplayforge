package com.horizon.Cosplayforge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import com.horizon.Cosplayforge.dto.CadastroRequest;
import com.horizon.Cosplayforge.dto.LoginRequest;
import com.horizon.Cosplayforge.dto.LoginResponse;
import com.horizon.Cosplayforge.dto.MensagemResponse;
import com.horizon.Cosplayforge.dto.UsuarioResponse;
import com.horizon.Cosplayforge.service.AuthService;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // recebe a solicitação
@RequestMapping("/api/auth") // caminho
@RequiredArgsConstructor
public class AuthController {
 
    private final AuthService authService;
 
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(requisicao));
    }

    @GetMapping("/confirmar-email")
    public MensagemResponse confirmarEmail(@RequestParam String token) {
        authService.confirmarEmail(token);
        return new MensagemResponse("E-mail confirmado com sucesso. Você já pode fazer login.");
    }
 
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest requisicao) {

        return authService.login(requisicao);
    }
}
