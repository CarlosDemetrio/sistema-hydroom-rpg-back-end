package br.com.hydroom.rpg.fichacontrolador.dto.request;

import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request para upload de imagem de ficha via multipart/form-data.
 * Campos são recebidos como @RequestParam separados no controller.
 */
public record UploadImagemRequest(
    MultipartFile arquivo,

    @NotNull(message = "Tipo de imagem é obrigatório")
    TipoImagem tipoImagem,

    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    String titulo
) {}
