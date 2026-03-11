package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseAptidaoBonusRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseBonusRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateClasseRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateClasseRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseAptidaoBonusResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseBonusResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ClassePersonagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseAptidaoBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarRequest;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.ReordenacaoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ClasseConfiguracaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * Controller REST para gerenciamento de Classes de Personagem.
 * Coordena requisições HTTP e transformação de dados.
 *
 * @author Carlos Demétrio
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/configuracoes/classes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(
    name = "Configurações - Classes",
    description = "API para gerenciamento de classes do jogo (Guerreiro, Mago, Arqueiro, etc). " +
                  "Apenas MESTRE pode criar/editar/deletar. JOGADOR pode apenas visualizar."
)
public class ClasseController {

    private final ClasseConfiguracaoService configuracaoService;
    private final JogoService jogoService;
    private final ClassePersonagemMapper mapper;
    private final ReordenacaoService reordenacaoService;

    @PutMapping("/reordenar")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Reordenar classes (Apenas MESTRE)", description = "Atualiza a ordem de exibição de múltiplos itens em batch")
    public ResponseEntity<Void> reordenar(
            @RequestParam Long jogoId,
            @Valid @RequestBody ReordenarRequest request) {
        reordenacaoService.reordenarClasses(jogoId, request.itens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(
        summary = "Listar classes de um jogo",
        description = "Retorna todas as classes ativas do jogo especificado"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de classes retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    public ResponseEntity<List<ClasseResponse>> listar(
            @Parameter(description = "ID do jogo", required = true, example = "1")
            @RequestParam Long jogoId,
            @RequestParam(required = false) String nome) {

        log.info("Listando classes do jogo: {}", jogoId);
        List<ClassePersonagem> classes = configuracaoService.listar(jogoId, nome);
        List<ClasseResponse> response = classes.stream()
            .map(mapper::toResponse)
            .toList();

        log.info("Total de classes encontradas: {}", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Buscar classe por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Classe encontrada"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Classe não encontrada")
    })
    public ResponseEntity<ClasseResponse> buscar(
            @Parameter(description = "ID da classe", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Buscando classe ID: {}", id);
        ClassePersonagem classe = configuracaoService.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(classe));
    }

    @PostMapping
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Criar nova classe (Apenas MESTRE)",
        description = "Cria uma nova classe para o jogo. O nome deve ser único dentro do jogo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Classe criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Já existe classe com este nome neste jogo")
    })
    public ResponseEntity<ClasseResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da classe a ser criada",
                required = true
            )
            @Valid @RequestBody CreateClasseRequest request) {

        log.info("Criando classe para jogo ID: {} - Nome: {}", request.jogoId(), request.nome());

        ClassePersonagem classe = mapper.toEntity(request);
        Jogo jogo = jogoService.buscarJogo(request.jogoId());
        classe.setJogo(jogo);

        ClassePersonagem criada = configuracaoService.criar(classe);
        log.info("Classe criada com sucesso. ID: {}, Nome: {}", criada.getId(), criada.getNome());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapper.toResponse(criada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Atualizar classe (Apenas MESTRE)",
        description = "Atualiza uma classe existente. Campos null não serão alterados."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Classe atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Classe não encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflito com nome existente")
    })
    public ResponseEntity<ClasseResponse> atualizar(
            @Parameter(description = "ID da classe", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados da classe a serem atualizados",
                required = true
            )
            @Valid @RequestBody UpdateClasseRequest request) {

        log.info("Atualizando classe ID: {}", id);

        ClassePersonagem classe = configuracaoService.buscarPorId(id);
        mapper.updateEntity(request, classe);

        ClassePersonagem atualizada = configuracaoService.atualizar(id, classe);
        log.info("Classe ID: {} atualizada com sucesso", id);

        return ResponseEntity.ok(mapper.toResponse(atualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(
        summary = "Deletar classe (Apenas MESTRE)",
        description = "Soft delete - marca a classe como inativa, mantendo dados históricos"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Classe deletada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "403", description = "Usuário não tem permissão"),
        @ApiResponse(responseCode = "404", description = "Classe não encontrada")
    })
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID da classe", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Deletando (soft delete) classe ID: {}", id);
        configuracaoService.deletar(id);
        log.info("Classe ID: {} deletada com sucesso", id);

        return ResponseEntity.noContent().build();
    }

    // ===== ENDPOINTS DE BÔNUS =====

    @GetMapping("/{id}/bonus")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar bônus de uma classe")
    public ResponseEntity<List<ClasseBonusResponse>> listarBonus(@PathVariable Long id) {
        return ResponseEntity.ok(
            configuracaoService.listarBonus(id).stream().map(mapper::toBonusResponse).toList()
        );
    }

    @PostMapping("/{id}/bonus")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar bônus a uma classe (Apenas MESTRE)")
    public ResponseEntity<ClasseBonusResponse> adicionarBonus(
            @PathVariable Long id,
            @Valid @RequestBody ClasseBonusRequest request) {
        ClasseBonus cb = configuracaoService.adicionarBonus(id, request.bonusId(), request.valorPorNivel());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toBonusResponse(cb));
    }

    @DeleteMapping("/{id}/bonus/{bonusId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover bônus de uma classe (Apenas MESTRE)")
    public ResponseEntity<Void> removerBonus(@PathVariable Long id, @PathVariable Long bonusId) {
        configuracaoService.removerBonus(id, bonusId);
        return ResponseEntity.noContent().build();
    }

    // ===== ENDPOINTS DE BÔNUS DE APTIDÃO =====

    @GetMapping("/{id}/aptidao-bonus")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar bônus de aptidão de uma classe")
    public ResponseEntity<List<ClasseAptidaoBonusResponse>> listarAptidaoBonus(@PathVariable Long id) {
        return ResponseEntity.ok(
            configuracaoService.listarAptidaoBonus(id).stream().map(mapper::toAptidaoResponse).toList()
        );
    }

    @PostMapping("/{id}/aptidao-bonus")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar bônus de aptidão a uma classe (Apenas MESTRE)")
    public ResponseEntity<ClasseAptidaoBonusResponse> adicionarAptidaoBonus(
            @PathVariable Long id,
            @Valid @RequestBody ClasseAptidaoBonusRequest request) {
        ClasseAptidaoBonus cab = configuracaoService.adicionarAptidaoBonus(id, request.aptidaoId(), request.bonus());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toAptidaoResponse(cab));
    }

    @DeleteMapping("/{id}/aptidao-bonus/{aptidaoBonusId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover bônus de aptidão de uma classe (Apenas MESTRE)")
    public ResponseEntity<Void> removerAptidaoBonus(@PathVariable Long id, @PathVariable Long aptidaoBonusId) {
        configuracaoService.removerAptidaoBonus(id, aptidaoBonusId);
        return ResponseEntity.noContent().build();
    }
}
