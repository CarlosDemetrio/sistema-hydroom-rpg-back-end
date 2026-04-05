package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * Response da concessão de XP, incluindo flag de level up.
 */
public record ConcederXpResponse(
        Long fichaId,
        Long xp,
        Integer nivel,
        boolean levelUp
) {
}
