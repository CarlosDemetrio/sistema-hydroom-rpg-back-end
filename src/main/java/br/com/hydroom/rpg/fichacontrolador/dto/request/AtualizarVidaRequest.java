package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AtualizarVidaRequest(
        @NotNull(message = "Vida atual é obrigatória")
        @Min(value = 0, message = "Vida atual não pode ser negativa")
        Integer vidaAtual,

        @NotNull(message = "Essência atual é obrigatória")
        @Min(value = 0, message = "Essência atual não pode ser negativa")
        Integer essenciaAtual,

        @Valid
        List<MembroVidaRequest> membros
) {
    public record MembroVidaRequest(
            @NotNull(message = "ID do membro é obrigatório")
            Long membroCorpoConfigId,

            @NotNull(message = "Dano recebido é obrigatório")
            @Min(value = 0, message = "Dano recebido não pode ser negativo")
            Integer danoRecebido
    ) {}
}
