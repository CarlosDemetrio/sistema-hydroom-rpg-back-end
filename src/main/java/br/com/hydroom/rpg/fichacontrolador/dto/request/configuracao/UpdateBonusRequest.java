package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar um bônus existente.
 *
 * @param nome Nome do bônus
 * @param descricao Descrição do bônus
 * @param formulaBase Fórmula para cálculo do bônus
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateBonusRequest(
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    String descricao,

    @Size(max = 200, message = "Fórmula base deve ter no máximo 200 caracteres")
    String formulaBase,

    Integer ordemExibicao
) {}
