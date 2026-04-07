package br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;

import java.time.LocalDateTime;

/**
 * Response DTO para TipoItemConfig.
 */
public record TipoItemConfigResponse(
    Long id,
    Long jogoId,
    String nome,
    CategoriaItem categoria,
    SubcategoriaItem subcategoria,
    boolean requerDuasMaos,
    int ordemExibicao,
    String descricao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
