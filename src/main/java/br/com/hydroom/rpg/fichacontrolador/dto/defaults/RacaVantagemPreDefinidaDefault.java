package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO default para vantagens predefinidas concedidas por raça.
 * A referência pode ser resolvida por nome ou sigla durante a inicialização.
 */
public record RacaVantagemPreDefinidaDefault(
        String vantagemNome,
        String vantagemSigla,
        Integer nivel
) {}
