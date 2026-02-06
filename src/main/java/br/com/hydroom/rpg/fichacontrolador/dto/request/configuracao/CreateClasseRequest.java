package br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para criar uma nova classe de personagem.
 *
 * @param jogoId ID do jogo ao qual a classe pertence
 * @param nome Nome da classe (ex: Guerreiro, Mago)
 * @param descricao Descrição da classe
 * @param ordemExibicao Ordem de exibição na interface
 */
public record CreateClasseRequest(
    @NotNull(message = ValidationMessages.ClassePersonagem.JOGO_OBRIGATORIO)
    Long jogoId,

    @NotBlank(message = ValidationMessages.ClassePersonagem.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.CLASSE_NOME_MIN,
          max = ValidationMessages.Limites.CLASSE_NOME_MAX,
          message = ValidationMessages.ClassePersonagem.NOME_TAMANHO)
    String nome,

    @Size(max = ValidationMessages.Limites.CLASSE_DESCRICAO_MAX,
          message = ValidationMessages.ClassePersonagem.DESCRICAO_TAMANHO)
    String descricao,

    Integer ordemExibicao
) {}
