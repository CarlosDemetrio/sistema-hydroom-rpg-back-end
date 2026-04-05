package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RacaVantagemPreDefinidaRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull Long vantagemConfigId
) {}
