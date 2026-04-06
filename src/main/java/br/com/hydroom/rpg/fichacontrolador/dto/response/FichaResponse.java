package br.com.hydroom.rpg.fichacontrolador.dto.response;

import java.time.LocalDateTime;

/**
 * DTO de resposta para a Ficha de personagem.
 */
public record FichaResponse(
    Long id,
    Long jogoId,
    String nome,
    Long jogadorId,
    Long racaId,
    String racaNome,
    Long classeId,
    String classeNome,
    Long generoId,
    String generoNome,
    Long indoleId,
    String indoleNome,
    Long presencaId,
    String presencaNome,
    Integer nivel,
    Long xp,
    Integer renascimentos,
    boolean isNpc,
    String descricao,
    String status,
    /** Null para fichas de jogadores; true/false para NPCs. */
    Boolean visivelGlobalmente,
    /**
     * Null para fichas de jogadores. Para NPCs: true se o Jogador atual tem acesso
     * aos stats via FichaVisibilidade, false caso contrário. Preenchido no service.
     */
    Boolean jogadorTemAcessoStats,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
