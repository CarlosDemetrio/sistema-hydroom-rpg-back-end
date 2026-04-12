package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO default para vantagens predefinidas concedidas por classe.
 * A referência pode ser resolvida por nome ou sigla durante a inicialização.
 */
public record ClasseVantagemPreDefinidaDefault(
        String vantagemNome,
        String vantagemSigla,
        Integer nivel
) {}
