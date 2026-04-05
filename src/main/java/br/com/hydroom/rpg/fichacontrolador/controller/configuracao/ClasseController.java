package br.com.hydroom.rpg.fichacontrolador.controller.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClassePontosConfigRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.CreateClasseRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.UpdateClasseRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClassePontosConfigResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseResponse;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseVantagemPreDefinidaResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ClassePersonagemMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ClassePontosConfigMapper;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ClasseVantagemPreDefinidaMapper;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePontosConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.service.JogoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ClasseConfiguracaoService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ClassePontosConfigService;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.ClasseVantagemPreDefinidaService;
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
    private final ClassePontosConfigService classePontosConfigService;
    private final ClasseVantagemPreDefinidaService classeVantagemPreDefinidaService;
    private final ClassePontosConfigMapper classePontosConfigMapper;
    private final ClasseVantagemPreDefinidaMapper classeVantagemPreDefinidaMapper;

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
            @RequestParam Long jogoId) {

        log.info("Listando classes do jogo: {}", jogoId);
        List<ClassePersonagem> classes = configuracaoService.listar(jogoId);
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

    // ===== SUB-RECURSO: PONTOS POR NÍVEL =====

    @GetMapping("/{id}/pontos-config")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar configurações de pontos por nível de uma classe")
    public ResponseEntity<List<ClassePontosConfigResponse>> listarPontosConfig(@PathVariable Long id) {
        List<ClassePontosConfig> pontos = classePontosConfigService.listarPorClasse(id);
        return ResponseEntity.ok(classePontosConfigMapper.toResponseList(pontos));
    }

    @PostMapping("/{id}/pontos-config")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Criar configuração de pontos para um nível da classe (Apenas MESTRE)")
    public ResponseEntity<ClassePontosConfigResponse> criarPontosConfig(
            @PathVariable Long id,
            @Valid @RequestBody ClassePontosConfigRequest request) {
        ClassePontosConfig entity = classePontosConfigMapper.toEntity(request);
        ClassePontosConfig salvo = classePontosConfigService.criar(id, entity);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classePontosConfigMapper.toResponse(salvo));
    }

    @PutMapping("/{id}/pontos-config/{pontosConfigId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Atualizar configuração de pontos de uma classe (Apenas MESTRE)")
    public ResponseEntity<ClassePontosConfigResponse> atualizarPontosConfig(
            @PathVariable Long id,
            @PathVariable Long pontosConfigId,
            @Valid @RequestBody ClassePontosConfigRequest request) {
        ClassePontosConfig entity = classePontosConfigMapper.toEntity(request);
        ClassePontosConfig atualizado = classePontosConfigService.atualizar(id, pontosConfigId, entity);
        return ResponseEntity.ok(classePontosConfigMapper.toResponse(atualizado));
    }

    @DeleteMapping("/{id}/pontos-config/{pontosConfigId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Deletar configuração de pontos de uma classe (Apenas MESTRE)")
    public ResponseEntity<Void> deletarPontosConfig(
            @PathVariable Long id,
            @PathVariable Long pontosConfigId) {
        classePontosConfigService.deletar(id, pontosConfigId);
        return ResponseEntity.noContent().build();
    }

    // ===== SUB-RECURSO: VANTAGENS PRÉ-DEFINIDAS =====

    @GetMapping("/{id}/vantagens-predefinidas")
    @PreAuthorize("hasAnyRole('MESTRE', 'JOGADOR')")
    @Operation(summary = "Listar vantagens pré-definidas de uma classe")
    public ResponseEntity<List<ClasseVantagemPreDefinidaResponse>> listarVantagensPreDefinidas(
            @PathVariable Long id) {
        List<ClasseVantagemPreDefinida> vantagens = classeVantagemPreDefinidaService.listarPorClasse(id);
        return ResponseEntity.ok(classeVantagemPreDefinidaMapper.toResponseList(vantagens));
    }

    @PostMapping("/{id}/vantagens-predefinidas")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Adicionar vantagem pré-definida a uma classe (Apenas MESTRE)")
    public ResponseEntity<ClasseVantagemPreDefinidaResponse> criarVantagemPreDefinida(
            @PathVariable Long id,
            @Valid @RequestBody ClasseVantagemPreDefinidaRequest request) {
        ClasseVantagemPreDefinida salvo = classeVantagemPreDefinidaService.criar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(classeVantagemPreDefinidaMapper.toResponse(salvo));
    }

    @DeleteMapping("/{id}/vantagens-predefinidas/{predefinidaId}")
    @PreAuthorize("hasRole('MESTRE')")
    @Operation(summary = "Remover vantagem pré-definida de uma classe (Apenas MESTRE)")
    public ResponseEntity<Void> deletarVantagemPreDefinida(
            @PathVariable Long id,
            @PathVariable Long predefinidaId) {
        classeVantagemPreDefinidaService.deletar(id, predefinidaId);
        return ResponseEntity.noContent().build();
    }
}
