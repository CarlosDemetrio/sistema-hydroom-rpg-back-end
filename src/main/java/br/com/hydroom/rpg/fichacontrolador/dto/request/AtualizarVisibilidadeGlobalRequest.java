package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotNull;

public record AtualizarVisibilidadeGlobalRequest(
    @NotNull(message = "visivelGlobalmente é obrigatório")
    Boolean visivelGlobalmente
) {}
