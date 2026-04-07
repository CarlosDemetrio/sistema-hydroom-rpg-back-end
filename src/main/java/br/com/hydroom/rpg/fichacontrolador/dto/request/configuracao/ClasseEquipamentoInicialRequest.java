package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request para criação de equipamento inicial de classe.
 */
public record ClasseEquipamentoInicialRequest(
        @NotNull Long itemConfigId,
        @NotNull Boolean obrigatorio,
        Integer grupoEscolha,
        @Min(1) @Max(99) int quantidade
) {}
