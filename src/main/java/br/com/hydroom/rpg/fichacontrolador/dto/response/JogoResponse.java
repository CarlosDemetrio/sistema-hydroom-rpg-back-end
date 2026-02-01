package br.com.hydroom.rpg.fichacontrolador.dto.response;

import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response com dados completos de um jogo.
 *
 * @author Carlos Demétrio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JogoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean ativo;
    private RoleJogo meuRole;
    private Integer totalParticipantes;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
