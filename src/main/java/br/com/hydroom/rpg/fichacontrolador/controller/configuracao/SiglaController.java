package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.SiglaEmUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.SiglaValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller REST para consulta de siglas em uso por jogo.
 */
@RestController
@RequestMapping("/api/jogos/{jogoId}/siglas")
@RequiredArgsConstructor
@Tag(name = "Siglas", description = "Consulta de siglas e abreviações em uso por jogo")
@SecurityRequirement(name = "cookieAuth")
public class SiglaController {

    private final SiglaValidationService siglaValidationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Lista todas as siglas em uso no jogo",
        description = "Retorna siglas de atributos, bônus e vantagens, ordenadas alfabeticamente. " +
                      "Útil para verificar disponibilidade antes de criar configurações."
    )
    public ResponseEntity<List<SiglaEmUsoResponse>> listar(@PathVariable Long jogoId) {
        return ResponseEntity.ok(siglaValidationService.listarSiglasDoJogo(jogoId));
    }
}
