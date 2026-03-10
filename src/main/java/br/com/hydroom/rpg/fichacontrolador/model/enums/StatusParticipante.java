package br.com.hydroom.rpg.fichacontrolador.model.enums;

/**
 * Status de participação de um usuário em um jogo.
 */
public enum StatusParticipante {

    /** Solicitação pendente de aprovação pelo Mestre. */
    PENDENTE,

    /** Participação aprovada — acesso completo aos recursos do jogo. */
    APROVADO,

    /** Solicitação rejeitada pelo Mestre. */
    REJEITADO,

    /** Participante banido pelo Mestre. */
    BANIDO
}
