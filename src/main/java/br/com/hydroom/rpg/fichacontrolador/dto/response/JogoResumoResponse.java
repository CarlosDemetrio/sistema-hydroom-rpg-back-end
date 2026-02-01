package br.com.hydroom.rpg.fichacontrolador.dto.response;

import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response resumido de um jogo (para listagens).
 *
 * @author Carlos Demétrio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JogoResumoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private RoleJogo meuRole;
    private Integer totalParticipantes;
    private Boolean ativo;
}
