package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints de autenticação e informações do usuário")
public class AuthController {

    @Operation(
            summary = "Obter informações do usuário autenticado",
            description = "Retorna os dados do usuário logado via OAuth2 (nome, email, foto)"
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
