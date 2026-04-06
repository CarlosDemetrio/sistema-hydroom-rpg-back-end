package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotNull;

public record UsarProspeccaoRequest(
        @NotNull Long dadoProspeccaoConfigId
) {}
