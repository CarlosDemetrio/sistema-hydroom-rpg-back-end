package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuração de auditoria JPA usando Hibernate Envers.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@RequiredArgsConstructor
public class AuditConfig {

    private final UsuarioRepository usuarioRepository;

    /**
     * Provedor de informações do auditor (usuário logado).
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl(usuarioRepository);
    }
}
