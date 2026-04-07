package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;

/**
 * Response para um efeito de item.
 */
public record ItemEfeitoResponse(
    Long id,
    TipoItemEfeito tipoEfeito,
    Long atributoAlvoId,
    Long aptidaoAlvoId,
    Long bonusAlvoId,
    Integer valorFixo,
    String formula,
    String descricaoEfeito
) {}
