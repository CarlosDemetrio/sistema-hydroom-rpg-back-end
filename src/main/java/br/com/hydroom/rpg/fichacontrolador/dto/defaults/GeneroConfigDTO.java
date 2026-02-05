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
    private Integer ordemExibicao;

    /**
     * Cria um GeneroConfigDTO com valores básicos.
     */
    public static GeneroConfigDTO of(String nome, Integer ordemExibicao) {
        return GeneroConfigDTO.builder()
                .nome(nome)
                .ordemExibicao(ordemExibicao)
                .build();
    }
}
