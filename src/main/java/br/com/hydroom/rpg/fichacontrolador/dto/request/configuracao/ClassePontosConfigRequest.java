package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ClassePontosConfigRequest(
    @NotNull @Min(1) Integer nivel,
    @NotNull @Min(0) Integer pontosAtributo,
    @NotNull @Min(0) Integer pontosVantagem
) {}
