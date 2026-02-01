package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação do AuditorAware para capturar o usuário logado nas operações de auditoria.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<Long> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof OAuth2User oauth2User) {
                String providerId = oauth2User.getAttribute("sub");

                if (providerId != null) {
                    return usuarioRepository.findByProviderId(providerId)
                            .map(Usuario::getId);
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.warn("Erro ao obter auditor atual: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
