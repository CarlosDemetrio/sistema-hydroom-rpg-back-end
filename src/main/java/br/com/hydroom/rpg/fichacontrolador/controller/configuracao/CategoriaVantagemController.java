package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateCategoriaVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateCategoriaVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.CategoriaVantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.CategoriaVantagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.CategoriaVantagem;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.CategoriaVantagemService;
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
@RequestMapping("/api/jogos/{jogoId}/config/categorias-vantagem")
@RequiredArgsConstructor
@SecurityRequirement(name = "cookieAuth")
@Tag(name = "Categorias de Vantagem", description = "Gerenciamento de categorias de vantagem do jogo")
public class CategoriaVantagemController {

    private final CategoriaVantagemService service;
    private final JogoService jogoService;
    private final CategoriaVantagemMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar categorias de vantagem do jogo")
    public ResponseEntity<List<CategoriaVantagemResponse>> listar(@PathVariable Long jogoId) {
        return ResponseEntity.ok(service.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar categoria de vantagem por ID")
    public ResponseEntity<CategoriaVantagemResponse> buscar(@PathVariable Long jogoId, @PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(service.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar categoria de vantagem (Apenas MESTRE)")
    public ResponseEntity<CategoriaVantagemResponse> criar(
            @PathVariable Long jogoId,
            @Valid @RequestBody CreateCategoriaVantagemRequest request) {
        CategoriaVantagem categoria = mapper.toEntity(request);
        categoria.setJogo(jogoService.buscarJogo(jogoId));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(service.criar(categoria)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar categoria de vantagem (Apenas MESTRE)")
    public ResponseEntity<CategoriaVantagemResponse> atualizar(
            @PathVariable Long jogoId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoriaVantagemRequest request) {
        CategoriaVantagem categoria = service.buscarPorId(id);
        mapper.updateEntity(request, categoria);
        return ResponseEntity.ok(mapper.toResponse(service.atualizar(id, categoria)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar categoria de vantagem (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long jogoId, @PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
