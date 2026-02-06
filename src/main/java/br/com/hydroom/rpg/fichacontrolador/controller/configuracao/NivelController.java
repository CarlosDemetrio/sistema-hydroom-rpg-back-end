package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateNivelRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateNivelRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.NivelResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.NivelConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.NivelConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/configuracoes/niveis")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Níveis", description = "Gerenciamento de níveis e progressão de XP")
public class NivelController {

    private final NivelConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final NivelConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar níveis de um jogo")
    public ResponseEntity<List<NivelResponse>> listar(@RequestParam Long jogoId) {
        log.info("Listando níveis do jogo: {}", jogoId);
        return ResponseEntity.ok(configuracaoService.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar nível por ID")
    public ResponseEntity<NivelResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar nível (Apenas MESTRE)")
    public ResponseEntity<NivelResponse> criar(@Valid @RequestBody CreateNivelRequest request) {
        log.info("Criando nível {} para jogo ID: {}", request.nivel(), request.jogoId());
        NivelConfig nivel = mapper.toEntity(request);
        nivel.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(nivel)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar nível (Apenas MESTRE)")
    public ResponseEntity<NivelResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateNivelRequest request) {
        NivelConfig nivel = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, nivel);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, nivel)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar nível (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
