package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateMembroCorpoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateMembroCorpoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.MembroCorpoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.MembroCorpoConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarRequest;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.MembroCorpoConfiguracaoService;
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
@RequestMapping("/api/v1/configuracoes/membros-corpo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Membros do Corpo", description = "Gerenciamento de membros do corpo para sistema de vida")
public class MembroCorpoController {

    private final MembroCorpoConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final MembroCorpoConfigMapper mapper;
    private final ReordenacaoService reordenacaoService;

    @PutMapping("/reordenar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reordenar membros do corpo (Apenas MESTRE)", description = "Atualiza a ordem de exibição de múltiplos itens em batch")
    public ResponseEntity<Void> reordenar(
            @RequestParam Long jogoId,
            @Valid @RequestBody ReordenarRequest request) {
        reordenacaoService.reordenarMembrosCorpo(jogoId, request.itens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar membros do corpo de um jogo")
    public ResponseEntity<List<MembroCorpoResponse>> listar(
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nome) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId, nome).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar membro do corpo por ID")
    public ResponseEntity<MembroCorpoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar membro do corpo (Apenas MESTRE)")
    public ResponseEntity<MembroCorpoResponse> criar(@Valid @RequestBody CreateMembroCorpoRequest request) {
        MembroCorpoConfig membro = mapper.toEntity(request);
        membro.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(membro)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar membro do corpo (Apenas MESTRE)")
    public ResponseEntity<MembroCorpoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateMembroCorpoRequest request) {
        MembroCorpoConfig membro = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, membro);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, membro)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar membro do corpo (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
