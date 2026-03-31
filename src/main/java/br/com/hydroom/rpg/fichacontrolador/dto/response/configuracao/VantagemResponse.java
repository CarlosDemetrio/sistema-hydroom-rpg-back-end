package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO para Vantagem.
 */
public record VantagemResponse(
    Long id,
    Long jogoId,
    String nome,
    String sigla,
    String descricao,
    Long categoriaVantagemId,
    String categoriaNome,
    Integer nivelMaximo,
    String formulaCusto,
    String descricaoEfeito,
    Integer ordemExibicao,
    List<VantagemPreRequisitoResponse> preRequisitos,
    List<VantagemEfeitoResponse> efeitos,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
