package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.RacaMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RacaConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/racas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Raças", description = "Gerenciamento de raças do jogo")
public class RacaController {

    private final RacaConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final RacaMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar raças de um jogo")
    public ResponseEntity<List<RacaResponse>> listar(@RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar raça por ID")
    public ResponseEntity<RacaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar raça (Apenas MESTRE)")
    public ResponseEntity<RacaResponse> criar(@Valid @RequestBody CreateRacaRequest request) {
        Raca raca = mapper.toEntity(request);
        raca.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(raca)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar raça (Apenas MESTRE)")
    public ResponseEntity<RacaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateRacaRequest request) {
        Raca raca = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, raca);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, raca)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar raça (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
