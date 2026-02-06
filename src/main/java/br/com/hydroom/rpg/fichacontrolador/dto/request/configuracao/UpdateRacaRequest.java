package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar uma raça existente.
 *
 * @param nome Nome da raça
 * @param descricao Descrição da raça
 * @param ordemExibicao Ordem de exibição
 */
public record UpdateRacaRequest(
    @Size(min = ValidationMessages.Limites.RACA_NOME_MIN,
          max = ValidationMessages.Limites.RACA_NOME_MAX,
          message = ValidationMessages.Raca.NOME_TAMANHO)
    String nome,

    @Size(max = ValidationMessages.Limites.RACA_DESCRICAO_MAX,
          message = ValidationMessages.Raca.DESCRICAO_TAMANHO)
    String descricao,

    Integer ordemExibicao
) {}
