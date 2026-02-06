package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.config.GameDefaultConfigProvider;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsável por inicializar configurações padrão de um jogo.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Criar todas as configurações default ao criar um jogo</li>
 *   <li>Verificar se configs já existem (evitar duplicação)</li>
 *   <li>Relacionar configs entre si (ex: bônus raciais com atributos)</li>
 * </ul>
 *
 * <p><strong>Chamado por</strong>: {@code JogoService.criarJogo()}</p>
 *
 * @author Carlos Demétrio
 * @since 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameConfigInitializerService {

    // Repositories
    private final JogoRepository jogoRepository;
    private final ConfiguracaoAtributoRepository atributoRepository;
    private final ConfiguracaoAptidaoRepository aptidaoRepository;
    private final TipoAptidaoRepository tipoAptidaoRepository;
    private final ConfiguracaoNivelRepository nivelRepository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final RacaBonusAtributoRepository racaBonusAtributoRepository;
    private final DadoProspeccaoConfigRepository prospeccaoRepository;
    private final GeneroConfigRepository generoRepository;
    private final IndoleConfigRepository indoleRepository;
    private final PresencaConfigRepository presencaRepository;
    private final MembroCorpoConfigRepository membroCorpoRepository;
    private final VantagemConfigRepository vantagemRepository;

    // Provider de dados default
    private final GameDefaultConfigProvider defaultProvider;

    /**
     * Inicializa todas as configurações padrão para um jogo.
     *
     * <p><strong>⚠️ IMPORTANTE</strong>: Este método é transacional.
     * Se qualquer erro ocorrer, todas as mudanças serão revertidas.</p>
     *
     * @param jogoId ID do jogo a ser inicializado
     * @throws IllegalArgumentException se jogo não existe
     * @throws IllegalStateException se configs já foram inicializadas
     */
    @Transactional
    public void initializeGameConfigs(Long jogoId) {
        log.info("Inicializando configurações padrão para jogo ID: {}", jogoId);

        // 1. Buscar jogo
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + jogoId));

        // 2. Verificar se já tem configs (evitar duplicação)
        if (hasExistingConfigs(jogoId)) {
            log.warn("Jogo {} já possui configurações. Pulando inicialização.", jogoId);
            return;
        }

        log.info("Criando configurações padrão para jogo '{}'...", jogo.getNome());

        // 3. Criar atributos
        log.debug("Criando atributos...");
        List<AtributoConfig> atributos = createAtributos(jogo, defaultProvider.getDefaultAtributos());

        // 4. Criar tipos de aptidão (FISICA e MENTAL)
        log.debug("Criando tipos de aptidão...");
        Map<String, TipoAptidao> tiposAptidao = createTiposAptidao(jogo);

        // 5. Criar aptidões
        log.debug("Criando aptidões...");
        createAptidoes(jogo, tiposAptidao, defaultProvider.getDefaultAptidoes());

        // 6. Criar níveis
        log.debug("Criando níveis...");
        List<NivelConfig> niveis = createNiveis(jogo, defaultProvider.getDefaultNiveis());

        // 6. Criar limitadores (se houver repository - verificar se existe)
        // log.debug("Criando limitadores...");
        // List<LimitadorConfig> limitadores = createLimitadores(jogo, defaultProvider.getDefaultLimitadores());

        // 7. Criar classes
        log.debug("Criando classes...");
        List<ClassePersonagem> classes = createClasses(jogo, defaultProvider.getDefaultClasses());

        // 8. Criar raças
        log.debug("Criando raças...");
        List<Raca> racas = createRacas(jogo, defaultProvider.getDefaultRacas());

        // 9. Criar bônus raciais
        log.debug("Criando bônus raciais...");
        createBonusRaciais(racas, atributos, defaultProvider.getDefaultBonusRaciais());

        // 10. Criar prospecções
        log.debug("Criando dados de prospecção...");
        createProspeccoes(jogo, defaultProvider.getDefaultProspeccoes());

        // 11. Criar gêneros
        log.debug("Criando gêneros...");
        createGeneros(jogo, defaultProvider.getDefaultGeneros());

        // 12. Criar índoles
        log.debug("Criando índoles...");
        createIndoles(jogo, defaultProvider.getDefaultIndoles());

        // 13. Criar presenças
        log.debug("Criando presenças...");
        createPresencas(jogo, defaultProvider.getDefaultPresencas());

        // 14. Criar membros do corpo
        log.debug("Criando membros do corpo...");
        createMembrosCorpo(jogo, defaultProvider.getDefaultMembrosCorpo());

        // 15. Criar vantagens
        log.debug("Criando vantagens...");
        createVantagens(jogo, defaultProvider.getDefaultVantagens());

        log.info("✅ Configurações padrão criadas com sucesso para jogo '{}'", jogo.getNome());
    }

    /**
     * Verifica se o jogo já possui configurações.
     * Checa apenas atributos - se tiver pelo menos 1, considera que já foi inicializado.
     *
     * @param jogoId ID do jogo
     * @return true se já possui configs, false caso contrário
     */
    private boolean hasExistingConfigs(Long jogoId) {
        long count = atributoRepository.countByJogoId(jogoId);
        return count > 0;
    }

    /**
     * Cria atributos padrão para o jogo.
     */
    private List<AtributoConfig> createAtributos(Jogo jogo, List<AtributoConfigDTO> dtos) {
        List<AtributoConfig> entities = dtos.stream()
                .map(dto -> AtributoConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .abreviacao(dto.getAbreviacao())
                        .descricao(dto.getDescricao())
                        .formulaImpeto(dto.getFormulaImpeto())
                        .descricaoImpeto(dto.getUnidadeImpeto())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        List<AtributoConfig> saved = atributoRepository.saveAll(entities);
        log.debug("✅ {} atributos criados", saved.size());
        return saved;
    }

    /**
     * Cria tipos de aptidão (FISICA e MENTAL).
     * Deve ser chamado ANTES de criar aptidões.
     */
    private Map<String, TipoAptidao> createTiposAptidao(Jogo jogo) {
        TipoAptidao fisica = TipoAptidao.builder()
                .jogo(jogo)
                .nome("FISICA")
                .descricao("Aptidões físicas e corporais")
                .ordemExibicao(1)
                .build();

        TipoAptidao mental = TipoAptidao.builder()
                .jogo(jogo)
                .nome("MENTAL")
                .descricao("Aptidões mentais e sociais")
                .ordemExibicao(2)
                .build();

        List<TipoAptidao> saved = tipoAptidaoRepository.saveAll(List.of(fisica, mental));
        log.debug("✅ {} tipos de aptidão criados", saved.size());

        Map<String, TipoAptidao> map = new HashMap<>();
        map.put("FISICA", saved.get(0));
        map.put("MENTAL", saved.get(1));
        return map;
    }

    /**
     * Cria aptidões padrão para o jogo.
     */
    private void createAptidoes(Jogo jogo, Map<String, TipoAptidao> tiposAptidao, List<AptidaoConfigDTO> dtos) {
        List<AptidaoConfig> entities = dtos.stream()
                .map(dto -> {
                    TipoAptidao tipo = tiposAptidao.get(dto.getTipo());
                    return AptidaoConfig.builder()
                            .jogo(jogo)
                            .tipoAptidao(tipo)
                            .nome(dto.getNome())
                            .descricao(dto.getDescricao())
                            .ordemExibicao(dto.getOrdemExibicao())
                            .build();
                })
                .toList();

        List<AptidaoConfig> saved = aptidaoRepository.saveAll(entities);
        log.debug("✅ {} aptidões criadas", saved.size());
    }

    /**
     * Cria níveis padrão para o jogo (0-35).
     */
    private List<NivelConfig> createNiveis(Jogo jogo, List<NivelConfigDTO> dtos) {
        List<NivelConfig> entities = dtos.stream()
                .map(dto -> {
                    NivelConfig nivel = new NivelConfig();
                    nivel.setJogo(jogo);
                    nivel.setNivel(dto.getNivel());
                    nivel.setXpNecessaria(dto.getExperienciaNecessaria());
                    nivel.setPontosAtributo(dto.getPontosAtributo());
                    nivel.setPontosAptidao(dto.getPontosAptidao());
                    nivel.setLimitadorAtributo(50); // Default limitador (will be configurable later)
                    return nivel;
                })
                .toList();

        List<NivelConfig> saved = nivelRepository.saveAll(entities);
        log.debug("✅ {} níveis criados", saved.size());
        return saved;
    }

    /**
     * Cria classes padrão para o jogo.
     */
    private List<ClassePersonagem> createClasses(Jogo jogo, List<ClasseConfigDTO> dtos) {
        List<ClassePersonagem> entities = dtos.stream()
                .map(dto -> ClassePersonagem.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        List<ClassePersonagem> saved = classeRepository.saveAll(entities);
        log.debug("✅ {} classes criadas", saved.size());
        return saved;
    }

    /**
     * Cria raças padrão para o jogo.
     */
    private List<Raca> createRacas(Jogo jogo, List<RacaConfigDTO> dtos) {
        List<Raca> entities = dtos.stream()
                .map(dto -> Raca.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        List<Raca> saved = racaRepository.saveAll(entities);
        log.debug("✅ {} raças criadas", saved.size());
        return saved;
    }

    /**
     * Cria bônus raciais (relacionamento N:N entre raças e atributos).
     *
     * @param racas Raças já criadas
     * @param atributos Atributos já criados
     * @param bonusMap Mapa de bônus (key: nome da raça, value: lista de bônus)
     */
    private void createBonusRaciais(List<Raca> racas, List<AtributoConfig> atributos,
                                     Map<String, List<BonusAtributoDTO>> bonusMap) {
        // Criar mapas para lookup rápido
        Map<String, Raca> racaMap = new HashMap<>();
        for (Raca raca : racas) {
            racaMap.put(raca.getNome(), raca);
        }

        // Map por abreviação para encontrar atributos rapidamente
        Map<String, AtributoConfig> atributoMap = new HashMap<>();
        for (AtributoConfig attr : atributos) {
            atributoMap.put(attr.getAbreviacao(), attr);
        }

        // Criar bônus
        List<RacaBonusAtributo> bonusList = new ArrayList<>();
        for (Map.Entry<String, List<BonusAtributoDTO>> entry : bonusMap.entrySet()) {
            String nomeRaca = entry.getKey();
            List<BonusAtributoDTO> bonusDTOs = entry.getValue();

            Raca raca = racaMap.get(nomeRaca);
            if (raca == null) {
                log.warn("Raça '{}' não encontrada. Pulando bônus.", nomeRaca);
                continue;
            }

            for (BonusAtributoDTO bonusDTO : bonusDTOs) {
                AtributoConfig atributo = atributoMap.get(bonusDTO.getAbreviacaoAtributo());

                if (atributo == null) {
                    log.warn("Atributo '{}' não encontrado para bônus racial. Pulando.",
                             bonusDTO.getAbreviacaoAtributo());
                    continue;
                }

                // Criar RacaBonusAtributo
                RacaBonusAtributo bonus = RacaBonusAtributo.builder()
                        .raca(raca)
                        .atributo(atributo)
                        .bonus(bonusDTO.getBonus())
                        .build();

                bonusList.add(bonus);
            }
        }

        racaBonusAtributoRepository.saveAll(bonusList);
        log.debug("✅ {} bônus raciais criados", bonusList.size());
    }

    /**
     * Cria dados de prospecção padrão para o jogo.
     */
    private void createProspeccoes(Jogo jogo, List<ProspeccaoConfigDTO> dtos) {
        List<DadoProspeccaoConfig> entities = dtos.stream()
                .map(dto -> DadoProspeccaoConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .numeroFaces(dto.getNumLados())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        prospeccaoRepository.saveAll(entities);
        log.debug("✅ {} dados de prospecção criados", entities.size());
    }

    /**
     * Cria gêneros padrão para o jogo.
     */
    private void createGeneros(Jogo jogo, List<GeneroConfigDTO> dtos) {
        List<GeneroConfig> entities = dtos.stream()
                .map(dto -> {
                    GeneroConfig genero = new GeneroConfig();
                    genero.setJogo(jogo);
                    genero.setNome(dto.getNome());
                    genero.setOrdem(dto.getOrdemExibicao());
                    return genero;
                })
                .toList();

        generoRepository.saveAll(entities);
        log.debug("✅ {} gêneros criados", entities.size());
    }

    /**
     * Cria índoles padrão para o jogo.
     */
    private void createIndoles(Jogo jogo, List<IndoleConfigDTO> dtos) {
        List<IndoleConfig> entities = dtos.stream()
                .map(dto -> IndoleConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .ordem(dto.getOrdemExibicao())
                        .build())
                .toList();

        indoleRepository.saveAll(entities);
        log.debug("✅ {} índoles criadas", entities.size());
    }

    /**
     * Cria níveis de presença padrão para o jogo.
     */
    private void createPresencas(Jogo jogo, List<PresencaConfigDTO> dtos) {
        List<PresencaConfig> entities = dtos.stream()
                .map(dto -> PresencaConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .ordem(dto.getOrdemExibicao())
                        .build())
                .toList();

        presencaRepository.saveAll(entities);
        log.debug("✅ {} presenças criadas", entities.size());
    }

    /**
     * Cria membros do corpo padrão para o jogo.
     */
    private void createMembrosCorpo(Jogo jogo, List<MembroCorpoConfigDTO> dtos) {
        List<MembroCorpoConfig> entities = dtos.stream()
                .map(dto -> MembroCorpoConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .porcentagemVida(dto.getPorcentagemVida())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        membroCorpoRepository.saveAll(entities);
        log.debug("✅ {} membros do corpo criados", entities.size());
    }

    /**
     * Cria vantagens padrão para o jogo.
     */
    private void createVantagens(Jogo jogo, List<VantagemConfigDTO> dtos) {
        List<VantagemConfig> entities = dtos.stream()
                .map(dto -> VantagemConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .nivelMaximo(dto.getNivelMaximoVantagem() != null ? dto.getNivelMaximoVantagem() : 10)
                        .formulaCusto(dto.getFormulaCusto())
                        .descricaoEfeito(dto.getTipoBonus()) // Map tipoBonus to descricaoEfeito
                        .ordemExibicao(dto.getOrdemExibicao() != null ? dto.getOrdemExibicao() : 0)
                        .build())
                .toList();

        vantagemRepository.saveAll(entities);
        log.debug("✅ {} vantagens criadas", entities.size());
    }
}
