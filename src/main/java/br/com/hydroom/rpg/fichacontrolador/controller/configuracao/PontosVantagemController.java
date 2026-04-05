package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreatePontosVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdatePontosVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.PontosVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.PontosVantagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.PontosVantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.PontosVantagemService;
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
@RequestMapping("/api/jogos/{jogoId}/config/pontos-vantagem")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Pontos de Vantagem", description = "Configuração de pontos de vantagem ganhos por nível")
public class PontosVantagemController {

    private final PontosVantagemService service;
    private final JogoService jogoService;
    private final PontosVantagemMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar pontos de vantagem por nível do jogo")
    public ResponseEntity<List<PontosVantagemResponse>> listar(@PathVariable Long jogoId) {
        return ResponseEntity.ok(service.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar configuração de pontos de vantagem por ID")
    public ResponseEntity<PontosVantagemResponse> buscar(@PathVariable Long jogoId, @PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(service.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar configuração de pontos de vantagem (Apenas MESTRE)")
    public ResponseEntity<PontosVantagemResponse> criar(
            @PathVariable Long jogoId,
            @Valid @RequestBody CreatePontosVantagemRequest request) {
        PontosVantagemConfig config = mapper.toEntity(request);
        config.setJogo(jogoService.buscarJogo(jogoId));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.criar(config)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar configuração de pontos de vantagem (Apenas MESTRE)")
    public ResponseEntity<PontosVantagemResponse> atualizar(
            @PathVariable Long jogoId,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePontosVantagemRequest request) {
        PontosVantagemConfig config = service.buscarPorId(id);
        mapper.updateEntity(request, config);
        return ResponseEntity.ok(mapper.toResponse(service.atualizar(id, config)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar configuração de pontos de vantagem (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long jogoId, @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
