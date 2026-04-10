package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * Serviço customizado para processar usuários OAuth2.
 * Cria ou atualiza o usuário no banco de dados no primeiro login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public void configureRestTemplate(@Qualifier("oAuth2RestTemplate") RestTemplate restTemplate) {
        setRestOperations(restTemplate);
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String providerId = oauth2User.getAttribute("sub"); // Google uses "sub" for user ID
        String email = oauth2User.getAttribute("email");
        String nome = oauth2User.getAttribute("name");
        String imagemUrl = oauth2User.getAttribute("picture");

        log.info("Processando login OAuth2 - Provider: {}, Email: {}", provider, email);

        // Busca usuário por providerId OU por email (para casos de seed data)
        Usuario usuario = usuarioRepository.findByProviderId(providerId)
                .or(() -> usuarioRepository.findByEmail(email))
                .orElseGet(() -> {
                    log.info("Criando novo usuário - Email: {}, Provider: {}", email, provider);
                    return criarNovoUsuario(provider, providerId, email, nome, imagemUrl);
                });

        // Se usuário existia mas não tinha providerId (seed data), atualizar
        if (usuario.getProviderId() == null || !usuario.getProviderId().equals(providerId)) {
            usuario.setProviderId(providerId);
            usuario.setProvider(provider);
            usuarioRepository.save(usuario);
            log.info("ProviderId atualizado para usuário existente - ID: {}, Email: {}", usuario.getId(), email);
        }

        // Atualiza dados do usuário se mudaram
        if (atualizarDadosUsuario(usuario, email, nome, imagemUrl)) {
            usuarioRepository.save(usuario);
            log.info("Dados do usuário atualizados - ID: {}, Email: {}", usuario.getId(), email);
        }

        // Criar authorities baseadas na role do usuário
        Set<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());

        // Adicionar role do banco de dados como authority
        // Spring Security espera prefixo "ROLE_" para @PreAuthorize("hasRole('...')")
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()));

        log.info("Authorities configuradas para usuário {}: {}", email, authorities);

        // Retornar novo OAuth2User com authorities customizadas
        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "email" // Nome do atributo que será usado como principal
        );
    }

    private Usuario criarNovoUsuario(String provider, String providerId, String email, String nome, String imagemUrl) {
        Usuario usuario = Usuario.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nome(nome)
                .imagemUrl(imagemUrl)
                .build();

        return usuarioRepository.save(usuario);
    }

    private boolean atualizarDadosUsuario(Usuario usuario, String email, String nome, String imagemUrl) {
        boolean atualizado = false;

        if (!usuario.getEmail().equals(email)) {
            usuario.setEmail(email);
            atualizado = true;
        }

        if (!usuario.getNome().equals(nome)) {
            usuario.setNome(nome);
            atualizado = true;
        }

        if (imagemUrl != null && !imagemUrl.equals(usuario.getImagemUrl())) {
            usuario.setImagemUrl(imagemUrl);
            atualizado = true;
        }

        return atualizado;
    }
}
