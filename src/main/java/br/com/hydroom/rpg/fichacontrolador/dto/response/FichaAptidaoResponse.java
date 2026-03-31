package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * DTO de resposta para os valores de uma aptidão de uma ficha.
 */
public record FichaAptidaoResponse(
    Long id,
    Long aptidaoConfigId,
    String aptidaoNome,
    Integer base,
    Integer sorte,
    Integer classe,
    Integer total
) {}
