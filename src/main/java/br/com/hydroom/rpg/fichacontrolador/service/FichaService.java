package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
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

        // 3. Determinar jogadorId
        Long jogadorId = request.jogadorId();
        if (!isMestre) {
            // Jogador só pode criar ficha para si mesmo
            jogadorId = usuarioAtual.getId();
        }

        // 4. Validar FK do mesmo jogo (se fornecidos)
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

        // 5. Criar e salvar a Ficha
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

        // 6. Inicializar sub-registros
        inicializarSubRegistros(ficha, jogo);

        // 7. Recalcular valores derivados
        recalcular(ficha);

        log.info("Ficha '{}' criada com sucesso (ID: {})", ficha.getNome(), ficha.getId());
        return ficha;
    }

    /**
     * Busca uma ficha por ID, verificando acesso.
     */
    public Ficha buscarPorId(Long id) {
        Ficha ficha = fichaRepository.findById(id)
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
     */
    public List<Ficha> listarComFiltros(Long jogoId, String nome, Long classeId, Long racaId, Integer nivel) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return fichaRepository.findByJogoIdWithFilters(jogoId, nome, classeId, racaId, nivel);
        } else {
            return fichaRepository.findByJogoIdAndJogadorIdWithFilters(
                    jogoId, usuarioAtual.getId(), nome, classeId, racaId, nivel);
        }
    }

    /**
     * Lista fichas do usuário atual em um jogo (apenas jogador, isNpc=false).
     */
    public List<Ficha> listarMinhas(Long jogoId) {
        jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResourceNotFoundException("Jogo não encontrado: " + jogoId));

        Usuario usuarioAtual = getUsuarioAtual();
        return fichaRepository.findByJogoIdAndJogadorIdAndIsNpcFalse(jogoId, usuarioAtual.getId());
    }

    /**
     * Lista apenas NPCs de um jogo (apenas Mestre).
     */
    public List<Ficha> listarNpcs(Long jogoId) {
        Usuario usuarioAtual = getUsuarioAtual();
        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (!isMestre) {
            throw new ForbiddenException("Apenas o Mestre pode visualizar NPCs.");
        }

        return fichaRepository.findByJogoIdAndIsNpcTrue(jogoId);
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
     * Verifica se o usuário atual pode LER a ficha.
     */
    private void verificarAcessoLeitura(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return; // Mestre vê tudo
        }

        // Jogador só vê suas próprias fichas
        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }
    }

    /**
     * Verifica se o usuário atual pode ESCREVER na ficha.
     */
    private void verificarAcessoEscrita(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return; // Mestre pode editar qualquer ficha
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
     */
    private void validarERecalcular(Ficha ficha) {
        Long fichaId = ficha.getId();
        Long jogoId = ficha.getJogo().getId();

        // Carregar sub-registros
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaId(fichaId);
        List<FichaVantagem> vantagens = fichaVantagemRepository.findByFichaId(fichaId);
        List<FichaBonus> bonus = fichaBonusRepository.findByFichaId(fichaId);

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
     */
    private void recalcular(Ficha ficha) {
        Long fichaId = ficha.getId();

        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaId);
        List<FichaBonus> bonus = fichaBonusRepository.findByFichaId(fichaId);

        FichaVida vida = fichaVidaRepository.findByFichaId(fichaId).orElse(null);
        if (vida == null) return;

        List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaId(fichaId);
        FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(fichaId).orElse(null);
        FichaAmeaca ameaca = fichaAmeacaRepository.findByFichaId(fichaId).orElse(null);

        if (essencia == null || ameaca == null) return;

        // Recalcular tudo
        fichaCalculationService.recalcular(ficha, atributos, bonus, vida, membros, essencia, ameaca);

        // Persistir sub-registros recalculados
        fichaAtributoRepository.saveAll(atributos);
        fichaBonusRepository.saveAll(bonus);
        fichaVidaRepository.save(vida);
        fichaVidaMembroRepository.saveAll(membros);
        fichaEssenciaRepository.save(essencia);
        fichaAmeacaRepository.save(ameaca);
    }
}
