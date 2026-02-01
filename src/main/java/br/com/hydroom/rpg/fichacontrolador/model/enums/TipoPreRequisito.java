package br.com.hydroom.rpg.fichacontrolador.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de pré-requisitos para vantagens.
 */
@Getter
@RequiredArgsConstructor
public enum TipoPreRequisito {
    ATRIBUTO("Atributo", "Requer valor mínimo em um atributo"),
    NIVEL("Nível", "Requer nível mínimo do personagem"),
    CLASSE("Classe", "Requer classe específica"),
    RACA("Raça", "Requer raça específica"),
    VANTAGEM("Vantagem", "Requer outra vantagem"),
    APTIDAO("Aptidão", "Requer valor mínimo em uma aptidão");

    private final String nome;
    private final String descricao;
}
