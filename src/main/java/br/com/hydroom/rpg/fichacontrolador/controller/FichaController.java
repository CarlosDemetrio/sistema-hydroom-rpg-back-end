package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.ComprarVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederXpRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.DuplicarFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.NpcCreateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ConcederXpResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.DuplicarFichaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaAptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaAtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaAptidaoMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaAtributoMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaVantagemMapper;
import br.com.hydroom.rpg.fichacontrolador.service.FichaPreviewService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaResumoService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaVantagemService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaVidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de Fichas de personagem.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Fichas", description = "Gerenciamento de fichas de personagem")
public class FichaController {

    private final FichaService fichaService;
    private final FichaMapper fichaMapper;
    private final FichaVantagemService fichaVantagemService;
    private final FichaVantagemMapper fichaVantagemMapper;
    private final FichaPreviewService fichaPreviewService;
    private final FichaResumoService fichaResumoService;
    private final FichaAtributoMapper fichaAtributoMapper;
    private final FichaAptidaoMapper fichaAptidaoMapper;
    private final FichaVidaService fichaVidaService;

    @GetMapping("/api/v1/jogos/{jogoId}/fichas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar fichas do jogo", description = "Mestre vê todas as fichas; Jogador vê apenas as suas. Suporta filtros opcionais.")
    public ResponseEntity<List<FichaResponse>> listar(
            @PathVariable Long jogoId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long racaId,
            @RequestParam(required = false) Integer nivel) {
        var fichas = fichaService.listarComFiltros(jogoId, nome, classeId, racaId, nivel);
        var response = fichas.stream().map(fichaMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/jogos/{jogoId}/fichas/minhas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar minhas fichas", description = "Retorna apenas as fichas do usuário atual no jogo")
    public ResponseEntity<List<FichaResponse>> listarMinhas(@PathVariable Long jogoId) {
        var fichas = fichaService.listarMinhas(jogoId);
        var response = fichas.stream().map(fichaMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/jogos/{jogoId}/fichas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Criar ficha", description = "Cria uma nova ficha com inicialização automática de sub-registros")
    public ResponseEntity<FichaResponse> criar(
            @PathVariable Long jogoId,
            @Valid @RequestBody CreateFichaRequest request) {
        // Garantir que o jogoId da URL seja usado
        CreateFichaRequest requestComJogo = new CreateFichaRequest(
                jogoId,
                request.nome(),
                request.jogadorId(),
                request.racaId(),
                request.classeId(),
                request.generoId(),
                request.indoleId(),
                request.presencaId(),
                request.isNpc()
        );
        var ficha = fichaService.criar(requestComJogo);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/fichas/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar ficha por ID")
    public ResponseEntity<FichaResponse> buscarPorId(@PathVariable Long id) {
        var ficha = fichaService.buscarPorId(id);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/v1/fichas/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar ficha", description = "Mestre pode editar qualquer ficha; Jogador só edita as próprias")
    public ResponseEntity<FichaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFichaRequest request) {
        var ficha = fichaService.atualizar(id, request);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/v1/fichas/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar ficha (Apenas MESTRE)", description = "Soft delete da ficha")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        fichaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/jogos/{jogoId}/npcs")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Listar NPCs do jogo (Apenas MESTRE)")
    public ResponseEntity<List<FichaResponse>> listarNpcs(@PathVariable Long jogoId) {
        var fichas = fichaService.listarNpcs(jogoId);
        var response = fichas.stream().map(fichaMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/jogos/{jogoId}/npcs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar NPC (Apenas MESTRE)", description = "Cria uma ficha de NPC com isNpc=true e sem jogadorId")
    public FichaResponse criarNpc(
            @PathVariable Long jogoId,
            @Valid @RequestBody NpcCreateRequest request) {
        CreateFichaRequest createRequest = new CreateFichaRequest(
                jogoId,
                request.nome(),
                null,
                request.racaId(),
                request.classeId(),
                request.generoId(),
                request.indoleId(),
                request.presencaId(),
                true
        );
        var ficha = fichaService.criar(createRequest);
        if (request.descricao() != null) {
            ficha = fichaService.atualizarDescricao(ficha.getId(), request.descricao());
        }
        return fichaMapper.toResponse(ficha);
    }

    @PostMapping("/api/v1/fichas/{id}/duplicar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Duplicar ficha", description = "Cria uma cópia da ficha com novo nome. Mestre pode duplicar qualquer ficha; Jogador só as próprias.")
    public DuplicarFichaResponse duplicar(
            @PathVariable Long id,
            @Valid @RequestBody DuplicarFichaRequest request) {
        var ficha = fichaService.duplicar(id, request.novoNome(), request.manterJogador());
        return new DuplicarFichaResponse(ficha.getId(), ficha.getNome(), ficha.isNpc());
    }

    // ==================== RESUMO ====================

    @GetMapping("/api/v1/fichas/{id}/resumo")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Resumo calculado da ficha", description = "Retorna valores calculados agregados da ficha: atributos, bônus, vida, essência e ameaça")
    public ResponseEntity<FichaResumoResponse> getResumo(@PathVariable Long id) {
        var resumo = fichaResumoService.getResumo(id);
        return ResponseEntity.ok(resumo);
    }

    // ==================== PREVIEW ====================

    @PostMapping("/api/v1/fichas/{id}/preview")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Preview de cálculos sem persistir",
            description = "Simula mudanças de atributos/XP e retorna valores recalculados sem salvar")
    public ResponseEntity<FichaPreviewResponse> preview(
            @PathVariable Long id,
            @Valid @RequestBody FichaPreviewRequest request) {
        var result = fichaPreviewService.simular(id, request);
        return ResponseEntity.ok(result);
    }

    // ==================== VANTAGENS ====================

    @GetMapping("/api/v1/fichas/{id}/vantagens")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar vantagens da ficha")
    public ResponseEntity<List<FichaVantagemResponse>> listarVantagens(@PathVariable Long id) {
        var vantagens = fichaVantagemService.listar(id);
        var response = vantagens.stream().map(fichaVantagemMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/fichas/{id}/vantagens")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Comprar vantagem para a ficha", description = "Verifica pré-requisitos e compra a vantagem no nível 1")
    public ResponseEntity<FichaVantagemResponse> comprarVantagem(
            @PathVariable Long id,
            @Valid @RequestBody ComprarVantagemRequest request) {
        var fichaVantagem = fichaVantagemService.comprar(id, request.vantagemConfigId());
        var response = fichaVantagemMapper.toResponse(fichaVantagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/v1/fichas/{id}/vantagens/{vid}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Aumentar nível de vantagem", description = "Incrementa o nível da vantagem (não pode exceder nivelMaximo)")
    public ResponseEntity<FichaVantagemResponse> aumentarNivelVantagem(
            @PathVariable Long id,
            @PathVariable Long vid) {
        var fichaVantagem = fichaVantagemService.aumentarNivel(id, vid);
        var response = fichaVantagemMapper.toResponse(fichaVantagem);
        return ResponseEntity.ok(response);
    }

    // ==================== ATRIBUTOS ====================

    @GetMapping("/api/v1/fichas/{id}/atributos")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar atributos da ficha",
               description = "Retorna todos os atributos da ficha ordenados por ordemExibicao. Mestre acessa qualquer ficha; Jogador acessa apenas as próprias.")
    public ResponseEntity<List<FichaAtributoResponse>> listarAtributos(@PathVariable Long id) {
        var atributos = fichaService.listarAtributos(id);
        var response = atributos.stream().map(fichaAtributoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/v1/fichas/{id}/atributos")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar atributos da ficha em lote",
               description = "Mestre pode editar qualquer ficha; Jogador só as próprias. Valida que base não excede o limitador do nível.")
    public ResponseEntity<List<FichaAtributoResponse>> atualizarAtributos(
            @PathVariable Long id,
            @Valid @RequestBody List<AtualizarAtributoRequest> requests) {
        var atributos = fichaService.atualizarAtributos(id, requests);
        var response = atributos.stream().map(fichaAtributoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    // ==================== APTIDOES ====================

    @GetMapping("/api/v1/fichas/{id}/aptidoes")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar aptidões da ficha",
               description = "Retorna todas as aptidões da ficha ordenadas por ordemExibicao. Mestre acessa qualquer ficha; Jogador acessa apenas as próprias.")
    public ResponseEntity<List<FichaAptidaoResponse>> listarAptidoes(@PathVariable Long id) {
        var aptidoes = fichaService.listarAptidoes(id);
        var response = aptidoes.stream().map(fichaAptidaoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/v1/fichas/{id}/aptidoes")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar aptidões da ficha em lote",
               description = "Mestre pode editar qualquer ficha; Jogador só as próprias. Recalcula valores derivados após salvar.")
    public ResponseEntity<List<FichaAptidaoResponse>> atualizarAptidoes(
            @PathVariable Long id,
            @Valid @RequestBody List<AtualizarAptidaoRequest> requests) {
        var aptidoes = fichaService.atualizarAptidoes(id, requests);
        var response = aptidoes.stream().map(fichaAptidaoMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    // ==================== VIDA (ESTADO DE COMBATE) ====================

    @PutMapping("/api/v1/fichas/{id}/vida")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar estado de vida da ficha",
               description = "Atualiza vida atual, essência atual e dano nos membros. Mestre pode editar qualquer ficha; Jogador só as próprias. Não recalcula atributos derivados.")
    public ResponseEntity<FichaResumoResponse> atualizarVida(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarVidaRequest request) {
        fichaVidaService.atualizarVida(id, request);
        var resumo = fichaResumoService.getResumo(id);
        return ResponseEntity.ok(resumo);
    }

    // ==================== PROSPECÇÃO ====================

    @PutMapping("/api/v1/fichas/{id}/prospeccao")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Atualizar dado de prospecção da ficha",
               description = "Atualiza a quantidade de um dado de prospecção específico. Mestre pode editar qualquer ficha; Jogador só as próprias.")
    public ResponseEntity<FichaResumoResponse> atualizarProspeccao(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarProspeccaoRequest request) {
        fichaVidaService.atualizarProspeccao(id, request);
        var resumo = fichaResumoService.getResumo(id);
        return ResponseEntity.ok(resumo);
    }

    // ==================== XP ====================

    @PutMapping("/api/v1/fichas/{id}/xp")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Conceder XP à ficha (Apenas MESTRE)",
               description = "Define o XP total da ficha e recalcula o nível automaticamente. Retorna levelUp=true se o nível aumentou.")
    public ResponseEntity<ConcederXpResponse> concederXp(
            @PathVariable Long id,
            @Valid @RequestBody ConcederXpRequest request) {
        var response = fichaService.concederXp(id, request.xp());
        return ResponseEntity.ok(response);
    }
}
