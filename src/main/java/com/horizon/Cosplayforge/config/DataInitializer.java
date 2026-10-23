package com.horizon.Cosplayforge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
 
import com.horizon.Cosplayforge.model.Categoria;
import com.horizon.Cosplayforge.model.PerfilUsuario;
import com.horizon.Cosplayforge.model.Usuario;
import com.horizon.Cosplayforge.repository.CategoriaRepository;
import com.horizon.Cosplayforge.repository.UsuarioRepository;
 
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
	 
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Value("${admin.email:admin@cosplayforge.local}")
    private String adminEmail;
 
    @Value("${admin.password:}")
    private String adminSenha;
 
    @Override
    public void run(String... args) {
        criarAdministrador();
        criarCategorias();
    }
 
    private void criarAdministrador() {
        if (adminSenha == null || adminSenha.isBlank()) {
            log.warn("ADMIN_PASSWORD não definida: o administrador não foi criado.");
            return;
        }
        if (usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }
 
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setNomeUsuario("admin");
        admin.setEmail(adminEmail.toLowerCase());
        admin.setCpf("12334567800"); // valor de sistema, não é um CPF real
        admin.setSenhaHash(passwordEncoder.encode(adminSenha));
        admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
        admin.setEmailConfirmado(true);
        usuarioRepository.save(admin);
 
        log.info("Administrador criado: {}", adminEmail);
    }
 
    private void criarCategorias() {
        if (categoriaRepository.count() > 0) {
            return;
        }
        categoriaRepository.save(Categoria.builder()
                .nome("Cosplay").descricao("Fantasias, acessórios e adereços para cosplay").build());
        categoriaRepository.save(Categoria.builder()
                .nome("Impressão 3D").descricao("Peças, miniaturas e itens impressos em 3D").build());
        categoriaRepository.save(Categoria.builder()
                .nome("Cultura Gamer").descricao("Colecionáveis e produtos do universo gamer").build());
 
        log.info("Categorias iniciais criadas.");
    }	
}
