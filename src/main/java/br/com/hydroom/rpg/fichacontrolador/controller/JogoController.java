package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.DuplicarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.EditarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ConfigImportRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ConfigExportResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.DuplicarJogoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.JogoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.JogoResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.MeuJogoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.JogoMapper;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.service.ConfigExportImportService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoDuplicacaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
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
 * Controller para gerenciamento de Jogos/Campanhas.
 * CRUD completo - Apenas MESTREs podem criar, editar e deletar jogos.
 */
@RestController
@RequestMapping("/api/v1/jogos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Jogos", description = "Gerenciamento de jogos/campanhas de RPG")
public class JogoController {

    private final JogoService jogoService;
    private final JogoMapper mapper;
    private final JogoDuplicacaoService jogoDuplicacaoService;
    private final ConfigExportImportService configExportImportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar jogos do usuário", description = "Retorna todos os jogos onde o usuário é Mestre ou Jogador")
    public ResponseEntity<List<JogoResumoResponse>> listar() {
        var jogos = jogoService.listarJogosDoUsuario();
        var response = jogos.stream().map(mapper::toResumoResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar meus jogos", description = "Retorna todos os jogos do usuário com informações de role e quantidade de personagens")
    public ResponseEntity<List<MeuJogoResponse>> listarMeus() {
        var response = jogoService.listarMeus();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar jogo por ID", description = "Retorna os detalhes de um jogo específico se o usuário tiver acesso")
    public ResponseEntity<JogoResponse> buscarPorId(@PathVariable Long id) {
        var jogo = jogoService.buscarJogo(id);
        var response = mapper.toResponse(jogo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativo")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar jogo ativo", description = "Retorna o jogo atualmente ativo onde o usuário é mestre (404 se não houver)")
    public ResponseEntity<JogoResponse> buscarAtivo() {
        var jogo = jogoService.buscarJogoAtivo();
        var response = mapper.toResponse(jogo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar novo jogo (Apenas MESTRE)", description = "Cria um novo jogo/campanha com o usuário como Mestre")
    public ResponseEntity<JogoResponse> criar(@Valid @RequestBody CriarJogoRequest request) {
        var jogo = jogoService.criarJogo(request);
        var response = mapper.toResponse(jogo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar jogo (Apenas MESTRE)", description = "Atualiza informações do jogo - apenas o Mestre do jogo pode fazer isso")
    public ResponseEntity<JogoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody EditarJogoRequest request) {
        var jogo = jogoService.atualizarJogo(id, request);
        var response = mapper.toResponse(jogo);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar jogo (Apenas MESTRE)", description = "Soft delete - marca o jogo como inativo - apenas o Mestre do jogo pode fazer isso")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        jogoService.deletarJogo(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reativar jogo (Apenas MESTRE)", description = "Reativa um jogo marcado como inativo")
    public ResponseEntity<JogoResponse> ativar(@PathVariable Long id) {
        var jogo = jogoService.ativarJogo(id);
        var response = mapper.toResponse(jogo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/duplicar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Duplicar jogo (Apenas MESTRE)", description = "Cria uma cópia do jogo com todas as configurações, sem fichas nem participantes")
    public ResponseEntity<DuplicarJogoResponse> duplicar(
            @PathVariable Long id,
            @Valid @RequestBody DuplicarJogoRequest request) {
        Jogo novoJogo = jogoDuplicacaoService.duplicar(id, request.novoNome());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DuplicarJogoResponse(novoJogo.getId(), novoJogo.getNome()));
    }

    @GetMapping("/{id}/config/export")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Exportar configurações (Apenas MESTRE)", description = "Exporta todas as 13 configurações do jogo em formato portável")
    public ResponseEntity<ConfigExportResponse> exportarConfig(@PathVariable Long id) {
        return ResponseEntity.ok(configExportImportService.exportar(id));
    }

    @PostMapping("/{id}/config/import")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Importar configurações (Apenas MESTRE)", description = "Importa configurações para o jogo. Itens com nomes já existentes são ignorados.")
    public ResponseEntity<Void> importarConfig(
            @PathVariable Long id,
            @Valid @RequestBody ConfigImportRequest request) {
        configExportImportService.importar(id, request);
        return ResponseEntity.noContent().build();
    }
}
