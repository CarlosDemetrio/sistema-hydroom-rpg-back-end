package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Raças.
 * Usado pelo GameDefaultConfigProvider para inicializar raças de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacaConfigDTO {

    private String nome;
    private String descricao;
    private Integer ordemExibicao;

    /**
     * Cria um RacaConfigDTO com valores básicos.
     */
    public static RacaConfigDTO of(String nome, String descricao, Integer ordemExibicao) {
        return RacaConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }
}
