package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para atualizar uma classe existente.
 * Campos null não serão atualizados.
 *
 * @param nome Nome da classe
 * @param descricao Descrição da classe
 * @param ordemExibicao Ordem de exibição na interface
 */
public record UpdateClasseRequest(
    @Size(min = ValidationMessages.Limites.CLASSE_NOME_MIN,
          max = ValidationMessages.Limites.CLASSE_NOME_MAX,
          message = ValidationMessages.ClassePersonagem.NOME_TAMANHO)
    String nome,

    @Size(max = ValidationMessages.Limites.CLASSE_DESCRICAO_MAX,
          message = ValidationMessages.ClassePersonagem.DESCRICAO_TAMANHO)
    String descricao,

    Integer ordemExibicao
) {}
