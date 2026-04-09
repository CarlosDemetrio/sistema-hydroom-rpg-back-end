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
}
