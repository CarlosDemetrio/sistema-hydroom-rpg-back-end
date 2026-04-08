package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Request para atualização parcial de metadados de imagem (título e ordem).
 * urlCloudinary, publicId e tipoImagem são imutáveis após o upload.
 * Campos nulos são ignorados (NullValuePropertyMappingStrategy.IGNORE no mapper).
 */
public record AtualizarImagemRequest(
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    String titulo,

    Integer ordemExibicao
) {}
