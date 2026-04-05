package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para duplicar um jogo.
 */
public record DuplicarJogoRequest(
        @NotBlank String novoNome
) {}
