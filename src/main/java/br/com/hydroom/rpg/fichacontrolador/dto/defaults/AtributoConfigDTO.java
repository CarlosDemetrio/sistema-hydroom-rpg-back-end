package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Atributos.
 * Usado pelo GameDefaultConfigProvider para inicializar atributos de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributoConfigDTO {

    private String nome;
    private String abreviacao;         // Abreviação do atributo (ex: "FOR", "AGI", "VIG")
    private String descricao;
    private String formulaImpeto;      // Fórmula exp4j (ex: "total * 3", "min(total/20, 3)")
    private String unidadeImpeto;      // Unidade de medida (ex: "kg", "metros", "RD")
    private Integer valorMinimo;
    private Integer valorMaximo;
    private Integer ordemExibicao;

    public static AtributoConfigDTO of(String nome, String abreviacao, String descricao,
                                       String formulaImpeto, String unidadeImpeto,
                                       Integer valorMinimo, Integer valorMaximo, Integer ordemExibicao) {
        return AtributoConfigDTO.builder()
                .nome(nome)
                .abreviacao(abreviacao)
                .descricao(descricao)
                .formulaImpeto(formulaImpeto)
                .unidadeImpeto(unidadeImpeto)
                .valorMinimo(valorMinimo)
                .valorMaximo(valorMaximo)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    public static AtributoConfigDTO of(String nome, String abreviacao, String descricao,
                                       String formulaImpeto, String unidadeImpeto, Integer ordemExibicao) {
        return of(nome, abreviacao, descricao, formulaImpeto, unidadeImpeto, null, null, ordemExibicao);
    }
}
