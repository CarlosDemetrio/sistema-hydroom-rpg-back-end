package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service centralizado para validação de acesso a recursos de jogos.
 *
 * <p>Regras de acesso:
 * <ul>
 *   <li>Mestre sempre tem acesso ao próprio jogo (qualquer operação)</li>
 *   <li>Jogador precisa de participação com status APROVADO para acessar</li>
 *   <li>PENDENTE, REJEITADO e BANIDO não têm acesso</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipanteSecurityService {

    private final JogoParticipanteRepository participanteRepository;

    /**
     * Verifica se o usuário pode acessar os recursos do jogo.
     * Retorna true se é Mestre ou participante APROVADO.
     */
    public boolean canAccessJogo(Long jogoId, Long usuarioId) {
        return isMestreDoJogo(jogoId, usuarioId) || isParticipanteAprovado(jogoId, usuarioId);
    }

    /**
     * Verifica se o usuário é o Mestre do jogo.
     */
    public boolean isMestreDoJogo(Long jogoId, Long usuarioId) {
        return participanteRepository.existsByJogoIdAndUsuarioIdAndRole(jogoId, usuarioId, RoleJogo.MESTRE);
    }

    /**
     * Verifica se o usuário tem participação com status APROVADO.
     */
    public boolean isParticipanteAprovado(Long jogoId, Long usuarioId) {
        return participanteRepository.existsByJogoIdAndUsuarioIdAndStatus(jogoId, usuarioId, StatusParticipante.APROVADO);
    }

    /**
     * Lança ForbiddenException se o usuário não tem acesso ao jogo.
     */
    public void assertCanAccessJogo(Long jogoId, Long usuarioId) {
        if (!canAccessJogo(jogoId, usuarioId)) {
            throw new ForbiddenException(
                "Acesso negado: você não é participante aprovado deste jogo.");
        }
    }

    /**
     * Lança ForbiddenException se o usuário não é o Mestre do jogo.
     */
    public void assertMestreDoJogo(Long jogoId, Long usuarioId) {
        if (!isMestreDoJogo(jogoId, usuarioId)) {
            throw new ForbiddenException(
                "Acesso negado: apenas o Mestre pode realizar esta operação.");
        }
    }
}
