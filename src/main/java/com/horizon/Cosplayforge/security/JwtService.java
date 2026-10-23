package com.horizon.Cosplayforge.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.horizon.Cosplayforge.model.Usuario;
 
/**
 * Gera e valida os tokens JWT (RNF001). O token guarda só o id do usuário;
 * o perfil é relido do banco a cada requisição, então uma mudança de perfil
 * ou a inativação da conta valem imediatamente.
 */
@Service
public class JwtService {
 
    private static final String EMISSOR = "cosplayforge";
 
    private final Algorithm algoritmo;
    private final long expiracaoMinutos;
 
    public JwtService(@Value("${jwt.secret}") String segredo,
                      @Value("${jwt.expiration-minutes:120}") long expiracaoMinutos) {
        this.algoritmo = Algorithm.HMAC256(segredo);
        this.expiracaoMinutos = expiracaoMinutos;
    }
 
    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(usuario.getId().toString())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES))
                .sign(algoritmo);
    }
 
    /** Devolve o id do usuário se o token for válido (assinatura, emissor e validade). */
    public Optional<UUID> validarEExtrairId(String token) {
        try {
            DecodedJWT jwt = JWT.require(algoritmo).withIssuer(EMISSOR).build().verify(token);
            return Optional.of(UUID.fromString(jwt.getSubject()));
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
 
    public long getExpiracaoMinutos() {
        return expiracaoMinutos;
    }
}