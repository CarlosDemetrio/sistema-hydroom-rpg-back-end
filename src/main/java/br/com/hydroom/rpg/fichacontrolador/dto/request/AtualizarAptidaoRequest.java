package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para atualizar os valores de uma aptidão de uma ficha.
 */
public record AtualizarAptidaoRequest(
    @NotNull(message = "aptidaoConfigId é obrigatório")
    Long aptidaoConfigId,

    @Min(value = 0, message = "base não pode ser negativo")
    Integer base,

    @Min(value = 0, message = "sorte não pode ser negativo")
    Integer sorte,

    @Min(value = 0, message = "classe não pode ser negativo")
    Integer classe
) {}
