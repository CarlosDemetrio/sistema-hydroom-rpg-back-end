package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateTipoAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateTipoAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.TipoAptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.TipoAptidaoMapper;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarRequest;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.TipoAptidaoConfiguracaoService;
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
@RequestMapping("/api/v1/configuracoes/tipos-aptidao")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Tipos de Aptidão", description = "Gerenciamento de tipos de aptidão")
public class TipoAptidaoController {

    private final TipoAptidaoConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final TipoAptidaoMapper mapper;
    private final ReordenacaoService reordenacaoService;

    @PutMapping("/reordenar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reordenar tipos de aptidão (Apenas MESTRE)", description = "Atualiza a ordem de exibição de múltiplos itens em batch")
    public ResponseEntity<Void> reordenar(
            @RequestParam Long jogoId,
            @Valid @RequestBody ReordenarRequest request) {
        reordenacaoService.reordenarTiposAptidao(jogoId, request.itens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar tipos de aptidão de um jogo")
    public ResponseEntity<List<TipoAptidaoResponse>> listar(
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nome) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId, nome).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar tipo de aptidão por ID")
    public ResponseEntity<TipoAptidaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar tipo de aptidão (Apenas MESTRE)")
    public ResponseEntity<TipoAptidaoResponse> criar(@Valid @RequestBody CreateTipoAptidaoRequest request) {
        TipoAptidao tipo = mapper.toEntity(request);
        tipo.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(tipo)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar tipo de aptidão (Apenas MESTRE)")
    public ResponseEntity<TipoAptidaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateTipoAptidaoRequest request) {
        TipoAptidao tipo = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, tipo);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, tipo)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar tipo de aptidão (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
