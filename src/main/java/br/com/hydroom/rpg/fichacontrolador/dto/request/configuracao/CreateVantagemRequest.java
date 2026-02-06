package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova vantagem.
 *
 * @param jogoId ID do jogo
 * @param nome Nome da vantagem
 * @param descricao Descrição da vantagem
 * @param nivelMaximo Nível máximo da vantagem
 * @param formulaCusto Fórmula para cálculo do custo (ex: "NIVEL * 2")
 * @param descricaoEfeito Descrição do efeito da vantagem
 * @param ordemExibicao Ordem de exibição
 */
public record CreateVantagemRequest(
    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    String descricao,

    @NotNull(message = "Nível máximo é obrigatório")
    @Min(value = 1, message = "Nível máximo deve ser no mínimo 1")
    Integer nivelMaximo,

    @NotBlank(message = "Fórmula de custo é obrigatória")
    @Size(max = 100, message = "Fórmula de custo deve ter no máximo 100 caracteres")
    String formulaCusto,

    @Size(max = 500, message = "Descrição do efeito deve ter no máximo 500 caracteres")
    String descricaoEfeito,

    Integer ordemExibicao
) {}
