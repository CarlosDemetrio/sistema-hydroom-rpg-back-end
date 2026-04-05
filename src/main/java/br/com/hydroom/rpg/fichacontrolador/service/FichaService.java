package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.enums.FichaStatus;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import br.com.hydroom.rpg.fichacontrolador.dto.response.ConcederXpResponse;

/**
 * Service para gerenciamento de Fichas de personagem.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>CRUD de fichas</li>
 *   <li>Inicialização automática de sub-registros ao criar ficha</li>
 *   <li>Controle de acesso: Mestre vê tudo, Jogador vê apenas as próprias fichas</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaService {

    // Repositories de ficha
    private final FichaRepository fichaRepository;
    private final FichaAtributoRepository fichaAtributoRepository;
    private final FichaAptidaoRepository fichaAptidaoRepository;
    private final FichaBonusRepository fichaBonusRepository;
    private final FichaVidaRepository fichaVidaRepository;
    private final FichaVidaMembroRepository fichaVidaMembroRepository;
    private final FichaEssenciaRepository fichaEssenciaRepository;
    private final FichaAmeacaRepository fichaAmeacaRepository;
    private final FichaProspeccaoRepository fichaProspeccaoRepository;
    private final FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;
    private final FichaVantagemRepository fichaVantagemRepository;

    // Repositories de configuração
    private final ConfiguracaoAtributoRepository atributoConfigRepository;
    private final ConfiguracaoAptidaoRepository aptidaoConfigRepository;
    private final BonusConfigRepository bonusConfigRepository;
    private final MembroCorpoConfigRepository membroCorpoConfigRepository;
    private final DadoProspeccaoConfigRepository dadoProspeccaoConfigRepository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final GeneroConfigRepository generoConfigRepository;
    private final IndoleConfigRepository indoleConfigRepository;
    private final PresencaConfigRepository presencaConfigRepository;
    private final ClasseBonusRepository classeBonusRepository;
    private final ClasseAptidaoBonusRepository classeAptidaoBonusRepository;
    private final RacaBonusAtributoRepository racaBonusAtributoRepository;

    // Repositories de configuração de nível
    private final ConfiguracaoNivelRepository nivelConfigRepository;

    // Outros
    private final JogoRepository jogoRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    // Services de cálculo e validação
    private final FichaCalculationService fichaCalculationService;
    private final FichaValidationService fichaValidationService;

    /**
     * Cria uma nova ficha e inicializa todos os sub-registros automaticamente.
     */
    @Transactional
    public Ficha criar(CreateFichaRequest request) {
        Long jogoId = request.jogoId();

        // 1. Buscar jogo
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        // 2. Verificar acesso do usuário
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            // Jogador: verificar participação aprovada
            boolean isAprovado = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndStatus(
                    jogoId, usuarioAtual.getId(), br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante.APROVADO);
            if (!isAprovado) {
                throw new ForbiddenException("Acesso negado: você não é participante aprovado deste jogo.");
            }
        }

        // 3. Jogador não pode criar NPC
        if (!isMestre && Boolean.TRUE.equals(request.isNpc())) {
            throw new ForbiddenException("Apenas o Mestre pode criar NPCs.");
        }

        // 4. Determinar jogadorId
        Long jogadorId = request.jogadorId();
        if (!isMestre) {
            // Jogador só pode criar ficha para si mesmo
            jogadorId = usuarioAtual.getId();
        }

        // 5. Validar FK do mesmo jogo (se fornecidos)
        Raca raca = null;
        if (request.racaId() != null) {
            raca = racaRepository.findById(request.racaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada: " + request.racaId()));
            if (!raca.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Raça não pertence a este jogo.");
            }
        }

        ClassePersonagem classe = null;
        if (request.classeId() != null) {
            classe = classeRepository.findById(request.classeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada: " + request.classeId()));
            if (!classe.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Classe não pertence a este jogo.");
            }
        }

        GeneroConfig genero = null;
        if (request.generoId() != null) {
            genero = generoConfigRepository.findById(request.generoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gênero não encontrado: " + request.generoId()));
            if (!genero.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Gênero não pertence a este jogo.");
            }
        }

        IndoleConfig indole = null;
        if (request.indoleId() != null) {
            indole = indoleConfigRepository.findById(request.indoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Índole não encontrada: " + request.indoleId()));
            if (!indole.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Índole não pertence a este jogo.");
            }
        }

        PresencaConfig presenca = null;
        if (request.presencaId() != null) {
            presenca = presencaConfigRepository.findById(request.presencaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Presença não encontrada: " + request.presencaId()));
            if (!presenca.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Presença não pertence a este jogo.");
            }
        }

        // 6. Criar e salvar a Ficha
        boolean isNpc = Boolean.TRUE.equals(request.isNpc());
        Ficha ficha = Ficha.builder()
                .jogo(jogo)
                .nome(request.nome())
                .jogadorId(isNpc ? null : jogadorId)
                .raca(raca)
                .classe(classe)
                .genero(genero)
                .indole(indole)
                .presenca(presenca)
                .isNpc(isNpc)
                .build();

        ficha = fichaRepository.save(ficha);

        // 7. Inicializar sub-registros
        inicializarSubRegistros(ficha, jogo);

        // 8. Recalcular valores derivados
        recalcular(ficha);

        log.info("Ficha '{}' criada com sucesso (ID: {})", ficha.getNome(), ficha.getId());
        return ficha;
    }

    /**
     * Busca uma ficha por ID, verificando acesso.
     * Usa JOIN FETCH para carregar relacionamentos ManyToOne e evitar N+1 no mapper.
     */
    public Ficha buscarPorId(Long id) {
        Ficha ficha = fichaRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + id));

        verificarAcessoLeitura(ficha);
        return ficha;
    }

    /**
     * Lista fichas de um jogo.
     * Mestre vê todas (incluindo NPCs); Jogador vê apenas as suas.
     */
    public List<Ficha> listar(Long jogoId) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return fichaRepository.findByJogoId(jogoId);
        } else {
            return fichaRepository.findByJogoIdAndJogadorId(jogoId, usuarioAtual.getId());
        }
    }

    /**
     * Lista fichas de um jogo com filtros opcionais.
     * Mestre vê todas (isNpc=false); Jogador vê apenas as suas.
     * Usa JOIN FETCH para carregar relacionamentos ManyToOne e evitar N+1 no mapper.
     */
    public List<Ficha> listarComFiltros(Long jogoId, String nome, Long classeId, Long racaId, Integer nivel) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return fichaRepository.findByJogoIdWithFiltersAndRelationships(jogoId, nome, classeId, racaId, nivel);
        } else {
            return fichaRepository.findByJogoIdAndJogadorIdWithFiltersAndRelationships(
                    jogoId, usuarioAtual.getId(), nome, classeId, racaId, nivel);
        }
    }

    /**
     * Lista fichas do usuário atual em um jogo (apenas jogador, isNpc=false).
     * Usa JOIN FETCH para carregar relacionamentos ManyToOne e evitar N+1 no mapper.
     */
    public List<Ficha> listarMinhas(Long jogoId) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        return fichaRepository.findByJogoIdAndJogadorIdAndIsNpcFalseWithRelationships(jogoId, usuarioAtual.getId());
    }

    /**
     * Lista apenas NPCs de um jogo (apenas Mestre).
     * Usa JOIN FETCH para carregar relacionamentos ManyToOne e evitar N+1 no mapper.
     */
    public List<Ficha> listarNpcs(Long jogoId) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode visualizar NPCs.");
        }

        return fichaRepository.findByJogoIdAndIsNpcTrueWithRelationships(jogoId);
    }

    /**
     * Atualiza dados básicos de uma ficha.
     */
    @Transactional
    public Ficha atualizar(Long id, UpdateFichaRequest request) {
        Ficha ficha = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + id));

        verificarAcessoEscrita(ficha);

        Long jogoId = ficha.getJogo().getId();

        // Atualizar campos simples
        if (request.nome() != null) {
            ficha.setNome(request.nome());
        }
        if (request.xp() != null) {
            ficha.setXp(request.xp());
            // Auto-nível: encontrar nível baseado na XP (mínimo nível 1)
            Optional<NivelConfig> nivelConfig = nivelConfigRepository.findNivelPorExperiencia(jogoId, request.xp());
            if (nivelConfig.isPresent() && nivelConfig.get().getNivel() >= 1) {
                ficha.setNivel(nivelConfig.get().getNivel());
            }
            // Se não encontrou NivelConfig ou nivel é 0, mantém o nível atual (não rebaixa abaixo de 1)
        }
        if (request.renascimentos() != null) {
            ficha.setRenascimentos(request.renascimentos());
        }

        // Atualizar FKs com validação de jogo
        if (request.racaId() != null) {
            Raca raca = racaRepository.findById(request.racaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada: " + request.racaId()));
            if (!raca.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Raça não pertence a este jogo.");
            }
            ficha.setRaca(raca);
        }

        if (request.classeId() != null) {
            ClassePersonagem classe = classeRepository.findById(request.classeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada: " + request.classeId()));
            if (!classe.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Classe não pertence a este jogo.");
            }
            ficha.setClasse(classe);
        }

        if (request.generoId() != null) {
            GeneroConfig genero = generoConfigRepository.findById(request.generoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gênero não encontrado: " + request.generoId()));
            if (!genero.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Gênero não pertence a este jogo.");
            }
            ficha.setGenero(genero);
        }

        if (request.indoleId() != null) {
            IndoleConfig indole = indoleConfigRepository.findById(request.indoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Índole não encontrada: " + request.indoleId()));
            if (!indole.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Índole não pertence a este jogo.");
            }
            ficha.setIndole(indole);
        }

        if (request.presencaId() != null) {
            PresencaConfig presenca = presencaConfigRepository.findById(request.presencaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Presença não encontrada: " + request.presencaId()));
            if (!presenca.getJogo().getId().equals(jogoId)) {
                throw new ForbiddenException("Presença não pertence a este jogo.");
            }
            ficha.setPresenca(presenca);
        }

        Ficha fichaAtualizada = fichaRepository.save(ficha);

        // Validar e recalcular após salvar campos básicos
        validarERecalcular(fichaAtualizada);

        return fichaAtualizada;
    }

    /**
     * Soft delete de uma ficha (apenas Mestre).
     */
    @Transactional
    public void deletar(Long id) {
        Ficha ficha = fichaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + id));

        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                ficha.getJogo().getId(), usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode deletar fichas.");
        }

        ficha.delete();
        fichaRepository.save(ficha);
        log.info("Ficha '{}' (ID: {}) deletada por {}", ficha.getNome(), ficha.getId(), usuarioAtual.getEmail());
    }

    /**
     * Duplica uma ficha existente com um novo nome.
     * Mestre pode duplicar qualquer ficha; Jogador só pode duplicar as próprias.
     * Copia todos os sub-registros com os mesmos valores da ficha original.
     */
    @Transactional
    public Ficha duplicar(Long fichaId, String novoNome, boolean manterJogador) {
        Ficha original = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = original.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        // Jogador não pode duplicar fichas de NPC
        if (!isMestre && original.isNpc()) {
            throw new ForbiddenException("Acesso negado: Jogadores não podem duplicar fichas de NPC.");
        }

        if (!isMestre && !usuarioAtual.getId().equals(original.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para duplicar esta ficha.");
        }

        Long jogadorId = manterJogador ? original.getJogadorId() : null;
        if (!isMestre) {
            // Jogador duplica para si mesmo
            jogadorId = usuarioAtual.getId();
        }

        Ficha copia = Ficha.builder()
                .jogo(original.getJogo())
                .nome(novoNome)
                .jogadorId(original.isNpc() ? null : jogadorId)
                .raca(original.getRaca())
                .classe(original.getClasse())
                .genero(original.getGenero())
                .indole(original.getIndole())
                .presenca(original.getPresenca())
                .nivel(original.getNivel())
                .xp(original.getXp())
                .renascimentos(original.getRenascimentos())
                .isNpc(original.isNpc())
                .build();

        copia = fichaRepository.save(copia);
        log.info("Duplicando ficha '{}' (ID: {}) como '{}' (ID: {})", original.getNome(), fichaId, novoNome, copia.getId());

        copiarSubRegistros(fichaId, copia);
        recalcular(copia);

        return copia;
    }

    /**
     * Atualiza a descrição textual de uma ficha (útil para NPCs).
     */
    @Transactional
    public Ficha atualizarDescricao(Long fichaId, String descricao) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));
        verificarAcessoEscrita(ficha);
        ficha.setDescricao(descricao);
        return fichaRepository.save(ficha);
    }

    /**
     * Marca a ficha como COMPLETA após validar que todos os campos obrigatórios estão preenchidos.
     *
     * <p>Idempotente: se a ficha já estiver COMPLETA, retorna sem erro.</p>
     * <p>Campos obrigatórios: raça, classe, gênero, índole, presença.</p>
     *
     * @throws ValidationException se algum campo obrigatório estiver ausente
     */
    @Transactional
    public Ficha completar(Long fichaId) {
        Ficha ficha = fichaRepository.findByIdWithRelationships(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoEscrita(ficha);

        if (FichaStatus.COMPLETA.equals(ficha.getStatus())) {
            return ficha;
        }

        fichaValidationService.validarCompletude(ficha);

        ficha.setStatus(FichaStatus.COMPLETA);
        return fichaRepository.save(ficha);
    }

    /**
     * Concede XP a uma ficha (Mestre only) e recalcula o nível automaticamente.
     * Retorna flag levelUp=true se o nível aumentou após a concessão.
     */
    @Transactional
    public ConcederXpResponse concederXp(Long fichaId, Long xp) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoEscrita(ficha);

        int nivelAnterior = ficha.getNivel() != null ? ficha.getNivel() : 1;
        ficha.setXp(xp);

        Long jogoId = ficha.getJogo().getId();
        Optional<NivelConfig> nivelAlcancado = nivelConfigRepository.findNivelPorExperiencia(jogoId, xp);
        int novoNivel = nivelAlcancado
                .filter(n -> n.getNivel() >= 1)
                .map(NivelConfig::getNivel)
                .orElse(nivelAnterior);
        ficha.setNivel(novoNivel);

        ficha = fichaRepository.save(ficha);

        boolean levelUp = novoNivel > nivelAnterior;
        if (levelUp) {
            recalcular(ficha);
            log.info("Level up! Ficha '{}' (ID: {}) avançou do nível {} para {}",
                    ficha.getNome(), fichaId, nivelAnterior, novoNivel);
        }

        return new ConcederXpResponse(fichaId, ficha.getXp(), ficha.getNivel(), levelUp);
    }

    /**
     * Atualiza os valores de atributos de uma ficha em lote.
     *
     * <p>Valida que nenhum atributo (base) excede o limitador do nível atual.
     * Recalcula todos os valores derivados após salvar.</p>
     *
     * @param fichaId  ID da ficha
     * @param requests lista de atualizações de atributos
     * @return lista de FichaAtributo atualizada
     */
    @Transactional
    public List<FichaAtributo> atualizarAtributos(Long fichaId, List<AtualizarAtributoRequest> requests) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoEscrita(ficha);

        Long jogoId = ficha.getJogo().getId();

        // Buscar NivelConfig atual para validar limitador
        NivelConfig nivelConfig = nivelConfigRepository
                .findByJogoIdAndNivel(jogoId, ficha.getNivel())
                .orElse(null);

        for (AtualizarAtributoRequest req : requests) {
            FichaAtributo fichaAtributo = fichaAtributoRepository
                    .findByFichaIdAndAtributoConfigId(fichaId, req.atributoConfigId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Atributo de ficha não encontrado para atributoConfigId: " + req.atributoConfigId()));

            // Validar limitador de atributo (campo base não pode exceder limitador)
            if (req.base() != null && nivelConfig != null && nivelConfig.getLimitadorAtributo() != null) {
                if (req.base() > nivelConfig.getLimitadorAtributo()) {
                    String nomeAtributo = fichaAtributo.getAtributoConfig() != null
                            ? fichaAtributo.getAtributoConfig().getNome()
                            : String.valueOf(req.atributoConfigId());
                    throw new ValidationException(
                            "Atributo '" + nomeAtributo + "': valor base " + req.base()
                            + " excede o limitador do nível " + ficha.getNivel()
                            + " (" + nivelConfig.getLimitadorAtributo() + ")");
                }
            }

            if (req.base() != null) fichaAtributo.setBase(req.base());
            if (req.nivel() != null) fichaAtributo.setNivel(req.nivel());
            if (req.outros() != null) fichaAtributo.setOutros(req.outros());
        }

        List<FichaAtributo> todosAtributos = fichaAtributoRepository.findByFichaIdWithConfig(fichaId);
        fichaAtributoRepository.saveAll(todosAtributos);

        recalcular(ficha);

        log.info("Atributos da ficha {} atualizados em lote ({} itens)", fichaId, requests.size());
        return fichaAtributoRepository.findByFichaIdWithConfig(fichaId);
    }

    /**
     * Atualiza os valores de aptidões de uma ficha em lote.
     *
     * <p>Recalcula todos os valores derivados após salvar.</p>
     *
     * @param fichaId  ID da ficha
     * @param requests lista de atualizações de aptidões
     * @return lista de FichaAptidao atualizada
     */
    @Transactional
    public List<FichaAptidao> atualizarAptidoes(Long fichaId, List<AtualizarAptidaoRequest> requests) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoEscrita(ficha);

        for (AtualizarAptidaoRequest req : requests) {
            FichaAptidao fichaAptidao = fichaAptidaoRepository
                    .findByFichaIdAndAptidaoConfigId(fichaId, req.aptidaoConfigId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aptidão de ficha não encontrada para aptidaoConfigId: " + req.aptidaoConfigId()));

            if (req.base() != null) fichaAptidao.setBase(req.base());
            if (req.sorte() != null) fichaAptidao.setSorte(req.sorte());
            if (req.classe() != null) fichaAptidao.setClasse(req.classe());
        }

        List<FichaAptidao> todasAptidoes = fichaAptidaoRepository.findByFichaId(fichaId);
        fichaAptidaoRepository.saveAll(todasAptidoes);

        recalcular(ficha);

        log.info("Aptidões da ficha {} atualizadas em lote ({} itens)", fichaId, requests.size());
        return fichaAptidaoRepository.findByFichaId(fichaId);
    }

    /**
     * Lista os atributos de uma ficha, ordenados por ordemExibicao do AtributoConfig.
     *
     * @param fichaId ID da ficha
     * @return lista de FichaAtributo ordenada
     */
    public List<FichaAtributo> listarAtributos(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoLeitura(ficha);

        return fichaAtributoRepository.findByFichaIdWithConfigOrdenado(fichaId);
    }

    /**
     * Lista as aptidões de uma ficha, ordenadas por ordemExibicao do AptidaoConfig.
     *
     * @param fichaId ID da ficha
     * @return lista de FichaAptidao ordenada
     */
    public List<FichaAptidao> listarAptidoes(Long fichaId) {
        Ficha ficha = fichaRepository.findById(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoLeitura(ficha);

        return fichaAptidaoRepository.findByFichaIdWithConfigOrdenado(fichaId);
    }

    // ==================== PRIVADOS ====================

    /**
     * Inicializa todos os sub-registros de uma nova ficha (tudo zerado).
     */
    private void inicializarSubRegistros(Ficha ficha, Jogo jogo) {
        Long jogoId = jogo.getId();

        // FichaAtributo: 1 por AtributoConfig do jogo
        List<AtributoConfig> atributos = atributoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogoId);
        List<FichaAtributo> fichaAtributos = atributos.stream()
                .map(atributo -> FichaAtributo.builder()
                        .ficha(ficha)
                        .atributoConfig(atributo)
                        .build())
                .toList();
        fichaAtributoRepository.saveAll(fichaAtributos);

        // FichaAptidao: 1 por AptidaoConfig do jogo
        List<AptidaoConfig> aptidoes = aptidaoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogoId);
        List<FichaAptidao> fichaAptidoes = aptidoes.stream()
                .map(aptidao -> FichaAptidao.builder()
                        .ficha(ficha)
                        .aptidaoConfig(aptidao)
                        .build())
                .toList();
        fichaAptidaoRepository.saveAll(fichaAptidoes);

        // FichaBonus: 1 por BonusConfig do jogo
        List<BonusConfig> bonusConfigs = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogoId);
        List<FichaBonus> fichaBonus = bonusConfigs.stream()
                .map(bonus -> FichaBonus.builder()
                        .ficha(ficha)
                        .bonusConfig(bonus)
                        .build())
                .toList();
        fichaBonusRepository.saveAll(fichaBonus);

        // FichaVida: 1 por ficha
        FichaVida fichaVida = FichaVida.builder()
                .ficha(ficha)
                .build();
        fichaVidaRepository.save(fichaVida);

        // FichaVidaMembro: 1 por MembroCorpoConfig do jogo
        List<MembroCorpoConfig> membros = membroCorpoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogoId);
        List<FichaVidaMembro> fichaVidaMembros = membros.stream()
                .map(membro -> FichaVidaMembro.builder()
                        .ficha(ficha)
                        .membroCorpoConfig(membro)
                        .build())
                .toList();
        fichaVidaMembroRepository.saveAll(fichaVidaMembros);

        // FichaEssencia: 1 por ficha
        FichaEssencia fichaEssencia = FichaEssencia.builder()
                .ficha(ficha)
                .build();
        fichaEssenciaRepository.save(fichaEssencia);

        // FichaAmeaca: 1 por ficha
        FichaAmeaca fichaAmeaca = FichaAmeaca.builder()
                .ficha(ficha)
                .build();
        fichaAmeacaRepository.save(fichaAmeaca);

        // FichaProspeccao: 1 por DadoProspeccaoConfig do jogo
        List<DadoProspeccaoConfig> dadosProspeccao = dadoProspeccaoConfigRepository.findByJogoIdOrderByOrdemExibicao(jogoId);
        List<FichaProspeccao> fichaProspeccoes = dadosProspeccao.stream()
                .map(dado -> FichaProspeccao.builder()
                        .ficha(ficha)
                        .dadoProspeccaoConfig(dado)
                        .build())
                .toList();
        fichaProspeccaoRepository.saveAll(fichaProspeccoes);

        // FichaDescricaoFisica: 1 por ficha
        FichaDescricaoFisica descricaoFisica = FichaDescricaoFisica.builder()
                .ficha(ficha)
                .build();
        fichaDescricaoFisicaRepository.save(descricaoFisica);

        log.debug("Sub-registros inicializados para ficha '{}' (ID: {}): {} atributos, {} aptidões, {} bônus, {} membros, {} prospecções",
                ficha.getNome(), ficha.getId(),
                fichaAtributos.size(), fichaAptidoes.size(), fichaBonus.size(),
                fichaVidaMembros.size(), fichaProspeccoes.size());
    }

    /**
     * Copia todos os sub-registros da ficha original para a nova ficha com os mesmos valores.
     * Usa queries com JOIN FETCH para evitar N+1 ao acessar configs durante cópia.
     */
    private void copiarSubRegistros(Long fichaOrigemId, Ficha novaFicha) {
        // FichaAtributo
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaIdWithConfig(fichaOrigemId);
        List<FichaAtributo> copiaAtributos = atributos.stream()
                .map(a -> FichaAtributo.builder()
                        .ficha(novaFicha)
                        .atributoConfig(a.getAtributoConfig())
                        .base(a.getBase())
                        .nivel(a.getNivel())
                        .outros(a.getOutros())
                        .total(a.getTotal())
                        .impeto(a.getImpeto())
                        .build())
                .toList();
        fichaAtributoRepository.saveAll(copiaAtributos);

        // FichaAptidao
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaIdWithConfig(fichaOrigemId);
        List<FichaAptidao> copiaAptidoes = aptidoes.stream()
                .map(a -> FichaAptidao.builder()
                        .ficha(novaFicha)
                        .aptidaoConfig(a.getAptidaoConfig())
                        .base(a.getBase())
                        .sorte(a.getSorte())
                        .classe(a.getClasse())
                        .total(a.getTotal())
                        .build())
                .toList();
        fichaAptidaoRepository.saveAll(copiaAptidoes);

        // FichaBonus
        List<FichaBonus> bonus = fichaBonusRepository.findByFichaIdWithConfig(fichaOrigemId);
        List<FichaBonus> copiaBonus = bonus.stream()
                .map(b -> FichaBonus.builder()
                        .ficha(novaFicha)
                        .bonusConfig(b.getBonusConfig())
                        .base(b.getBase())
                        .vantagens(b.getVantagens())
                        .classe(b.getClasse())
                        .itens(b.getItens())
                        .gloria(b.getGloria())
                        .outros(b.getOutros())
                        .total(b.getTotal())
                        .build())
                .toList();
        fichaBonusRepository.saveAll(copiaBonus);

        // FichaVida
        fichaVidaRepository.findByFichaId(fichaOrigemId).ifPresent(v -> {
            FichaVida copiaVida = FichaVida.builder()
                    .ficha(novaFicha)
                    .vt(v.getVt())
                    .outros(v.getOutros())
                    .vidaTotal(v.getVidaTotal())
                    .build();
            fichaVidaRepository.save(copiaVida);
        });

        // FichaVidaMembro
        List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaIdWithConfig(fichaOrigemId);
        List<FichaVidaMembro> copiaMembros = membros.stream()
                .map(m -> FichaVidaMembro.builder()
                        .ficha(novaFicha)
                        .membroCorpoConfig(m.getMembroCorpoConfig())
                        .vida(m.getVida())
                        .danoRecebido(0)
                        .build())
                .toList();
        fichaVidaMembroRepository.saveAll(copiaMembros);

        // FichaEssencia
        fichaEssenciaRepository.findByFichaId(fichaOrigemId).ifPresent(e -> {
            FichaEssencia copiaEssencia = FichaEssencia.builder()
                    .ficha(novaFicha)
                    .renascimentos(e.getRenascimentos())
                    .vantagens(e.getVantagens())
                    .outros(e.getOutros())
                    .total(e.getTotal())
                    .build();
            fichaEssenciaRepository.save(copiaEssencia);
        });

        // FichaAmeaca
        fichaAmeacaRepository.findByFichaId(fichaOrigemId).ifPresent(a -> {
            FichaAmeaca copiaAmeaca = FichaAmeaca.builder()
                    .ficha(novaFicha)
                    .itens(a.getItens())
                    .titulos(a.getTitulos())
                    .renascimentos(a.getRenascimentos())
                    .outros(a.getOutros())
                    .total(a.getTotal())
                    .build();
            fichaAmeacaRepository.save(copiaAmeaca);
        });

        // FichaProspeccao
        List<FichaProspeccao> prospeccoes = fichaProspeccaoRepository.findByFichaIdWithConfig(fichaOrigemId);
        List<FichaProspeccao> copiaProspeccoes = prospeccoes.stream()
                .map(p -> FichaProspeccao.builder()
                        .ficha(novaFicha)
                        .dadoProspeccaoConfig(p.getDadoProspeccaoConfig())
                        .quantidade(p.getQuantidade())
                        .build())
                .toList();
        fichaProspeccaoRepository.saveAll(copiaProspeccoes);

        // FichaVantagem — copia vantagens com os mesmos níveis e custos
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaIdWithConfig(fichaOrigemId);
        List<FichaVantagem> copiaVantagens = vantagens.stream()
                .map(fv -> FichaVantagem.builder()
                        .ficha(novaFicha)
                        .vantagemConfig(fv.getVantagemConfig())
                        .nivelAtual(fv.getNivelAtual())
                        .custoPago(fv.getCustoPago())
                        .build())
                .toList();
        fichaVantagemRepository.saveAll(copiaVantagens);

        // FichaDescricaoFisica — nova ficha começa com descrição em branco
        FichaDescricaoFisica descricaoFisica = FichaDescricaoFisica.builder()
                .ficha(novaFicha)
                .build();
        fichaDescricaoFisicaRepository.save(descricaoFisica);

        log.debug("Sub-registros copiados para ficha '{}' (ID: {}): {} atributos, {} aptidões, {} bônus, {} membros, {} prospecções, {} vantagens",
                novaFicha.getNome(), novaFicha.getId(),
                copiaAtributos.size(), copiaAptidoes.size(), copiaBonus.size(),
                copiaMembros.size(), copiaProspeccoes.size(), copiaVantagens.size());
    }

    /**
     * Verifica se o usuário atual pode LER a ficha.
     * NPCs só podem ser lidos pelo Mestre.
     */
    private void verificarAcessoLeitura(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return; // Mestre vê tudo
        }

        // NPCs só são visíveis para o Mestre
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só são acessíveis pelo Mestre.");
        }

        // Jogador só vê suas próprias fichas
        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }
    }

    /**
     * Verifica se o usuário atual pode ESCREVER na ficha.
     * NPCs só podem ser editados pelo Mestre.
     */
    private void verificarAcessoEscrita(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return; // Mestre pode editar qualquer ficha
        }

        // NPCs só podem ser editados pelo Mestre
        if (ficha.isNpc()) {
            throw new ForbiddenException("Acesso negado: NPCs só podem ser editados pelo Mestre.");
        }

        // Jogador só edita suas próprias fichas
        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você só pode editar suas próprias fichas.");
        }
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }

    /**
     * Valida as regras de negócio e recalcula os valores derivados da ficha.
     * Tolerante a configurações ausentes (nível, atributos etc.).
     * Usa queries com JOIN FETCH para evitar N+1 ao acessar configs durante validação.
     */
    private void validarERecalcular(Ficha ficha) {
        Long fichaId = ficha.getId();
        Long jogoId = ficha.getJogo().getId();

        // Carregar sub-registros (JOIN FETCH para evitar N+1)
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaIdWithConfig(fichaId);
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaId(fichaId);
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaIdWithConfig(fichaId);
        List<FichaBonus> bonus = fichaBonusRepository.findByFichaIdWithConfig(fichaId);

        // NivelConfig atual (null se não configurado)
        NivelConfig nivelConfig = nivelConfigRepository
                .findNivelPorExperiencia(jogoId, ficha.getXp() != null ? ficha.getXp() : 0L)
                .orElse(null);

        // Validações de negócio (null-safe)
        fichaValidationService.validarTudo(ficha, atributos, aptidoes, vantagens, nivelConfig);

        // Recalcular valores derivados
        recalcular(ficha);
    }

    /**
     * Carrega todos os sub-registros da ficha, recalcula e persiste.
     * Usa queries com JOIN FETCH para evitar N+1 ao acessar configs durante recálculo.
     *
     * <p>Carrega ClasseBonus, ClasseAptidaoBonus e FichaVantagem com efeitos
     * da classe/raça da ficha (se houver) para aplicar bônus completos.</p>
     */
    private void recalcular(Ficha ficha) {
        Long fichaId = ficha.getId();

        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaIdWithConfig(fichaId);
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaIdWithConfig(fichaId);
        List<FichaBonus> bonus = fichaBonusRepository.findByFichaIdWithConfig(fichaId);

        FichaVida vida = fichaVidaRepository.findByFichaId(fichaId).orElse(null);
        if (vida == null) return;

        List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaIdWithConfig(fichaId);
        FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(fichaId).orElse(null);
        FichaAmeaca ameaca = fichaAmeacaRepository.findByFichaId(fichaId).orElse(null);

        if (essencia == null || ameaca == null) return;

        // Carregar RacaBonusAtributo para evitar N+1 dentro do calculation service (GAP-CALC-03)
        List<RacaBonusAtributo> racaBonusAtributos = ficha.getRaca() != null
                ? racaBonusAtributoRepository.findByRacaIdWithAtributo(ficha.getRaca().getId())
                : List.of();

        // Carregar ClasseBonus e ClasseAptidaoBonus para evitar N+1 (GAP-CALC-01 / GAP-CALC-02)
        List<ClasseBonus> classeBonus = ficha.getClasse() != null
                ? classeBonusRepository.findByClasseIdWithBonusConfig(ficha.getClasse().getId())
                : List.of();
        List<ClasseAptidaoBonus> classeAptidaoBonus = ficha.getClasse() != null
                ? classeAptidaoBonusRepository.findByClasseIdWithAptidao(ficha.getClasse().getId())
                : List.of();

        // Carregar FichaVantagem com efeitos para aplicarEfeitosVantagens (Spec 007 T1)
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaIdWithEfeitos(fichaId);

        // Carregar DadoProspeccaoConfig e FichaProspeccao para DADO_UP (Spec 007 T5)
        List<DadoProspeccaoConfig> dadosOrdenados = ficha.getJogo() != null
                ? dadoProspeccaoConfigRepository.findByJogoIdOrderByOrdemExibicao(ficha.getJogo().getId())
                : List.of();
        List<FichaProspeccao> prospeccoes = fichaProspeccaoRepository.findByFichaIdWithConfig(fichaId);

        // Recalcular tudo
        fichaCalculationService.recalcular(
                ficha, atributos, aptidoes, bonus, vida, membros, essencia, ameaca,
                racaBonusAtributos, classeBonus, classeAptidaoBonus, vantagens,
                dadosOrdenados, prospeccoes);

        // Persistir sub-registros recalculados
        fichaAtributoRepository.saveAll(atributos);
        fichaAptidaoRepository.saveAll(aptidoes);
        fichaBonusRepository.saveAll(bonus);
        fichaVidaRepository.save(vida);
        fichaVidaMembroRepository.saveAll(membros);
        fichaEssenciaRepository.save(essencia);
        fichaAmeacaRepository.save(ameaca);
        fichaProspeccaoRepository.saveAll(prospeccoes);
    }
}
