package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Dados de Prospecção.
 * Usado pelo GameDefaultConfigProvider para inicializar dados de prospecção de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspeccaoConfigDTO {

    private String nome;               // Ex: "d3", "d4", "d6", "d8", "d10", "d12"
    private Integer numLados;          // Número de lados do dado
    private String descricao;
    private Integer ordemExibicao;

    public static ProspeccaoConfigDTO of(String nome, Integer numLados, String descricao, Integer ordemExibicao) {
        return ProspeccaoConfigDTO.builder()
                .nome(nome)
                .numLados(numLados)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    public static ProspeccaoConfigDTO of(String nome, Integer numLados, Integer ordemExibicao) {
        return of(nome, numLados, null, ordemExibicao);
    }
}
