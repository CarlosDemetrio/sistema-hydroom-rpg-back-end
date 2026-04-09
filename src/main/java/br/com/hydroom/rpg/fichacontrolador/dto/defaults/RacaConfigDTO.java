package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /** Pontos extras de atributo/vantagem concedidos por esta raça em marcos de nível. */
    @Builder.Default
    private List<PontosNivelConfigDTO> pontosConfig = List.of();

    /** Cria um RacaConfigDTO sem bônus de nível. */
    public static RacaConfigDTO of(String nome, String descricao, Integer ordemExibicao) {
        return RacaConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .build();
    }

    /** Cria um RacaConfigDTO com bônus de nível definidos. */
    public static RacaConfigDTO of(String nome, String descricao, Integer ordemExibicao,
                                   List<PontosNivelConfigDTO> pontosConfig) {
        return RacaConfigDTO.builder()
                .nome(nome)
                .descricao(descricao)
                .ordemExibicao(ordemExibicao)
                .pontosConfig(pontosConfig)
                .build();
    }
}
