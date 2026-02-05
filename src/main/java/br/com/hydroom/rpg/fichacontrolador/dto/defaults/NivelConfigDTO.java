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
    private Integer pontosAptidao;      // ⭐ NOVO: pontos de aptidão por nível

    /**
     * Cria um NivelConfigDTO com valores básicos.
     */
    public static NivelConfigDTO of(Integer nivel, Long experienciaNecessaria,
                                   Integer pontosAtributo, Integer pontosVantagem, Integer pontosAptidao) {
        return NivelConfigDTO.builder()
                .nivel(nivel)
                .experienciaNecessaria(experienciaNecessaria)
                .pontosAtributo(pontosAtributo)
                .pontosVantagem(pontosVantagem)
                .pontosAptidao(pontosAptidao)
                .build();
    }
}
