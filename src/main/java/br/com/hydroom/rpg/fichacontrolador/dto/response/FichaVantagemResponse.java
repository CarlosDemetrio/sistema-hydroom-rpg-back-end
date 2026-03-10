package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * Response com dados de uma vantagem comprada por uma ficha.
 */
public record FichaVantagemResponse(
    Long id,
    Long vantagemConfigId,
    String nomeVantagem,
    Integer nivelAtual,
    Integer nivelMaximo,
    Integer custoPago
) {}
