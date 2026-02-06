package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateGeneroRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateGeneroRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.GeneroResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.GeneroConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.GeneroConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
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
@RequestMapping("/api/v1/configuracoes/generos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Gêneros", description = "Gerenciamento de gêneros disponíveis")
public class GeneroController {

    private final GeneroConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final GeneroConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar gêneros de um jogo")
    public ResponseEntity<List<GeneroResponse>> listar(@RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar gênero por ID")
    public ResponseEntity<GeneroResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar gênero (Apenas MESTRE)")
    public ResponseEntity<GeneroResponse> criar(@Valid @RequestBody CreateGeneroRequest request) {
        GeneroConfig genero = mapper.toEntity(request);
        genero.setJogo(jogoService.buscarJogo(request.jogoId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(genero)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar gênero (Apenas MESTRE)")
    public ResponseEntity<GeneroResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateGeneroRequest request) {
        GeneroConfig genero = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, genero);
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, genero)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar gênero (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
