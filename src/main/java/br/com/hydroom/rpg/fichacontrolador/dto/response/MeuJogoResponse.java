package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * Resposta resumida de um jogo do usuário atual, com informação de role e quantidade de personagens.
 */
public record MeuJogoResponse(
        Long id,
        String nome,
        boolean isMestre,
        int meusPersonagens
) {}
