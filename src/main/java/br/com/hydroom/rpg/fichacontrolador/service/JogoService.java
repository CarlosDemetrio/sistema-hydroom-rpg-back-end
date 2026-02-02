package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gerenciar Jogos/Campanhas.
 * Apenas o Mestre do jogo pode criar/editar/deletar.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JogoService {

    private final JogoRepository jogoRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Jogo> listarJogosDoUsuario() {
        Usuario usuarioAtual = getUsuarioAtual();
        List<JogoParticipante> participacoes = jogoParticipanteRepository.findByUsuarioId(usuarioAtual.getId());
        return participacoes.stream()
                .map(JogoParticipante::getJogo)
                .collect(Collectors.toList());
    }

    public Jogo buscarJogo(Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioPertenceAoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Você não tem acesso a este jogo");
        }

        return jogo;
    }

    @Transactional
    public Jogo criarJogo(Jogo jogo) {
        Usuario usuarioAtual = getUsuarioAtual();

        jogo.setAtivo(true);
        Jogo jogoSalvo = jogoRepository.save(jogo);

        JogoParticipante participacao = JogoParticipante.builder()
                .jogo(jogoSalvo)
                .usuario(usuarioAtual)
                .role(RoleJogo.MESTRE)
                .ativo(true)
                .build();

        jogoParticipanteRepository.save(participacao);

        return jogoSalvo;
    }

    @Transactional
    public Jogo atualizarJogo(Long id, Jogo jogoAtualizado) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioEhMestreDoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Apenas o Mestre do jogo pode editá-lo");
        }

        jogo.setNome(jogoAtualizado.getNome());
        jogo.setDescricao(jogoAtualizado.getDescricao());
        jogo.setImagemUrl(jogoAtualizado.getImagemUrl());

        return jogoRepository.save(jogo);
    }

    @Transactional
    public void deletarJogo(Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioEhMestreDoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Apenas o Mestre do jogo pode deletá-lo");
        }

        jogo.setAtivo(false);
        jogoRepository.save(jogo);
    }

    @Transactional
    public Jogo ativarJogo(Long id) {
        Jogo jogo = jogoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        if (!usuarioEhMestreDoJogo(usuarioAtual.getId(), id)) {
            throw new AccessDeniedException("Apenas o Mestre do jogo pode reativá-lo");
        }

        jogo.setAtivo(true);
        return jogoRepository.save(jogo);
    }

    public boolean usuarioEhMestreDoJogo(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndRoleAndAtivoTrue(
                usuarioId, jogoId, RoleJogo.MESTRE);
    }

    public boolean usuarioPertenceAoJogo(Long usuarioId, Long jogoId) {
        return jogoParticipanteRepository.existsByUsuarioIdAndJogoIdAndAtivoTrue(usuarioId, jogoId);
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }
}
