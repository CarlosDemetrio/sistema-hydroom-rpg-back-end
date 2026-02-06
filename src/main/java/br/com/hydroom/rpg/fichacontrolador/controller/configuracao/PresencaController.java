package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreatePresencaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdatePresencaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.PresencaResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.PresencaConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.PresencaConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
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
@RequestMapping("/api/v1/configuracoes/presencas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Presenças", description = "Gerenciamento de presenças disponíveis")
public class PresencaController {

    private final PresencaConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final PresencaConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar presenças de um jogo")
    public ResponseEntity<List<PresencaResponse>> listar(@RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar presença por ID")
    public ResponseEntity<PresencaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar presença (Apenas MESTRE)")
    public ResponseEntity<PresencaResponse> criar(@Valid @RequestBody CreatePresencaRequest request) {
        PresencaConfig presenca = mapper.toEntity(request);
        presenca.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(presenca)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar presença (Apenas MESTRE)")
    public ResponseEntity<PresencaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdatePresencaRequest request) {
        PresencaConfig presenca = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, presenca);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, presenca)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar presença (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
