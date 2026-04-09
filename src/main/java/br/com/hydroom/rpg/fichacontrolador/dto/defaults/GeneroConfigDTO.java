package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Gêneros.
 * Usado pelo GameDefaultConfigProvider para inicializar gêneros de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneroConfigDTO {

    private String nome;
    private String descricao;
    private Integer ordemExibicao;

    public static GeneroConfigDTO of(String nome, String descricao, Integer ordemExibicao) {
        return GeneroConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    public static GeneroConfigDTO of(String nome, Integer ordemExibicao) {
        return of(nome, null, ordemExibicao);
    }
}
