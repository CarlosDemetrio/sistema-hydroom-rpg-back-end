package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Response DTO para um bônus de AptidaoConfig em uma classe de personagem.
 *
 * @param id         ID do registro
 * @param aptidaoId  ID do AptidaoConfig
 * @param aptidaoNome Nome da aptidão
 * @param bonus      Bônus fixo na aptidão
 */
public record ClasseAptidaoBonusResponse(
    Long id,
    Long aptidaoId,
    String aptidaoNome,
    Integer bonus
) {}
