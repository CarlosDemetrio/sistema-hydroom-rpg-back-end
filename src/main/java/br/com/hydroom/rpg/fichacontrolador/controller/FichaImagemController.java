package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarImagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaImagemResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaImagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoImagem;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import br.com.hydroom.rpg.fichacontrolador.service.FichaImagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller para gerenciamento da galeria de imagens de fichas.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Galeria de Imagens", description = "Upload e gerenciamento de imagens em fichas")
@RequestMapping("/api/v1/fichas/{fichaId}/imagens")
public class FichaImagemController {

    private final FichaImagemService fichaImagemService;
    private final FichaImagemMapper fichaImagemMapper;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Listar imagens da ficha",
        description = "Retorna todas as imagens ativas da ficha. AVATAR aparece primeiro, depois GALERIA por ordemExibicao."
    )
    public ResponseEntity<List<FichaImagemResponse>> listar(@PathVariable Long fichaId) {
        Long usuarioAtualId = getUsuarioAtualId();
        var imagens = fichaImagemService.listar(fichaId, usuarioAtualId);
        var response = imagens.stream().map(fichaImagemMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Fazer upload de imagem",
        description = "Faz upload de imagem para o Cloudinary e vincula à ficha. " +
                      "Tipos aceitos: JPEG, PNG, WebP, GIF. Tamanho máximo: 10 MB. " +
                      "Ao adicionar um novo AVATAR, o avatar anterior é convertido para GALERIA."
    )
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
        schema = @Schema(implementation = UploadImagemSchema.class)))
    public ResponseEntity<FichaImagemResponse> adicionar(
            @PathVariable Long fichaId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("tipoImagem") TipoImagem tipoImagem,
            @RequestParam(value = "titulo", required = false) String titulo) {
        Long usuarioAtualId = getUsuarioAtualId();
        var imagem = fichaImagemService.adicionar(fichaId, arquivo, tipoImagem, titulo, usuarioAtualId);
        return ResponseEntity.status(HttpStatus.CREATED).body(fichaImagemMapper.toResponse(imagem));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Atualizar metadados da imagem",
        description = "Atualiza título e/ou ordem de exibição. Campos nulos são ignorados. " +
                      "urlCloudinary, publicId e tipoImagem são imutáveis após upload."
    )
    public ResponseEntity<FichaImagemResponse> atualizar(
            @PathVariable Long fichaId,
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody AtualizarImagemRequest request) {
        Long usuarioAtualId = getUsuarioAtualId();
        var imagem = fichaImagemService.atualizar(fichaId, id, request, usuarioAtualId);
        return ResponseEntity.ok(fichaImagemMapper.toResponse(imagem));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Deletar imagem",
        description = "Remove a imagem do Cloudinary e realiza soft delete local. " +
                      "Se a remoção do Cloudinary falhar, ela é apenas logada — o soft delete local prossegue normalmente."
    )
    public ResponseEntity<Void> deletar(
            @PathVariable Long fichaId,
            @PathVariable Long id) {
        Long usuarioAtualId = getUsuarioAtualId();
        fichaImagemService.deletar(fichaId, id, usuarioAtualId);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIVADOS ====================

    private Long getUsuarioAtualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
        return usuario.getId();
    }

    /**
     * Schema auxiliar para documentação Swagger do endpoint multipart.
     */
    @Schema(name = "UploadImagemRequest")
    private static class UploadImagemSchema {
        @Schema(type = "string", format = "binary", description = "Arquivo de imagem (JPEG, PNG, WebP, GIF, máx 10 MB)")
        public MultipartFile arquivo;

        @Schema(description = "Tipo da imagem", allowableValues = {"AVATAR", "GALERIA"}, requiredMode = Schema.RequiredMode.REQUIRED)
        public TipoImagem tipoImagem;

        @Schema(description = "Título opcional da imagem", maxLength = 200)
        public String titulo;
    }
}
