package br.com.hydroom.rpg.fichacontrolador.model.enums;

/**
 * Representa o papel de um usuário em um jogo.
 *
 * @author Carlos Demétrio
 */
public enum RoleJogo {

    /**
     * Mestre do jogo - tem permissões administrativas completas.
     */
    MESTRE,

    /**
     * Jogador - pode gerenciar apenas suas próprias fichas.
     */
    JOGADOR
}
