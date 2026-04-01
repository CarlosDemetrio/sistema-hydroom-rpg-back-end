package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarProspeccaoRequest(
        @NotNull(message = "ID do dado de prospecção é obrigatório")
        Long dadoProspeccaoConfigId,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 0, message = "Quantidade não pode ser negativa")
        Integer quantidade
) {}
