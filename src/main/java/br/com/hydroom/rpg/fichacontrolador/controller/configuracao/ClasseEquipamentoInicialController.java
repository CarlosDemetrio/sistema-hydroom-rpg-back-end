package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseEquipamentoInicialRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseEquipamentoInicialUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseEquipamentoInicialResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ClasseEquipamentoInicialMapper;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ClasseEquipamentoInicialService;
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

/**
 * Controller REST para gerenciamento de equipamentos iniciais de classes de personagem.
 *
 * <p>Base path: /api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/classes/{classeId}/equipamentos-iniciais")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Equipamentos Iniciais de Classe", description = "Gerenciamento de equipamentos iniciais das classes de personagem")
public class ClasseEquipamentoInicialController {

    private final ClasseEquipamentoInicialService equipamentoService;
    private final ClasseEquipamentoInicialMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar equipamentos iniciais de uma classe")
    public ResponseEntity<List<ClasseEquipamentoInicialResponse>> listar(@PathVariable Long classeId) {
        log.info("Listando equipamentos iniciais da classe ID: {}", classeId);
        List<ClasseEquipamentoInicialResponse> response = equipamentoService.listar(classeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar equipamento inicial a uma classe (Apenas MESTRE)")
    public ResponseEntity<ClasseEquipamentoInicialResponse> criar(
            @PathVariable Long classeId,
            @Valid @RequestBody ClasseEquipamentoInicialRequest request) {
        log.info("Criando equipamento inicial para classe ID: {}, item ID: {}", classeId, request.itemConfigId());
        ClasseEquipamentoInicialResponse response = mapper.toResponse(
                equipamentoService.criar(classeId, request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar equipamento inicial de uma classe (Apenas MESTRE)")
    public ResponseEntity<ClasseEquipamentoInicialResponse> atualizar(
            @PathVariable Long classeId,
            @PathVariable Long id,
            @Valid @RequestBody ClasseEquipamentoInicialUpdateRequest request) {
        log.info("Atualizando equipamento inicial ID: {} da classe ID: {}", id, classeId);
        ClasseEquipamentoInicialResponse response = mapper.toResponse(
                equipamentoService.atualizar(classeId, id, request)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover equipamento inicial de uma classe (soft delete - Apenas MESTRE)")
    public ResponseEntity<Void> deletar(
            @PathVariable Long classeId,
            @PathVariable Long id) {
        log.info("Deletando equipamento inicial ID: {} da classe ID: {}", id, classeId);
        equipamentoService.deletar(classeId, id);
        return ResponseEntity.noContent().build();
    }
}
