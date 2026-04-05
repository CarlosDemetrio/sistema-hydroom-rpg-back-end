package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateDadoProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateDadoProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.DadoProspeccaoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.DadoProspeccaoConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarRequest;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.DadoProspeccaoConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.ReordenacaoService;
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
@RequestMapping("/api/v1/configuracoes/dados-prospeccao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Dados de Prospecção", description = "Gerenciamento de dados para prospecção")
public class DadoProspeccaoController {

    private final DadoProspeccaoConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final DadoProspeccaoConfigMapper mapper;
    private final ReordenacaoService reordenacaoService;

    @PutMapping("/reordenar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reordenar dados de prospecção (Apenas MESTRE)", description = "Atualiza a ordem de exibição de múltiplos itens em batch")
    public ResponseEntity<Void> reordenar(
            @RequestParam Long jogoId,
            @Valid @RequestBody ReordenarRequest request) {
        reordenacaoService.reordenarDadosProspeccao(jogoId, request.itens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar dados de prospecção de um jogo")
    public ResponseEntity<List<DadoProspeccaoResponse>> listar(
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nome) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId, nome).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar dado de prospecção por ID")
    public ResponseEntity<DadoProspeccaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar dado de prospecção (Apenas MESTRE)")
    public ResponseEntity<DadoProspeccaoResponse> criar(@Valid @RequestBody CreateDadoProspeccaoRequest request) {
        DadoProspeccaoConfig dado = mapper.toEntity(request);
        dado.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(dado)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar dado de prospecção (Apenas MESTRE)")
    public ResponseEntity<DadoProspeccaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateDadoProspeccaoRequest request) {
        DadoProspeccaoConfig dado = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, dado);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, dado)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar dado de prospecção (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
