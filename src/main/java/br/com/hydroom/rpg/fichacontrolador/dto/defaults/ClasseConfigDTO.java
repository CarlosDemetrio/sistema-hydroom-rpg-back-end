package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para configuração padrão de Classes de Personagem.
 * Usado pelo GameDefaultConfigProvider para inicializar classes de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseConfigDTO {

    private String nome;
    private String descricao;
    private Integer ordemExibicao;

    /** Pontos extras de atributo/vantagem concedidos por esta classe em cada nível. */
    @Builder.Default
    private List<PontosNivelConfigDTO> pontosConfig = List.of();

    /** Bônus de derivados concedidos por esta classe. */
    @Builder.Default
    private List<ClasseBonusDefault> bonusDefaults = List.of();

    /** Bônus fixos de aptidão concedidos por esta classe. */
    @Builder.Default
    private List<ClasseAptidaoBonusDefault> aptidaoBonusDefaults = List.of();

    /** Vantagens predefinidas concedidas por esta classe. */
    @Builder.Default
    private List<ClasseVantagemPreDefinidaDefault> vantagemPreDefinidaDefaults = List.of();

    /** Cria um ClasseConfigDTO sem bônus de nível. */
    public static ClasseConfigDTO of(String nome, String descricao, Integer ordemExibicao) {
        return ClasseConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    /** Cria um ClasseConfigDTO com bônus de nível definidos. */
    public static ClasseConfigDTO of(String nome, String descricao, Integer ordemExibicao,
                                     List<PontosNivelConfigDTO> pontosConfig) {
        return ClasseConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .pontosConfig(pontosConfig)
                .build();
    }

    /** Cria um ClasseConfigDTO completo com relacionamentos defaults definidos. */
    public static ClasseConfigDTO of(String nome, String descricao, Integer ordemExibicao,
                                     List<PontosNivelConfigDTO> pontosConfig,
                                     List<ClasseBonusDefault> bonusDefaults,
                                     List<ClasseAptidaoBonusDefault> aptidaoBonusDefaults,
                                     List<ClasseVantagemPreDefinidaDefault> vantagemPreDefinidaDefaults) {
        return ClasseConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .pontosConfig(pontosConfig)
                .bonusDefaults(bonusDefaults)
                .aptidaoBonusDefaults(aptidaoBonusDefaults)
                .vantagemPreDefinidaDefaults(vantagemPreDefinidaDefaults)
                .build();
    }
}
