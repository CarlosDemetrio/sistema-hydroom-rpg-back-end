package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.dto.response.UsuarioResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.mapper.JogoMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.UsuarioMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de autenticação e informações do usuário")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final JogoService jogoService;
    private final JogoMapper jogoMapper;

    @Operation(summary = "Obter dados completos do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @SecurityRequirement(name = "Session")
    @GetMapping("/api/v1/auth/me")
    public ResponseEntity<UsuarioResponse> getMe(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String providerId = principal.getAttribute("sub");
        log.debug("Buscando usuário autenticado - ProviderId: {}", providerId);

        Usuario usuario = usuarioRepository.findByProviderId(providerId)
                .orElseThrow(() -> {
                    log.error("Usuário autenticado não encontrado no banco - ProviderId: {}", providerId);
                    return new BusinessException("Usuário não encontrado");
                });

        UsuarioResponse response = usuarioMapper.toResponse(usuario);

        // Se for MESTRE, buscar jogo ativo
        if ("MESTRE".equals(usuario.getRole())) {
            try {
                var jogoAtivo = jogoService.buscarJogoAtivo();
                response.setJogoAtivo(jogoMapper.toResumoResponse(jogoAtivo));
                log.debug("Jogo ativo encontrado para mestre - Jogo ID: {}", jogoAtivo.getId());
            } catch (IllegalStateException e) {
                // Nenhum jogo ativo - jogoAtivo fica null
                log.debug("Nenhum jogo ativo para o mestre - ID: {}", usuario.getId());
            }
        }

        log.debug("Usuário autenticado retornado - ID: {}, Email: {}", usuario.getId(), usuario.getEmail());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Realizar logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @SecurityRequirement(name = "Session")
    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String email = principal.getAttribute("email");
        log.info("Realizando logout - Email: {}", email);

        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, null);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Obter informações do usuário autenticado (formato simplificado)",
            description = "Retorna os dados do usuário logado via OAuth2 (nome, email, foto) - DEPRECATED: Use /api/v1/auth/me"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Informações do usuário retornadas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "João Silva",
                                      "email": "joao@example.com",
                                      "picture": "https://example.com/photo.jpg"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            )
    })
    @SecurityRequirement(name = "Session")
    @GetMapping("/api/user")
    public Map<String, Object> user(
            @AuthenticationPrincipal OAuth2User principal,
            HttpServletRequest request) {

        if (principal == null) {
            log.warn("Tentativa de acesso não autorizado ao /api/user de IP: {}",
                    request.getRemoteAddr());
            throw new BusinessException(ValidationMessages.Seguranca.NAO_AUTENTICADO);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", principal.getAttribute("name"));
        userInfo.put("email", principal.getAttribute("email"));
        userInfo.put("picture", principal.getAttribute("picture"));

        log.info("Usuário {} (email: {}) acessou informações de perfil de IP: {}",
                principal.getAttribute("name"),
                principal.getAttribute("email"),
                request.getRemoteAddr());

        return userInfo;
    }

    @Operation(
            summary = "Health check da API",
            description = "Verifica se a API está funcionando corretamente (endpoint público)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "API está funcionando",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": "UP",
                                      "message": "API Ficha Controlador está funcionando!"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/api/public/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "API Ficha Controlador está funcionando!");
        return response;
    }
}
