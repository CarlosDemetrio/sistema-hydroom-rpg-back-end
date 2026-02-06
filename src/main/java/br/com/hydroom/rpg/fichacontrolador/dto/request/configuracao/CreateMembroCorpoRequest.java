package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO para criar um novo membro do corpo.
 *
 * @param jogoId ID do jogo
 * @param nome Nome do membro do corpo (ex: Cabeça, Tronco)
 * @param porcentagemVida Porcentagem da vida total (0.01 a 1.00)
 * @param ordemExibicao Ordem de exibição
 */
public record CreateMembroCorpoRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    String nome,

    @NotNull(message = "Porcentagem de vida é obrigatória")
    @DecimalMin(value = "0.01", message = "Porcentagem de vida deve ser no mínimo 0.01")
    @DecimalMax(value = "1.00", message = "Porcentagem de vida deve ser no máximo 1.00")
    BigDecimal porcentagemVida,

    Integer ordemExibicao
) {}
