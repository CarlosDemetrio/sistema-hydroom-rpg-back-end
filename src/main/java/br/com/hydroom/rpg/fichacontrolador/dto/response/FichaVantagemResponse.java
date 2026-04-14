package br.com.hydroom.rpg.fichacontrolador.dto.response;

/**
 * Response com dados de uma vantagem de uma ficha (comprada ou concedida pelo Mestre).
 */
public record FichaVantagemResponse(
    Long id,
    Long vantagemConfigId,
    String nomeVantagem,
    String categoriaNome,
    String tipoVantagem,
    Integer nivelAtual,
    Integer nivelMaximo,
    Integer custoPago,
    Boolean concedidoPeloMestre,
    String origem
) {}
