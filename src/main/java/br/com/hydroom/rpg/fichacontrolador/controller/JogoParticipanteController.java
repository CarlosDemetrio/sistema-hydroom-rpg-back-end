package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.response.ParticipanteResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.JogoParticipanteMapper;
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
 * Cobre o fluxo de solicitação, aprovação, rejeição e banimento.
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
        description = "Mestre vê todos os participantes (qualquer status). Jogador vê apenas APROVADOS."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<List<ParticipanteResponse>> listar(
            @Parameter(description = "ID do jogo", required = true) @PathVariable Long jogoId) {
        return ResponseEntity.ok(
            participanteService.listar(jogoId).stream().map(mapper::toResponse).toList()
        );
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

    @DeleteMapping("/{participanteId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Banir participante do jogo (Apenas MESTRE)",
        description = "Marca participante como BANIDO. Não remove o registro histórico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Participante banido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Não é possível banir o próprio Mestre"),
        @ApiResponse(responseCode = "403", description = "Não é o Mestre deste jogo"),
        @ApiResponse(responseCode = "404", description = "Participante não encontrado")
    })
    public ResponseEntity<ParticipanteResponse> banir(
            @PathVariable Long jogoId,
            @PathVariable Long participanteId) {
        log.info("Banindo participante {} do jogo {}", participanteId, jogoId);
        return ResponseEntity.ok(mapper.toResponse(participanteService.banir(jogoId, participanteId)));
    }
}
