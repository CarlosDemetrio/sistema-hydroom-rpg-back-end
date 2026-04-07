package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;

import java.math.BigDecimal;

/**
 * Response resumido de ItemConfig, sem sub-entidades.
 * Utilizado em listagens paginadas para performance.
 */
public record ItemConfigResumoResponse(
    Long id,
    Long jogoId,
    String nome,
    Long raridadeId,
    String raridadeNome,
    String raridadeCor,
    Long tipoId,
    String tipoNome,
    CategoriaItem categoria,
    BigDecimal peso,
    Integer valor,
    int nivelMinimo,
    String propriedades,
    int ordemExibicao
) {}
