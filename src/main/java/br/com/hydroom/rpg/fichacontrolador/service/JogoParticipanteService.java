package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciamento do fluxo de participação em jogos.
 * Cobre: solicitação de entrada, aprovação, rejeição e banimento.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class JogoParticipanteService {

    private final JogoParticipanteRepository participanteRepository;
    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Jogador solicita entrada em um jogo. Status inicial: PENDENTE.
     *
     * <p>Implementa a strategy "Reactivate": reutiliza o registro existente (incluindo soft-deleted)
     * em vez de criar um novo, preservando a unique constraint (jogo_id, usuario_id).</p>
     */
    @Transactional
    public JogoParticipante solicitar(Long jogoId) {
        Usuario usuario = getUsuarioAtual();
        Jogo jogo = jogoRepository.findById(jogoId)
            .orElseThrow(() -> new ResourceNotFoundException("Jogo", jogoId));

        if (participanteRepository.existsByJogoIdAndUsuarioIdAndRole(jogoId, usuario.getId(), RoleJogo.MESTRE)) {
            throw new BusinessException("O Mestre não pode solicitar entrada no seu próprio jogo.");
        }

        var existente = participanteRepository
            .findByJogoIdAndUsuarioIdIncluindoRemovidos(jogoId, usuario.getId());

        if (existente.isPresent()) {
            JogoParticipante p = existente.get();

            if (StatusParticipante.BANIDO.equals(p.getStatus())) {
                throw new ConflictException("Você foi banido deste jogo pelo Mestre.");
            }
            if (StatusParticipante.APROVADO.equals(p.getStatus()) && p.isActive()) {
                throw new ConflictException("Você já é participante aprovado deste jogo.");
            }
            if (StatusParticipante.PENDENTE.equals(p.getStatus()) && p.isActive()) {
                throw new ConflictException("Você já possui uma solicitação pendente neste jogo.");
            }

            // REJEITADO ativo ou REMOVIDO (soft-deleted, não-BANIDO) — reativar registro
            p.setStatus(StatusParticipante.PENDENTE);
            p.restore();
            log.info("Reativando participação {} para usuário {} no jogo {}", p.getId(), usuario.getId(), jogoId);
            return participanteRepository.save(p);
        }

        JogoParticipante participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(usuario)
            .role(RoleJogo.JOGADOR)
            .status(StatusParticipante.PENDENTE)
            .build();

        log.info("Usuário {} solicitou entrada no jogo {}", usuario.getId(), jogoId);
        return participanteRepository.save(participante);
    }

    /**
     * Mestre aprova uma solicitação pendente.
     */
    @Transactional
    public JogoParticipante aprovar(Long jogoId, Long participanteId) {
        Usuario mestre = getUsuarioAtual();
        assertMestre(jogoId, mestre.getId());

        JogoParticipante participante = buscarParticipante(jogoId, participanteId);
        assertStatus(participante, StatusParticipante.PENDENTE, "aprovar");

        participante.setStatus(StatusParticipante.APROVADO);
        log.info("Mestre {} aprovou participante {} no jogo {}", mestre.getId(), participanteId, jogoId);
        return participanteRepository.save(participante);
    }

    /**
     * Mestre rejeita uma solicitação pendente.
     */
    @Transactional
    public JogoParticipante rejeitar(Long jogoId, Long participanteId) {
        Usuario mestre = getUsuarioAtual();
        assertMestre(jogoId, mestre.getId());

        JogoParticipante participante = buscarParticipante(jogoId, participanteId);
        assertStatus(participante, StatusParticipante.PENDENTE, "rejeitar");

        participante.setStatus(StatusParticipante.REJEITADO);
        log.info("Mestre {} rejeitou participante {} no jogo {}", mestre.getId(), participanteId, jogoId);
        return participanteRepository.save(participante);
    }

    /**
     * Mestre bane um participante APROVADO.
     */
    @Transactional
    public JogoParticipante banir(Long jogoId, Long participanteId) {
        Usuario mestre = getUsuarioAtual();
        assertMestre(jogoId, mestre.getId());

        JogoParticipante participante = buscarParticipante(jogoId, participanteId);
        if (participante.isMestre()) {
            throw new BusinessException("O Mestre não pode se banir do próprio jogo.");
        }
        assertStatus(participante, StatusParticipante.APROVADO, "banir");

        participante.setStatus(StatusParticipante.BANIDO);
        log.info("Mestre {} baniu participante {} no jogo {}", mestre.getId(), participanteId, jogoId);
        return participanteRepository.save(participante);
    }

    /**
     * Mestre desbane participante BANIDO, retornando-o ao status APROVADO.
     */
    @Transactional
    public JogoParticipante desbanir(Long jogoId, Long participanteId) {
        Usuario mestre = getUsuarioAtual();
        assertMestre(jogoId, mestre.getId());

        JogoParticipante participante = buscarParticipante(jogoId, participanteId);
        assertStatus(participante, StatusParticipante.BANIDO, "desbanir");

        participante.setStatus(StatusParticipante.APROVADO);
        log.info("Mestre {} desbaniu participante {} no jogo {}", mestre.getId(), participanteId, jogoId);
        return participanteRepository.save(participante);
    }

    /**
     * Mestre remove provisoriamente um participante APROVADO (soft delete).
     * O jogador pode re-solicitar via strategy Reactivate.
     */
    @Transactional
    public void remover(Long jogoId, Long participanteId) {
        Usuario mestre = getUsuarioAtual();
        assertMestre(jogoId, mestre.getId());

        JogoParticipante participante = buscarParticipante(jogoId, participanteId);
        assertStatus(participante, StatusParticipante.APROVADO, "remover");

        participante.delete();
        log.info("Mestre {} removeu participante {} do jogo {}", mestre.getId(), participanteId, jogoId);
        participanteRepository.save(participante);
    }

    /**
     * Retorna o status de participação do usuário autenticado no jogo.
     * Retorna Optional.empty() se nunca solicitou ou se o registro foi removido.
     */
    public Optional<JogoParticipante> meuStatus(Long jogoId) {
        Usuario usuario = getUsuarioAtual();
        return participanteRepository.findByJogoIdAndUsuarioId(jogoId, usuario.getId());
    }

    /**
     * Usuário cancela a própria solicitação PENDENTE (soft delete do registro).
     */
    @Transactional
    public void cancelarSolicitacao(Long jogoId) {
        Usuario usuario = getUsuarioAtual();

        JogoParticipante participante = participanteRepository
            .findByJogoIdAndUsuarioId(jogoId, usuario.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Participacao", jogoId));

        assertStatus(participante, StatusParticipante.PENDENTE, "cancelar solicitacao");

        participante.delete();
        log.info("Usuário {} cancelou solicitação no jogo {}", usuario.getId(), jogoId);
        participanteRepository.save(participante);
    }

    /**
     * Lista participantes: Mestre vê todos (com filtro opcional); Jogador vê apenas APROVADOS.
     */
    public List<JogoParticipante> listar(Long jogoId, StatusParticipante filtroStatus) {
        Usuario usuario = getUsuarioAtual();
        boolean ehMestre = participanteRepository.existsByJogoIdAndUsuarioIdAndRole(
            jogoId, usuario.getId(), RoleJogo.MESTRE);

        if (ehMestre) {
            return participanteRepository.findByJogoIdAndStatusOpcional(jogoId, filtroStatus);
        }
        return participanteRepository.findByJogoIdAndStatus(jogoId, StatusParticipante.APROVADO);
    }

    private JogoParticipante buscarParticipante(Long jogoId, Long participanteId) {
        JogoParticipante participante = participanteRepository.findById(participanteId)
            .orElseThrow(() -> new ResourceNotFoundException("JogoParticipante", participanteId));
        if (!participante.getJogo().getId().equals(jogoId)) {
            throw new ResourceNotFoundException("JogoParticipante", participanteId);
        }
        return participante;
    }

    private void assertMestre(Long jogoId, Long usuarioId) {
        if (!participanteRepository.existsByJogoIdAndUsuarioIdAndRole(jogoId, usuarioId, RoleJogo.MESTRE)) {
            throw new ForbiddenException("Apenas o Mestre do jogo pode realizar esta ação.");
        }
    }

    private void assertStatus(JogoParticipante participante, StatusParticipante esperado, String acao) {
        if (!esperado.equals(participante.getStatus())) {
            throw new BusinessException(
                "Não é possível " + acao + " participante com status " + participante.getStatus() + ".");
        }
    }

    private Usuario getUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }
}
