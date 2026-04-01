package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsável por operações no perfil do usuário autenticado.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Retorna o usuário autenticado atual.
     */
    public Usuario buscarAtual() {
        return getUsuarioAtual();
    }

    /**
     * Atualiza o nome do usuário autenticado.
     *
     * <p>Apenas o nome é editável — email e foto são gerenciados pelo provedor OAuth2 (Google).</p>
     */
    @Transactional
    public Usuario atualizarNome(String novoNome) {
        Usuario usuario = getUsuarioAtual();
        usuario.setNome(novoNome);
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Nome do usuário {} atualizado para '{}'", usuario.getId(), novoNome);
        return salvo;
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Usuário não autenticado."));
    }
}
