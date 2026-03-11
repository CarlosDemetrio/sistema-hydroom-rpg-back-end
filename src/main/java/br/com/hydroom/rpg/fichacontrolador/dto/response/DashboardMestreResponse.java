package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resposta do dashboard do Mestre com estatísticas do jogo.
 */
public record DashboardMestreResponse(
        int totalFichas,
        int totalParticipantes,
        Map<Integer, Long> fichasPorNivel,
        List<FichaAlteracaoResumo> ultimasAlteracoes
) {
    public record FichaAlteracaoResumo(
            Long fichaId,
            String nome,
            LocalDateTime dataUltimaAlteracao
    ) {}
}
