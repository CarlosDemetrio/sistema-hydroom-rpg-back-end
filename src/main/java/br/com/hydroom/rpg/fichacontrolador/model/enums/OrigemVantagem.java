package br.com.hydroom.rpg.fichacontrolador.model.enums;

/**
 * Origem de uma FichaVantagem — distingue como a vantagem foi adquirida.
 */
public enum OrigemVantagem {
    /** Comprada pelo jogador com pontos de vantagem. */
    JOGADOR,
    /** Concedida pelo Mestre (Insolitus ou concessão manual). */
    MESTRE,
    /** Auto-concedida pelo sistema (ClasseVantagemPreDefinida ou RacaVantagemPreDefinida). */
    SISTEMA
}
