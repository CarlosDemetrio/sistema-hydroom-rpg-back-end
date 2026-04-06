package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVisibilidadeGlobalRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVisibilidadeRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.ComprarVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederXpRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.DuplicarFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.FichaPreviewRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.NpcCreateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UsarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.DuplicarFichaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaAptidaoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaAtributoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaPreviewResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaVisibilidadeResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ProspeccaoUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaAptidaoMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaAtributoMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.FichaVantagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.service.FichaPreviewService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaResumoService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaVantagemService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaVidaService;
import br.com.hydroom.rpg.fichacontrolador.service.FichaVisibilidadeService;
import br.com.hydroom.rpg.fichacontrolador.service.ProspeccaoService;
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
    private final FichaVisibilidadeService fichaVisibilidadeService;
    private final ProspeccaoService prospeccaoService;

    @GetMapping("/api/v1/jogos/{jogoId}/fichas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar fichas do jogo", description = "Mestre vê todas as fichas; Jogador vê as suas e NPCs com visivelGlobalmente=true. Suporta filtros opcionais.")
    public ResponseEntity<List<FichaResponse>> listar(
            @PathVariable Long jogoId,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long racaId,
            @RequestParam(required = false) Integer nivel) {
        var fichas = fichaService.listarComFiltros(jogoId, nome, classeId, racaId, nivel);
        var response = fichas.stream()
                .map(ficha -> enriquecerComVisibilidade(ficha))
                .toList();
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
        if (Boolean.TRUE.equals(request.visivelGlobalmente())) {
            ficha = fichaService.atualizarVisivelGlobalmente(ficha.getId(), true);
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

    // ==================== INSOLITUS ====================

    @PostMapping("/api/v1/fichas/{id}/vantagens/insolitus/{vantagemConfigId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Conceder Insolitus (Apenas MESTRE)",
               description = "Concede uma vantagem do tipo INSOLITUS sem custo de pontos. Apenas o Mestre pode conceder.")
    public ResponseEntity<FichaVantagemResponse> concederInsolitus(
            @PathVariable Long id,
            @PathVariable Long vantagemConfigId) {
        var fichaVantagem = fichaVantagemService.concederInsolitus(id, vantagemConfigId);
        var response = fichaVantagemMapper.toResponse(fichaVantagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/v1/fichas/{id}/vantagens/{vid}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Revogar vantagem da ficha (Apenas MESTRE)",
               description = "Revoga qualquer vantagem (incluindo Insolitus) da ficha. Soft delete.")
    public ResponseEntity<Void> revogarVantagem(
            @PathVariable Long id,
            @PathVariable Long vid) {
        fichaVantagemService.revogarVantagem(id, vid);
        return ResponseEntity.noContent().build();
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

    // ==================== PROSPECÇÃO (SEMÂNTICA) ====================

    @PostMapping("/api/v1/fichas/{id}/prospeccao/conceder")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Conceder dados de prospecção (Apenas MESTRE)",
               description = "Incrementa a quantidade de um dado de prospecção na ficha. Cria o registro se não existir.")
    public ResponseEntity<FichaResumoResponse> concederProspeccao(
            @PathVariable Long id,
            @Valid @RequestBody ConcederProspeccaoRequest request) {
        var resumo = prospeccaoService.conceder(id, request);
        return ResponseEntity.ok(resumo);
    }

    @PostMapping("/api/v1/fichas/{id}/prospeccao/usar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar uso de dado de prospecção",
               description = "Decrementa a quantidade e cria registro PENDENTE. Jogador só pode usar dados das próprias fichas.")
    public ProspeccaoUsoResponse usarProspeccao(
            @PathVariable Long id,
            @Valid @RequestBody UsarProspeccaoRequest request) {
        return prospeccaoService.usar(id, request);
    }

    @PatchMapping("/api/v1/fichas/{id}/prospeccao/usos/{usoId}/confirmar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Confirmar uso de prospecção (Apenas MESTRE)",
               description = "Confirma o uso. Apenas usos PENDENTES podem ser confirmados.")
    public ResponseEntity<ProspeccaoUsoResponse> confirmarUso(
            @PathVariable Long id,
            @PathVariable Long usoId) {
        var response = prospeccaoService.confirmar(id, usoId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/v1/fichas/{id}/prospeccao/usos/{usoId}/reverter")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reverter uso de prospecção (Apenas MESTRE)",
               description = "Reverte o uso e restaura a quantidade. Apenas usos PENDENTES podem ser revertidos.")
    public ResponseEntity<ProspeccaoUsoResponse> reverterUso(
            @PathVariable Long id,
            @PathVariable Long usoId) {
        var response = prospeccaoService.reverter(id, usoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/fichas/{id}/prospeccao/usos")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar usos de prospecção da ficha",
               description = "Mestre vê todos os usos; Jogador vê apenas os usos da própria ficha.")
    public ResponseEntity<List<ProspeccaoUsoResponse>> listarUsos(@PathVariable Long id) {
        var usos = prospeccaoService.listarUsos(id);
        return ResponseEntity.ok(usos);
    }

    @GetMapping("/api/v1/jogos/{jogoId}/prospeccao/pendentes")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Listar usos pendentes de prospecção do jogo (Apenas MESTRE)",
               description = "Retorna todos os usos com status PENDENTE do jogo para o painel do Mestre.")
    public ResponseEntity<List<ProspeccaoUsoResponse>> listarPendentesJogo(
            @PathVariable Long jogoId) {
        var pendentes = prospeccaoService.listarPendentesJogo(jogoId);
        return ResponseEntity.ok(pendentes);
    }

    // ==================== STATUS ====================

    @PutMapping("/api/v1/fichas/{id}/completar")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Completar ficha",
               description = "Valida e marca a ficha como COMPLETA. Requer raça, classe, gênero, índole e presença preenchidos. Idempotente.")
    public ResponseEntity<FichaResponse> completar(@PathVariable Long id) {
        var ficha = fichaService.completar(id);
        var response = fichaMapper.toResponse(ficha);
        return ResponseEntity.ok(response);
    }

    // ==================== XP ====================

    @PutMapping("/api/v1/fichas/{id}/xp")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Conceder XP à ficha (Apenas MESTRE)",
               description = "Adiciona XP ao total da ficha (aditivo, nunca substitui) e recalcula o nível automaticamente. " +
                             "Retorna o resumo atualizado com os novos pontos disponíveis.")
    public ResponseEntity<FichaResumoResponse> concederXp(
            @PathVariable Long id,
            @Valid @RequestBody ConcederXpRequest request) {
        var resumo = fichaService.concederXp(id, request);
        return ResponseEntity.ok(resumo);
    }

    // ==================== VISIBILIDADE NPC ====================

    @GetMapping("/api/v1/fichas/{id}/visibilidade")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Listar jogadores com acesso aos stats do NPC (Apenas MESTRE)")
    public ResponseEntity<FichaVisibilidadeResponse> listarVisibilidade(@PathVariable Long id) {
        var visibilidade = fichaVisibilidadeService.listar(id);
        return ResponseEntity.ok(visibilidade);
    }

    @PostMapping("/api/v1/fichas/{id}/visibilidade")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Revelar stats do NPC para jogadores específicos (Apenas MESTRE)",
               description = "Idempotente: revelar para jogador que já tem acesso não duplica o registro.")
    public ResponseEntity<FichaVisibilidadeResponse> atualizarVisibilidade(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarVisibilidadeRequest request) {
        var visibilidade = fichaVisibilidadeService.atualizar(id, request);
        return ResponseEntity.ok(visibilidade);
    }

    @DeleteMapping("/api/v1/fichas/{id}/visibilidade/{jogadorId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Revogar acesso de jogador ao NPC (Apenas MESTRE)")
    public ResponseEntity<Void> revogarVisibilidade(
            @PathVariable Long id,
            @PathVariable Long jogadorId) {
        fichaVisibilidadeService.revogar(id, jogadorId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/v1/fichas/{id}/visibilidade/global")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar visibilidade global do NPC (Apenas MESTRE)",
               description = "Define se o NPC aparece na listagem de fichas para todos os Jogadores.")
    public ResponseEntity<FichaResponse> atualizarVisibilidadeGlobal(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarVisibilidadeGlobalRequest request) {
        var ficha = fichaService.atualizarVisivelGlobalmente(id, request.visivelGlobalmente());
        return ResponseEntity.ok(fichaMapper.toResponse(ficha));
    }

    // ==================== ESTADO RESET ====================

    @PostMapping("/api/v1/fichas/{id}/resetar-estado")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Resetar estado de combate da ficha (Apenas MESTRE)",
        description = "Restaura vidaAtual, essenciaAtual e danoRecebido de todos os membros ao estado base. " +
                      "Não altera atributos, nível, xp ou prospecção. Operação irreversível.")
    public ResponseEntity<FichaResumoResponse> resetarEstado(@PathVariable Long id) {
        fichaVidaService.resetarEstado(id);
        var resumo = fichaResumoService.getResumo(id);
        return ResponseEntity.ok(resumo);
    }

    /**
     * Enriquece o FichaResponse com o campo jogadorTemAcessoStats para NPCs.
     * Para fichas de jogadores, jogadorTemAcessoStats fica null.
     */
    private FichaResponse enriquecerComVisibilidade(Ficha ficha) {
        FichaResponse base = fichaMapper.toResponse(ficha);
        if (!ficha.isNpc()) {
            return base;
        }
        boolean temAcesso = fichaVisibilidadeService.temAcessoUsuarioAtual(ficha.getId());
        return new FichaResponse(
                base.id(), base.jogoId(), base.nome(), base.jogadorId(),
                base.racaId(), base.racaNome(), base.classeId(), base.classeNome(),
                base.generoId(), base.generoNome(), base.indoleId(), base.indoleNome(),
                base.presencaId(), base.presencaNome(), base.nivel(), base.xp(),
                base.renascimentos(), base.isNpc(), base.descricao(), base.status(),
                base.visivelGlobalmente(), temAcesso,
                base.dataCriacao(), base.dataUltimaAtualizacao()
        );
    }
}
