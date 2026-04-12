package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO default para classes explicitamente permitidas por raça.
 * A referência à classe é feita por nome e resolvida durante a inicialização.
 */
public record RacaClassePermitidaDefault(
        String classeNome
) {}
