package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request para criar um novo jogo.
 *
 * @author Carlos Demétrio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriarJogoRequest {

    @NotBlank(message = "Nome do jogo é obrigatório")
    @Size(max = 200, message = "Nome do jogo deve ter no máximo 200 caracteres")
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    private LocalDate dataInicio;
}
