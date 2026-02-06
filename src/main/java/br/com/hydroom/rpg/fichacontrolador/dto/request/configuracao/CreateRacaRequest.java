package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova raça.
 *
 * @param jogoId ID do jogo
 * @param nome Nome da raça (ex: Humano, Elfo, Anão)
 * @param descricao Descrição da raça
 * @param ordemExibicao Ordem de exibição
 */
public record CreateRacaRequest(
    @NotNull(message = ValidationMessages.Raca.JOGO_OBRIGATORIO)
    Long jogoId,

    @NotBlank(message = ValidationMessages.Raca.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.RACA_NOME_MIN,
          max = ValidationMessages.Limites.RACA_NOME_MAX,
          message = ValidationMessages.Raca.NOME_TAMANHO)
    String nome,

    @Size(max = ValidationMessages.Limites.RACA_DESCRICAO_MAX,
          message = ValidationMessages.Raca.DESCRICAO_TAMANHO)
    String descricao,

    Integer ordemExibicao
) {}
