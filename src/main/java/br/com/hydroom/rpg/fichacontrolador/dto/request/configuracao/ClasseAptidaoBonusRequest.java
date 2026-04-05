package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para adicionar bônus de AptidaoConfig a uma classe de personagem.
 *
 * @param aptidaoId ID do AptidaoConfig
 * @param bonus     Bônus fixo na aptidão
 */
public record ClasseAptidaoBonusRequest(
    @NotNull(message = "ID da aptidão é obrigatório")
    Long aptidaoId,

    @NotNull(message = "Bônus é obrigatório")
    @Min(value = 0, message = "Bônus não pode ser negativo")
    Integer bonus
) {}
