package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaPontosConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaPontosConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaVantagemPreDefinidaResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.RacaMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.RacaPontosConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.RacaVantagemPreDefinidaMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RacaPontosConfig;
import br.com.hydroom.rpg.fichacontrolador.model.RacaVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RacaConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RacaPontosConfigService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RacaVantagemPreDefinidaService;
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
    private final RacaPontosConfigService racaPontosConfigService;
    private final RacaVantagemPreDefinidaService racaVantagemPreDefinidaService;
    private final RacaPontosConfigMapper racaPontosConfigMapper;
    private final RacaVantagemPreDefinidaMapper racaVantagemPreDefinidaMapper;

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

    // ===== SUB-RECURSO: PONTOS POR NÍVEL =====

    @GetMapping("/{id}/pontos-config")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar configurações de pontos por nível de uma raça")
    public ResponseEntity<List<RacaPontosConfigResponse>> listarPontosConfig(@PathVariable Long id) {
        List<RacaPontosConfig> pontos = racaPontosConfigService.listarPorRaca(id);
        return ResponseEntity.ok(racaPontosConfigMapper.toResponseList(pontos));
    }

    @PostMapping("/{id}/pontos-config")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar configuração de pontos para um nível da raça (Apenas MESTRE)")
    public ResponseEntity<RacaPontosConfigResponse> criarPontosConfig(
            @PathVariable Long id,
            @Valid @RequestBody RacaPontosConfigRequest request) {
        RacaPontosConfig entity = racaPontosConfigMapper.toEntity(request);
        RacaPontosConfig salvo = racaPontosConfigService.criar(id, entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(racaPontosConfigMapper.toResponse(salvo));
    }

    @PutMapping("/{id}/pontos-config/{pontosConfigId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar configuração de pontos de uma raça (Apenas MESTRE)")
    public ResponseEntity<RacaPontosConfigResponse> atualizarPontosConfig(
            @PathVariable Long id,
            @PathVariable Long pontosConfigId,
            @Valid @RequestBody RacaPontosConfigRequest request) {
        RacaPontosConfig entity = racaPontosConfigMapper.toEntity(request);
        RacaPontosConfig atualizado = racaPontosConfigService.atualizar(id, pontosConfigId, entity);
        return ResponseEntity.ok(racaPontosConfigMapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}/pontos-config/{pontosConfigId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar configuração de pontos de uma raça (Apenas MESTRE)")
    public ResponseEntity<Void> deletarPontosConfig(
            @PathVariable Long id,
            @PathVariable Long pontosConfigId) {
        racaPontosConfigService.deletar(id, pontosConfigId);
        return ResponseEntity.noContent().build();
    }

    // ===== SUB-RECURSO: VANTAGENS PRÉ-DEFINIDAS =====

    @GetMapping("/{id}/vantagens-predefinidas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar vantagens pré-definidas de uma raça")
    public ResponseEntity<List<RacaVantagemPreDefinidaResponse>> listarVantagensPreDefinidas(
            @PathVariable Long id) {
        List<RacaVantagemPreDefinida> vantagens = racaVantagemPreDefinidaService.listarPorRaca(id);
        return ResponseEntity.ok(racaVantagemPreDefinidaMapper.toResponseList(vantagens));
    }

    @PostMapping("/{id}/vantagens-predefinidas")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar vantagem pré-definida a uma raça (Apenas MESTRE)")
    public ResponseEntity<RacaVantagemPreDefinidaResponse> criarVantagemPreDefinida(
            @PathVariable Long id,
            @Valid @RequestBody RacaVantagemPreDefinidaRequest request) {
        RacaVantagemPreDefinida salvo = racaVantagemPreDefinidaService.criar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(racaVantagemPreDefinidaMapper.toResponse(salvo));
    }

    @DeleteMapping("/{id}/vantagens-predefinidas/{predefinidaId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover vantagem pré-definida de uma raça (Apenas MESTRE)")
    public ResponseEntity<Void> deletarVantagemPreDefinida(
            @PathVariable Long id,
            @PathVariable Long predefinidaId) {
        racaVantagemPreDefinidaService.deletar(id, predefinidaId);
        return ResponseEntity.noContent().build();
    }
}
