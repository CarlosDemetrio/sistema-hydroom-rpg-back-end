package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Níveis.
 * Usado pelo GameDefaultConfigProvider para inicializar níveis de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NivelConfigDTO {

    private Integer nivel;
    private Long experienciaNecessaria;
    private Integer pontosAtributo;
    private Integer pontosVantagem;
    private Integer pontosAptidao;
    private Integer limitadorAtributo;

    /**
     * Cria um NivelConfigDTO com todos os campos.
     */
    public static NivelConfigDTO of(Integer nivel, Long experienciaNecessaria,
                                   Integer pontosAtributo, Integer pontosVantagem,
                                   Integer pontosAptidao, Integer limitadorAtributo) {
        return NivelConfigDTO.builder()
                .nivel(nivel)
                .experienciaNecessaria(experienciaNecessaria)
                .pontosAtributo(pontosAtributo)
                .pontosVantagem(pontosVantagem)
                .pontosAptidao(pontosAptidao)
                .limitadorAtributo(limitadorAtributo)
                .build();
    }

    /**
     * Cria um NivelConfigDTO sem limitadorAtributo (default null).
     * Usado pelo DefaultGameConfigProvider quando o limitador é definido
     * separadamente via LimitadorConfigDTO.
     */
    public static NivelConfigDTO of(Integer nivel, Long experienciaNecessaria,
                                   Integer pontosAtributo, Integer pontosVantagem,
                                   Integer pontosAptidao) {
        return of(nivel, experienciaNecessaria, pontosAtributo, pontosVantagem, pontosAptidao, null);
    }
}
