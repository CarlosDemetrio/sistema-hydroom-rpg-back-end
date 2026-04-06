package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.util.List;

/**
 * Resposta de visibilidade granular de um NPC.
 *
 * <p>Retorna o estado atual de visibilidade do NPC: se está visível globalmente
 * e quais jogadores têm acesso aos stats completos.</p>
 */
public record FichaVisibilidadeResponse(
    Long fichaId,
    boolean visivelGlobalmente,
    List<JogadorAcessoResponse> jogadoresComAcesso
) {

    public record JogadorAcessoResponse(
        Long jogadorId,
        String jogadorNome,
        String nomePersonagem
    ) {}
}
