package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateVantagemRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.VantagemResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.VantagemConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.CategoriaVantagemService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.VantagemConfiguracaoService;
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
@RequestMapping("/api/v1/configuracoes/vantagens")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Configurações - Vantagens", description = "Gerenciamento de vantagens compráveis do jogo")
public class VantagemController {

    private final VantagemConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final CategoriaVantagemService categoriaVantagemService;
    private final VantagemConfigMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar vantagens de um jogo")
    public ResponseEntity<List<VantagemResponse>> listar(@RequestParam Long jogoId) {
        return ResponseEntity.ok(configuracaoService.listar(jogoId).stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar vantagem por ID")
    public ResponseEntity<VantagemResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar vantagem (Apenas MESTRE)")
    public ResponseEntity<VantagemResponse> criar(@Valid @RequestBody CreateVantagemRequest request) {
        VantagemConfig vantagem = mapper.toEntity(request);
        vantagem.setJogo(jogoService.buscarJogo(request.jogoId()));
        if (request.categoriaVantagemId() != null) {
            vantagem.setCategoriaVantagem(categoriaVantagemService.buscarPorId(request.categoriaVantagemId()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(configuracaoService.criar(vantagem)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar vantagem (Apenas MESTRE)")
    public ResponseEntity<VantagemResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UpdateVantagemRequest request) {
        VantagemConfig vantagem = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, vantagem);
        if (request.categoriaVantagemId() != null) {
            vantagem.setCategoriaVantagem(categoriaVantagemService.buscarPorId(request.categoriaVantagemId()));
        }
        return ResponseEntity.ok(mapper.toResponse(configuracaoService.atualizar(id, vantagem)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar vantagem (Apenas MESTRE)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        configuracaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
