package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para adicionar um pré-requisito a uma vantagem.
 *
 * @param requisitoId ID da vantagem exigida como pré-requisito
 * @param nivelMinimo Nível mínimo exigido (padrão 1 se null)
 */
public record VantagemPreRequisitoRequest(
    @NotNull(message = "ID do requisito é obrigatório")
    Long requisitoId,

    @Min(value = 1, message = "Nível mínimo deve ser no mínimo 1")
    Integer nivelMinimo
) {}
