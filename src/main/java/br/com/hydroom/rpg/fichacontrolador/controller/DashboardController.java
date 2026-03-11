package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.response.DashboardMestreResponse;
import br.com.hydroom.rpg.fichacontrolador.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para o dashboard do Mestre com estatísticas do jogo.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Dashboard", description = "Dashboard e estatísticas do jogo para o Mestre")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/v1/jogos/{id}/dashboard")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
            summary = "Dashboard do Mestre",
            description = "Retorna estatísticas do jogo: total de fichas, participantes aprovados, fichas por nível e últimas alterações"
    )
    public ResponseEntity<DashboardMestreResponse> getDashboard(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.getDashboardMestre(id));
    }
}
