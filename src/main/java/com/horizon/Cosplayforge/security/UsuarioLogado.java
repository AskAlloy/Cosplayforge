package com.horizon.Cosplayforge.security;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
 
import com.horizon.Cosplayforge.model.PerfilUsuario;

public record UsuarioLogado(UUID id, String nomeUsuario, PerfilUsuario perfil) {
 

    // O vendedor também é cliente

    public List<GrantedAuthority> autoridades() {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
        if (perfil == PerfilUsuario.VENDEDOR) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + PerfilUsuario.CLIENTE.name()));
        }
        return autoridades;
    }
}