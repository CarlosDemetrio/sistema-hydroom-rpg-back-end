package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Vantagens.
 * Usado pelo GameDefaultConfigProvider para inicializar vantagens de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VantagemConfigDTO {

    private String nome;
    private String sigla;              // Ex: "TCO", "TCD", "ENF"
    private String descricao;
    private String tipoBonus;
    private String tipoVantagem;       // "VANTAGEM" or "INSOLITUS"
    private String categoriaNome;      // Ex: "Treinamento Físico"
    private String valorBonusFormula;
    private Integer custoBase;
    private String formulaCusto;       // Ex: "custo_base * nivel_vantagem"
    private Integer nivelMinimoPersonagem;
    private Boolean podeEvoluir;
    private Integer nivelMaximoVantagem;
    private Integer ordemExibicao;

    /**
     * Cria um VantagemConfigDTO básico.
     */
    public static VantagemConfigDTO of(String nome, String descricao, String tipoBonus,
                                      Integer custoBase, String formulaCusto) {
        return VantagemConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .tipoBonus(tipoBonus)
                .custoBase(custoBase)
                .formulaCusto(formulaCusto)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .build();
    }
}
