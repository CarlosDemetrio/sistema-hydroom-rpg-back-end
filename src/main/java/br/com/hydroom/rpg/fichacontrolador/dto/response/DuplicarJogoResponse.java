package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * Resposta da duplicação de um jogo.
 */
public record DuplicarJogoResponse(
        Long jogoId,
        String nome
) {}
