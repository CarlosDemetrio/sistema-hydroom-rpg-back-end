package br.com.hydroom.rpg.fichacontrolador.dto.defaults;

/**
 * DTO default para pré-requisitos de vantagem.
 * A referência é feita por sigla para evitar ambiguidades e facilitar a resolução durante a inicialização.
 */
public record VantagemPreRequisitoDefault(
        String requisitoSigla,
        Integer nivelMinimo
) {}
