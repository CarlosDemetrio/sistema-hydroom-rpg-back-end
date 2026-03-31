package br.com.hydroom.rpg.fichacontrolador.model.enums;

/**
 * Tipos de efeitos que uma vantagem pode aplicar.
 */
public enum TipoEfeito {
    BONUS_ATRIBUTO,       // bônus em um AtributoConfig específico
    BONUS_APTIDAO,        // bônus em uma AptidaoConfig específica
    BONUS_DERIVADO,       // bônus em um BonusConfig específico
    BONUS_VIDA,           // mais pontos de vida total (VT)
    BONUS_VIDA_MEMBRO,    // mais vida em um MembroCorpoConfig específico
    BONUS_ESSENCIA,       // mais pontos de essência
    DADO_UP,              // evolução de dado de prospecção (d3→d4→d6→d8→d10→d12)
    FORMULA_CUSTOMIZADA   // fórmula exp4j com variáveis (nivel_vantagem, etc.)
}
