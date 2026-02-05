package br.com.hydroom.rpg.fichacontrolador.filter;

import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro que recarrega as authorities do usuário do banco de dados a cada requisição.
 * Isso garante que mudanças na role do usuário sejam refletidas imediatamente,
 * sem necessidade de fazer logout/login.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshAuthoritiesFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Apenas processar se for OAuth2 e estiver autenticado
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token && authentication.isAuthenticated()) {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");

            if (email != null) {
                // Buscar usuário no banco
                usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                    // Verificar se as authorities precisam ser atualizadas
                    String expectedAuthority = "ROLE_" + usuario.getRole();
                    boolean hasCorrectAuthority = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals(expectedAuthority));

                    if (!hasCorrectAuthority) {
                        // Recriar authorities com a role atualizada do banco
                        Set<GrantedAuthority> updatedAuthorities = new HashSet<>(oauth2User.getAuthorities());

                        // Remover authorities antigas de ROLE_*
                        updatedAuthorities.removeIf(auth -> auth.getAuthority().startsWith("ROLE_"));

                        // Adicionar authority correta do banco
                        updatedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()));

                        // Criar novo OAuth2User com authorities atualizadas
                        OAuth2User updatedUser = new DefaultOAuth2User(
                                updatedAuthorities,
                                oauth2User.getAttributes(),
                                "email"
                        );

                        // Criar novo token com authorities atualizadas
                        OAuth2AuthenticationToken updatedToken = new OAuth2AuthenticationToken(
                                updatedUser,
                                updatedAuthorities,
                                oauth2Token.getAuthorizedClientRegistrationId()
                        );

                        // Atualizar o SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(updatedToken);

                        log.debug("Authorities atualizadas para usuário {}: {}", email, updatedAuthorities);
                    }
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}
