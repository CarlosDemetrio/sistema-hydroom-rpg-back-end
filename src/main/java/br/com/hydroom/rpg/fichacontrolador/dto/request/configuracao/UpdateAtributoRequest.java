package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar um atributo existente.
 * Campos null não serão atualizados.
 *
 * @param nome Nome do atributo
 * @param abreviacao Abreviação do atributo
 * @param descricao Descrição do atributo
 * @param formulaImpeto Fórmula para cálculo de ímpeto
 * @param descricaoImpeto Descrição do ímpeto
 * @param valorMinimo Valor mínimo permitido
 * @param valorMaximo Valor máximo permitido
 * @param ordemExibicao Ordem de exibição na interface
 */
public record UpdateAtributoRequest(
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(min = 2, max = 5, message = "Abreviação deve ter entre 2 e 5 caracteres")
    String abreviacao,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    @Size(max = 100, message = "Fórmula de ímpeto deve ter no máximo 100 caracteres")
    String formulaImpeto,

    @Size(max = 200, message = "Descrição do ímpeto deve ter no máximo 200 caracteres")
    String descricaoImpeto,

    Integer valorMinimo,

    Integer valorMaximo,

    Integer ordemExibicao
) {}
