package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * DTO de resposta para os valores de um atributo de uma ficha.
 */
public record FichaAtributoResponse(
    Long id,
    Long atributoConfigId,
    String atributoNome,
    String atributoAbreviacao,
    Integer base,
    Integer nivel,
    Integer outros,
    Integer total,
    Double impeto
) {}
