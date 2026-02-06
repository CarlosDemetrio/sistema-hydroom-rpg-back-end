package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para Nível.
 *
 * @param id ID do nível
 * @param jogoId ID do jogo
 * @param nivel Número do nível
 * @param xpNecessaria XP necessária para atingir este nível
 * @param pontosAtributo Pontos de atributo ganhos
 * @param pontosAptidao Pontos de aptidão ganhos
 * @param limitadorAtributo Valor máximo de atributo neste nível
 * @param ativo Status do nível
 * @param dataCriacao Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record NivelResponse(
    Long id,
    Long jogoId,
    Integer nivel,
    Long xpNecessaria,
    Integer pontosAtributo,
    Integer pontosAptidao,
    Integer limitadorAtributo,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
