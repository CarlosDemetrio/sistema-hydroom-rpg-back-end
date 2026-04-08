package br.com.hydroom.rpg.fichacontrolador.dto.response;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;

import java.time.LocalDateTime;

/**
 * DTO de resposta para uma imagem de ficha.
 */
public record FichaImagemResponse(
    Long id,
    Long fichaId,
    String urlCloudinary,
    String publicId,
    String titulo,
    TipoImagem tipoImagem,
    Integer ordemExibicao,
    LocalDateTime dataCriacao,
    LocalDateTime dataUltimaAtualizacao
) {}
