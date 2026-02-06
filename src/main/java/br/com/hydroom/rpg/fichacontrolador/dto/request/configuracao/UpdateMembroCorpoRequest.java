package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO para atualizar um membro do corpo existente.
 *
 * @param nome Nome do membro do corpo
 * @param porcentagemVida Porcentagem da vida total
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateMembroCorpoRequest(
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @DecimalMin(value = "0.01", message = "Porcentagem de vida deve ser no mínimo 0.01")
    @DecimalMax(value = "1.00", message = "Porcentagem de vida deve ser no máximo 1.00")
    BigDecimal porcentagemVida,

    Integer ordemExibicao
) {}
