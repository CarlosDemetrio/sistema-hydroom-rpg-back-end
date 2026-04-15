package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

/**
 * Response DTO para um valor de atributo dentro de um NpcDificuldadeConfig.
 *
 * @param id          ID do registro NpcDificuldadeAtributo
 * @param atributoId  ID do AtributoConfig associado
 * @param atributoNome Nome do atributo
 * @param valorBase   Valor base pré-definido
 */
public record NpcDificuldadeAtributoResponse(
    Long id,
    Long atributoId,
    String atributoNome,
    Integer valorBase
) {}
