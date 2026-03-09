package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.math.BigDecimal;

/**
 * Response DTO para um bônus de BonusConfig em uma classe de personagem.
 *
 * @param id           ID do registro
 * @param bonusId      ID do BonusConfig
 * @param bonusNome    Nome do bônus
 * @param valorPorNivel Valor acrescido por nível
 */
public record ClasseBonusResponse(
    Long id,
    Long bonusId,
    String bonusNome,
    BigDecimal valorPorNivel
) {}
