package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para criação de uma Ficha de personagem.
 */
public record CreateFichaRequest(

    @NotNull(message = "Jogo é obrigatório")
    Long jogoId,

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    /**
     * ID do jogador dono da ficha. Null para NPCs ou quando o próprio usuário é o dono.
     */
    Long jogadorId,

    Long racaId,

    Long classeId,

    Long generoId,

    Long indoleId,

    Long presencaId,

    Boolean isNpc
) {
    public CreateFichaRequest {
        if (isNpc == null) {
            isNpc = false;
        }
    }
}
