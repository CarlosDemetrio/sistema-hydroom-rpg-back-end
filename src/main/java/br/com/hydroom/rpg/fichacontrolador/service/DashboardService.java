package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.response.DashboardMestreResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service para geração do dashboard do Mestre com estatísticas do jogo.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final FichaRepository fichaRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardMestreResponse getDashboardMestre(Long jogoId) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);
        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode acessar o dashboard.");
        }

        // Total de fichas de jogadores
        long totalFichas = fichaRepository.countByJogoIdAndIsNpcFalse(jogoId);

        // Total de participantes aprovados
        long totalParticipantes = jogoParticipanteRepository.countByJogoIdAndStatus(jogoId, StatusParticipante.APROVADO);

        // Fichas por nível
        List<Object[]> porNivelRaw = fichaRepository.countByJogoIdGroupByNivel(jogoId);
        Map<Integer, Long> fichasPorNivel = new LinkedHashMap<>();
        for (Object[] row : porNivelRaw) {
            Integer nivel = (Integer) row[0];
            Long count = (Long) row[1];
            fichasPorNivel.put(nivel, count);
        }

        // Top 5 fichas recentemente alteradas
        List<Ficha> recentes = fichaRepository.findTop5ByJogoIdRecentlyUpdated(jogoId);
        List<DashboardMestreResponse.FichaAlteracaoResumo> ultimasAlteracoes = recentes.stream()
                .map(f -> new DashboardMestreResponse.FichaAlteracaoResumo(
                        f.getId(),
                        f.getNome(),
                        f.getUpdatedAt()
                ))
                .toList();

        return new DashboardMestreResponse(
                (int) totalFichas,
                (int) totalParticipantes,
                fichasPorNivel,
                ultimasAlteracoes
        );
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }
}
