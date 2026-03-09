package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO para adicionar bônus de BonusConfig a uma classe de personagem.
 *
 * @param bonusId      ID do BonusConfig
 * @param valorPorNivel Valor acrescido por nível
 */
public record ClasseBonusRequest(
    @NotNull(message = "ID do bônus é obrigatório")
    Long bonusId,

    @NotNull(message = "Valor por nível é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor por nível deve ser maior que zero")
    BigDecimal valorPorNivel
) {}
