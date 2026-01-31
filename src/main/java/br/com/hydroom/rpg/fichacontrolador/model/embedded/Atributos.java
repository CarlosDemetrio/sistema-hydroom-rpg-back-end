package br.com.hydroom.rpg.fichacontrolador.model.embedded;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Atributos básicos de um personagem de RPG.
 * Usado como @Embeddable na entidade Ficha.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Atributos {

    @Min(value = ValidationMessages.Limites.ATRIBUTO_MIN, message = ValidationMessages.Atributos.FORCA_MINIMO)
    @Max(value = ValidationMessages.Limites.ATRIBUTO_MAX, message = ValidationMessages.Atributos.FORCA_MAXIMO)
    private Integer forca;

    @Min(value = ValidationMessages.Limites.ATRIBUTO_MIN, message = ValidationMessages.Atributos.DESTREZA_MINIMO)
    @Max(value = ValidationMessages.Limites.ATRIBUTO_MAX, message = ValidationMessages.Atributos.DESTREZA_MAXIMO)
    private Integer destreza;

    @Min(value = ValidationMessages.Limites.ATRIBUTO_MIN, message = ValidationMessages.Atributos.CONSTITUICAO_MINIMO)
    @Max(value = ValidationMessages.Limites.ATRIBUTO_MAX, message = ValidationMessages.Atributos.CONSTITUICAO_MAXIMO)
    private Integer constituicao;

    @Min(value = ValidationMessages.Limites.ATRIBUTO_MIN, message = ValidationMessages.Atributos.INTELIGENCIA_MINIMO)
    @Max(value = ValidationMessages.Limites.ATRIBUTO_MAX, message = ValidationMessages.Atributos.INTELIGENCIA_MAXIMO)
    private Integer inteligencia;

    @Min(value = ValidationMessages.Limites.ATRIBUTO_MIN, message = ValidationMessages.Atributos.SABEDORIA_MINIMO)
    @Max(value = ValidationMessages.Limites.ATRIBUTO_MAX, message = ValidationMessages.Atributos.SABEDORIA_MAXIMO)
    private Integer sabedoria;

    @Min(value = ValidationMessages.Limites.ATRIBUTO_MIN, message = ValidationMessages.Atributos.CARISMA_MINIMO)
    @Max(value = ValidationMessages.Limites.ATRIBUTO_MAX, message = ValidationMessages.Atributos.CARISMA_MAXIMO)
    private Integer carisma;

    /**
     * Calcula modificador de atributo baseado no valor.
     * Fórmula D&D: (atributo - 10) / 2 (arredondado para baixo)
     */
    public int calcularModificador(int atributo) {
        return Math.floorDiv(atributo - 10, 2);
    }

    public int getModificadorForca() {
        return forca != null ? calcularModificador(forca) : 0;
    }

    public int getModificadorDestreza() {
        return destreza != null ? calcularModificador(destreza) : 0;
    }

    public int getModificadorConstituicao() {
        return constituicao != null ? calcularModificador(constituicao) : 0;
    }

    public int getModificadorInteligencia() {
        return inteligencia != null ? calcularModificador(inteligencia) : 0;
    }

    public int getModificadorSabedoria() {
        return sabedoria != null ? calcularModificador(sabedoria) : 0;
    }

    public int getModificadorCarisma() {
        return carisma != null ? calcularModificador(carisma) : 0;
    }
}
