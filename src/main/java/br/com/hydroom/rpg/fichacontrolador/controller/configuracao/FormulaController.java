package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.FormulaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.FormulaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VariaveisDisponiveisResponse;
import br.com.hydroom.rpg.fichacontrolador.service.FormulaPreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para preview e listagem de variáveis disponíveis para fórmulas.
 */
@RestController
@RequestMapping("/api/jogos/{jogoId}/formulas")
@RequiredArgsConstructor
@Tag(name = "Fórmulas", description = "Preview e variáveis disponíveis para fórmulas do jogo")
@SecurityRequirement(name = "cookieAuth")
public class FormulaController {

    private final FormulaPreviewService formulaPreviewService;

    @PostMapping("/preview")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Testa uma fórmula com valores de exemplo",
        description = "Valida a fórmula e calcula o resultado com os valores fornecidos, sem persistir nada."
    )
    public ResponseEntity<FormulaPreviewResponse> preview(
            @PathVariable Long jogoId,
            @RequestBody @Valid FormulaPreviewRequest request) {
        return ResponseEntity.ok(formulaPreviewService.preview(jogoId, request));
    }

    @GetMapping("/variaveis")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Lista variáveis disponíveis para fórmulas",
        description = "Retorna siglas agrupadas por tipo (atributos, bônus, vantagens) e variáveis fixas do sistema."
    )
    public ResponseEntity<VariaveisDisponiveisResponse> variaveis(@PathVariable Long jogoId) {
        return ResponseEntity.ok(formulaPreviewService.listarVariaveis(jogoId));
    }
}
