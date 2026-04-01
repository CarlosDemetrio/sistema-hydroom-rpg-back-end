package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarUsuarioRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.UsuarioResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.UsuarioMapper;
import br.com.hydroom.rpg.fichacontrolador.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para gerenciamento do perfil do usuário autenticado.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Usuários", description = "Gerenciamento do perfil do usuário autenticado")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obter perfil do usuário autenticado",
               description = "Retorna os dados do usuário logado via OAuth2")
    public ResponseEntity<UsuarioResponse> getMe() {
        var usuario = usuarioService.buscarAtual();
        var response = usuarioMapper.toResponse(usuario);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualizar perfil do usuário autenticado",
               description = "Atualiza o nome do usuário. Email e foto são gerenciados pelo Google OAuth2 e não podem ser alterados aqui.")
    public ResponseEntity<UsuarioResponse> atualizarMe(
            @Valid @RequestBody AtualizarUsuarioRequest request) {
        var usuario = usuarioService.atualizarNome(request.nome());
        var response = usuarioMapper.toResponse(usuario);
        return ResponseEntity.ok(response);
    }
}
