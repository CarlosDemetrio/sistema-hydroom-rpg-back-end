package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoRequisito;

/**
 * Response para um requisito de item.
 */
public record ItemRequisitoResponse(
    Long id,
    TipoRequisito tipo,
    String alvo,
    Integer valorMinimo
) {}
