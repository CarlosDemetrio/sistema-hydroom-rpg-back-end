package br.com.hydroom.rpg.fichacontrolador.dto.request;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request para editar um jogo existente.
 *
 * @author Carlos Demetrio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarJogoRequest {

    @NotBlank(message = ValidationMessages.Jogo.NOME_OBRIGATORIO)
    @Size(min = ValidationMessages.Limites.JOGO_NOME_MIN,
          max = ValidationMessages.Limites.JOGO_NOME_MAX,
          message = ValidationMessages.Jogo.NOME_TAMANHO)
    private String nome;

    @Size(max = ValidationMessages.Limites.JOGO_DESCRICAO_MAX,
          message = ValidationMessages.Jogo.DESCRICAO_TAMANHO)
    private String descricao;

    @Size(max = ValidationMessages.Limites.JOGO_IMAGEM_URL_MAX,
          message = ValidationMessages.Jogo.IMAGEM_URL_TAMANHO)
    private String imagemUrl;

    private LocalDate dataInicio;

    private LocalDate dataFim;
}
