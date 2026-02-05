package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuração padrão de Limitadores de Atributo.
 * Usado pelo GameDefaultConfigProvider para inicializar limitadores de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitadorConfigDTO {

    private Integer nivelInicio;
    private Integer nivelFim;
    private Integer limiteAtributo;

    /**
     * Cria um LimitadorConfigDTO com valores básicos.
     */
    public static LimitadorConfigDTO of(Integer nivelInicio, Integer nivelFim, Integer limiteAtributo) {
        return LimitadorConfigDTO.builder()
                .nivelInicio(nivelInicio)
                .nivelFim(nivelFim)
                .limiteAtributo(limiteAtributo)
                .build();
    }
}
