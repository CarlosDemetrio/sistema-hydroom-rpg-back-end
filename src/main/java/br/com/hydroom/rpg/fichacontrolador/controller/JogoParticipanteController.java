package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.response.ParticipanteResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.JogoParticipanteMapper;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.service.JogoParticipanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de participantes em jogos.
 * Cobre o fluxo de solicitação, aprovação, rejeição, banimento e remoção.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/jogos/{jogoId}/participantes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Participantes", description = "Fluxo de entrada e aprovação de jogadores em jogos")
public class JogoParticipanteController {

    private final JogoParticipanteService participanteService;
    private final JogoParticipanteMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Listar participantes do jogo",
        description = "Mestre vê todos os participantes (filtro opcional por status). Jogador vê apenas APROVADOS."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<List<ParticipanteResponse>> listar(
            @Parameter(description = "ID do jogo", required = true) @PathVariable Long jogoId,
            @Parameter(description = "Filtro por status (apenas para Mestre)") @RequestParam(required = false) StatusParticipante status) {
        return ResponseEntity.ok(
            participanteService.listar(jogoId, status).stream().map(mapper::toResponse).toList()
        );
    }

    @GetMapping("/meu-status")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Ver próprio status de participação",
        description = "Retorna o registro de participação do usuário autenticado. 404 se nunca solicitou."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Participação não encontrada")
    })
    public ResponseEntity<ParticipanteResponse> meuStatus(@PathVariable Long jogoId) {
        return participanteService.meuStatus(jogoId)
            .map(mapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Solicitar entrada no jogo",
        description = "Cria participação com status PENDENTE aguardando aprovação do Mestre."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Mestre não pode solicitar entrada no próprio jogo"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Participação já existente")
    })
    public ResponseEntity<ParticipanteResponse> solicitar(
            @Parameter(description = "ID do jogo", required = true) @PathVariable Long jogoId) {
        log.info("Solicitação de entrada no jogo {}", jogoId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toResponse(participanteService.solicitar(jogoId)));
    }

    @PutMapping("/{participanteId}/aprovar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Aprovar solicitação de participação (Apenas MESTRE)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participante aprovado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Status inválido para aprovação"),
        @ApiResponse(responseCode = "403", description = "Não é o Mestre deste jogo"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<ParticipanteResponse> aprovar(
            @PathVariable Long jogoId,
            @PathVariable Long participanteId) {
        log.info("Aprovando participante {} no jogo {}", participanteId, jogoId);
        return ResponseEntity.ok(mapper.toResponse(participanteService.aprovar(jogoId, participanteId)));
    }

    @PutMapping("/{participanteId}/rejeitar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Rejeitar solicitação de participação (Apenas MESTRE)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participante rejeitado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Status inválido para rejeição"),
        @ApiResponse(responseCode = "403", description = "Não é o Mestre deste jogo"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<ParticipanteResponse> rejeitar(
            @PathVariable Long jogoId,
            @PathVariable Long participanteId) {
        log.info("Rejeitando participante {} no jogo {}", participanteId, jogoId);
        return ResponseEntity.ok(mapper.toResponse(participanteService.rejeitar(jogoId, participanteId)));
    }

    @PutMapping("/{participanteId}/banir")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Banir participante aprovado (Apenas MESTRE)",
        description = "Transição APROVADO → BANIDO. Jogador não pode mais re-solicitar enquanto banido."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participante banido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Participante não está APROVADO ou tentativa de auto-banimento"),
        @ApiResponse(responseCode = "403", description = "Não é o Mestre deste jogo"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<ParticipanteResponse> banir(
            @PathVariable Long jogoId,
            @PathVariable Long participanteId) {
        log.info("Banindo participante {} no jogo {}", participanteId, jogoId);
        return ResponseEntity.ok(mapper.toResponse(participanteService.banir(jogoId, participanteId)));
    }

    @PutMapping("/{participanteId}/desbanir")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Desbanir participante banido (Apenas MESTRE)",
        description = "Transição BANIDO → APROVADO. Não exige nova solicitação."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participante desbanido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Participante não está BANIDO"),
        @ApiResponse(responseCode = "403", description = "Não é o Mestre deste jogo"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<ParticipanteResponse> desbanir(
            @PathVariable Long jogoId,
            @PathVariable Long participanteId) {
        log.info("Desbanindo participante {} no jogo {}", participanteId, jogoId);
        return ResponseEntity.ok(mapper.toResponse(participanteService.desbanir(jogoId, participanteId)));
    }

    @DeleteMapping("/{participanteId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Remover participante provisoriamente (Apenas MESTRE)",
        description = "Soft delete. Jogador pode re-solicitar. Use /banir para restrição permanente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Participante removido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Participante não está APROVADO"),
        @ApiResponse(responseCode = "403", description = "Não é o Mestre deste jogo"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<Void> remover(
            @PathVariable Long jogoId,
            @PathVariable Long participanteId) {
        log.info("Removendo participante {} do jogo {}", participanteId, jogoId);
        participanteService.remover(jogoId, participanteId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/minha-solicitacao")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Cancelar própria solicitação pendente",
        description = "Soft delete da própria participação. Apenas funciona enquanto status for PENDENTE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Solicitação cancelada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Solicitação não está PENDENTE"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Participação não encontrada")
    })
    public ResponseEntity<Void> cancelarSolicitacao(@PathVariable Long jogoId) {
        log.info("Cancelando solicitação no jogo {}", jogoId);
        participanteService.cancelarSolicitacao(jogoId);
        return ResponseEntity.noContent().build();
    }
}
