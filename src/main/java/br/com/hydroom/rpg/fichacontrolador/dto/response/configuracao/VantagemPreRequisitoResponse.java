package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoPreRequisito;

/**
 * Response DTO para um pré-requisito polimórfico de vantagem.
 *
 * <p>Campos populados variam conforme o {@code tipo}:
 * <ul>
 *   <li>VANTAGEM: {@code requisitoId}, {@code requisitoNome}, {@code nivelMinimo}</li>
 *   <li>RACA: {@code racaId}, {@code racaNome}</li>
 *   <li>CLASSE: {@code classeId}, {@code classeNome}</li>
 *   <li>ATRIBUTO: {@code atributoId}, {@code atributoNome}, {@code atributoAbreviacao}, {@code valorMinimo}</li>
 *   <li>NIVEL: {@code valorMinimo}</li>
 *   <li>APTIDAO: {@code aptidaoId}, {@code aptidaoNome}, {@code valorMinimo}</li>
 * </ul>
 */
public record VantagemPreRequisitoResponse(
    Long id,
    TipoPreRequisito tipo,

    // VANTAGEM
    Long requisitoId,
    String requisitoNome,
    Integer nivelMinimo,

    // RACA
    Long racaId,
    String racaNome,

    // CLASSE
    Long classeId,
    String classeNome,

    // ATRIBUTO
    Long atributoId,
    String atributoNome,
    String atributoAbreviacao,

    // APTIDAO
    Long aptidaoId,
    String aptidaoNome,

    // ATRIBUTO / APTIDAO / NIVEL
    Integer valorMinimo
) {}
