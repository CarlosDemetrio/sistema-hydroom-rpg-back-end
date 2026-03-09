package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;

/**
 * Response DTO para PontosVantagemConfig.
 *
 * @param id                    ID do registro
 * @param jogoId                ID do jogo
 * @param nivel                 Nível ao qual os pontos se aplicam
 * @param pontosGanhos          Pontos de vantagem ganhos ao atingir este nível
 * @param dataCriacao           Data de criação
 * @param dataUltimaAtualizacao Data da última atualização
 */
public record PontosVantagemResponse(
    Long id,
    Long jogoId,
    Integer nivel,
    Integer pontosGanhos,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
