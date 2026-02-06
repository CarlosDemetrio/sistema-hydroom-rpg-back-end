package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Atributo.
 * Contém todas as informações de um atributo para exibição.
 *
 * @param id ID do atributo
 * @param jogoId ID do jogo
 * @param nome Nome do atributo
 * @param abreviacao Abreviação do atributo
 * @param descricao Descrição do atributo
 * @param formulaImpeto Fórmula para cálculo de ímpeto
 * @param descricaoImpeto Descrição do ímpeto
 * @param valorMinimo Valor mínimo permitido
 * @param valorMaximo Valor máximo permitido
 * @param ordemExibicao Ordem de exibição na interface
 * @param ativo Status do atributo
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record AtributoResponse(
    Long id,
    Long jogoId,
    String nome,
    String abreviacao,
    String descricao,
    String formulaImpeto,
    String descricaoImpeto,
    Integer valorMinimo,
    Integer valorMaximo,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
