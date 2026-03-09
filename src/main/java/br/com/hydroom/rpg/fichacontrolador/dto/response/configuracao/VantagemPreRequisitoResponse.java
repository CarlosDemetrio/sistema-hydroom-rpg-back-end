package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Response DTO para um pré-requisito de vantagem.
 *
 * @param id            ID do pré-requisito
 * @param requisitoId   ID da vantagem exigida
 * @param requisitoNome Nome da vantagem exigida
 * @param nivelMinimo   Nível mínimo exigido
 */
public record VantagemPreRequisitoResponse(
    Long id,
    Long requisitoId,
    String requisitoNome,
    Integer nivelMinimo
) {}
