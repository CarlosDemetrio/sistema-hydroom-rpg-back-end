package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoEfeito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO para VantagemEfeito.
 */
public record VantagemEfeitoResponse(
    Long id,
    Long vantagemConfigId,
    TipoEfeito tipoEfeito,
    Long atributoAlvoId,
    String atributoAlvoNome,
    Long aptidaoAlvoId,
    String aptidaoAlvoNome,
    Long bonusAlvoId,
    String bonusAlvoNome,
    Long membroAlvoId,
    String membroAlvoNome,
    BigDecimal valorFixo,
    BigDecimal valorPorNivel,
    String formula,
    String descricaoEfeito,
    LocalDateTime dataCriacao
) {}
