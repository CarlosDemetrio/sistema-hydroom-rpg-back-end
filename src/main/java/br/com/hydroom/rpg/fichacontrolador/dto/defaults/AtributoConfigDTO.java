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
    private Integer ordemExibicao;

    /**
     * Cria um AtributoConfigDTO com valores básicos.
     *
     * @param nome Nome do atributo
     * @param abreviacao Abreviação do atributo (2-5 caracteres)
     * @param descricao Descrição
     * @param formulaImpeto Fórmula de cálculo do ímpeto
     * @param unidadeImpeto Unidade de medida
     * @param ordemExibicao Ordem de exibição
     * @return AtributoConfigDTO configurado
     */
    public static AtributoConfigDTO of(String nome, String abreviacao, String descricao,
                                       String formulaImpeto, String unidadeImpeto, Integer ordemExibicao) {
        return AtributoConfigDTO.builder()
                .nome(nome)
                .abreviacao(abreviacao)
                .descricao(descricao)
                .formulaImpeto(formulaImpeto)
                .unidadeImpeto(unidadeImpeto)
                .ordemExibicao(ordemExibicao)
                .build();
    }
}
