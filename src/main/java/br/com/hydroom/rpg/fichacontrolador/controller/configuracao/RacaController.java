package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaBonusAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaClassePermitidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateRacaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaBonusAtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaClassePermitidaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.RacaResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.RacaMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarRequest;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.ReordenacaoService;
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
    private final ReordenacaoService reordenacaoService;

    @PutMapping("/reordenar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reordenar raças (Apenas MESTRE)", description = "Atualiza a ordem de exibição de múltiplos itens em batch")
    public ResponseEntity<Void> reordenar(
            @RequestParam Long jogoId,
            @Valid @RequestBody ReordenarRequest request) {
        reordenacaoService.reordenarRacas(jogoId, request.itens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar raças de um jogo")
    public ResponseEntity<List<RacaResponse>> listar(
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nome) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId, nome).stream().map(mapper::toResponse).toList());
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

    // ===== ENDPOINTS DE BÔNUS DE ATRIBUTO =====

    @GetMapping("/{id}/bonus-atributos")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar bônus de atributo de uma raça")
    public ResponseEntity<List<RacaBonusAtributoResponse>> listarBonusAtributo(@PathVariable Long id) {
        return ResponseEntity.ok(
            configuracaoService.listarBonusAtributo(id).stream().map(mapper::toBonusAtributoResponse).toList()
        );
    }

    @PostMapping("/{id}/bonus-atributos")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar bônus de atributo a uma raça (Apenas MESTRE)")
    public ResponseEntity<RacaBonusAtributoResponse> adicionarBonusAtributo(
            @PathVariable Long id,
            @Valid @RequestBody RacaBonusAtributoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapper.toBonusAtributoResponse(
                configuracaoService.adicionarBonusAtributo(id, request.atributoId(), request.bonus())
            )
        );
    }

    @DeleteMapping("/{id}/bonus-atributos/{bonusAtributoId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover bônus de atributo de uma raça (Apenas MESTRE)")
    public ResponseEntity<Void> removerBonusAtributo(@PathVariable Long id, @PathVariable Long bonusAtributoId) {
        configuracaoService.removerBonusAtributo(id, bonusAtributoId);
        return ResponseEntity.noContent().build();
    }

    // ===== ENDPOINTS DE CLASSES PERMITIDAS =====

    @GetMapping("/{id}/classes-permitidas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar classes permitidas para uma raça")
    public ResponseEntity<List<RacaClassePermitidaResponse>> listarClassesPermitidas(@PathVariable Long id) {
        return ResponseEntity.ok(
            configuracaoService.listarClassesPermitidas(id).stream().map(mapper::toClassePermitidaResponse).toList()
        );
    }

    @PostMapping("/{id}/classes-permitidas")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Permitir classe para uma raça (Apenas MESTRE)")
    public ResponseEntity<RacaClassePermitidaResponse> permitirClasse(
            @PathVariable Long id,
            @Valid @RequestBody RacaClassePermitidaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapper.toClassePermitidaResponse(
                configuracaoService.permitirClasse(id, request.classeId())
            )
        );
    }

    @DeleteMapping("/{id}/classes-permitidas/{classePermitidaId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover classe permitida de uma raça (Apenas MESTRE)")
    public ResponseEntity<Void> removerClassePermitida(@PathVariable Long id, @PathVariable Long classePermitidaId) {
        configuracaoService.removerClassePermitida(id, classePermitidaId);
        return ResponseEntity.noContent().build();
    }
}
