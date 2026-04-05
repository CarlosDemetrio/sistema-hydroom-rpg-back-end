package br.com.hydroom.rpg.fichacontrolador.model.enums;

/**
 * Tipo de fórmula, que determina quais variáveis são permitidas.
 */
public enum TipoFormula {
    /** Fórmula de ímpeto de atributo — variável: total */
    IMPETO,
    /** Fórmula base de bônus — variáveis: siglas dos atributos do jogo + nivel, base */
    BONUS,
    /** Fórmula de custo de vantagem — variáveis: custo_base, nivel_vantagem */
    CUSTO_VANTAGEM
}
