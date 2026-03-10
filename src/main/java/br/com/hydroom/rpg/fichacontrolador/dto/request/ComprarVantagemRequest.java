package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request para comprar uma vantagem para uma ficha.
 */
public record ComprarVantagemRequest(
    @NotNull Long vantagemConfigId
) {}
