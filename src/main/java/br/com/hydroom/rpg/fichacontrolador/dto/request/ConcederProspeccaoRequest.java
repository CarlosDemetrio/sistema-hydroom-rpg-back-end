package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConcederProspeccaoRequest(
        @NotNull Long dadoProspeccaoConfigId,
        @NotNull @Min(1) @Max(99) Integer quantidade
) {}
