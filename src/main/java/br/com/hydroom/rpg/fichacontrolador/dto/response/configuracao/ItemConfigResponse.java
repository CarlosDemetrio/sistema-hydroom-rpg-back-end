package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response completo de ItemConfig, incluindo efeitos e requisitos.
 * Utilizado no endpoint de detalhe (GET por ID).
 */
public record ItemConfigResponse(
    Long id,
    Long jogoId,
    String nome,
    Long raridadeId,
    String raridadeNome,
    String raridadeCor,
    Long tipoId,
    String tipoNome,
    CategoriaItem categoria,
    BigDecimal peso,
    Integer valor,
    Integer duracaoPadrao,
    int nivelMinimo,
    String propriedades,
    String descricao,
    int ordemExibicao,
    List<ItemEfeitoResponse> efeitos,
    List<ItemRequisitoResponse> requisitos,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
