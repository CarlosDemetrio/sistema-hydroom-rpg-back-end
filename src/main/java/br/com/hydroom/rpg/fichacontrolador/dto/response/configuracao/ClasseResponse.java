package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO para Classe de Personagem.
 */
public record ClasseResponse(
    Long id,
    Long jogoId,
    String nome,
    String descricao,
    Integer ordemExibicao,
    List<ClasseBonusResponse> bonusConfig,
    List<ClasseAptidaoBonusResponse> aptidaoBonus,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
