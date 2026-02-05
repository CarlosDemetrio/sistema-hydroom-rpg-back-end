package br.com.hydroom.rpg.fichacontrolador.controller;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.EditarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.JogoResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.JogoResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.JogoMapper;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar jogos do usuário", description = "Retorna todos os jogos onde o usuário é Mestre ou Jogador")
    public ResponseEntity<List<JogoResumoResponse>> listar() {
        var jogos = jogoService.listarJogosDoUsuario();
        var response = jogos.stream().map(mapper::toResumoResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar jogo por ID", description = "Retorna detalhes do jogo se o usuário tiver acesso")
    public ResponseEntity<JogoResponse> buscar(@PathVariable Long id) {
        var usuarioId = jogoService.getUsuarioAtualId();
        var jogo = jogoService.buscarJogo(id);
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
}
