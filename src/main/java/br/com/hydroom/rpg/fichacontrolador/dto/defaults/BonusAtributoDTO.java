package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para bônus de atributo racial.
 * Usado pelo GameDefaultConfigProvider para inicializar bônus raciais de um jogo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusAtributoDTO {

    private String abreviacaoAtributo;  // Abreviação do atributo (ex: "FOR", "AGI", "VIG")
    private Integer bonus;              // Valor do bônus (pode ser negativo)

    /**
     * Cria um BonusAtributoDTO com valores básicos.
     *
     * @param abreviacaoAtributo Abreviação do atributo (ex: "FOR", "AGI")
     * @param bonus Valor do bônus (positivo ou negativo)
     * @return BonusAtributoDTO configurado
     */
    public static BonusAtributoDTO of(String abreviacaoAtributo, Integer bonus) {
        return BonusAtributoDTO.builder()
                .abreviacaoAtributo(abreviacaoAtributo)
                .bonus(bonus)
                .build();
    }
}
