package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.config.GameDefaultConfigProvider;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoItemEfeito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoVantagem;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final ClassePontosConfigRepository classePontosConfigRepository;
    private final ClasseBonusRepository classeBonusRepository;
    private final ClasseAptidaoBonusRepository classeAptidaoBonusRepository;
    private final ClasseVantagemPreDefinidaRepository classeVantagemPreDefinidaRepository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final RacaPontosConfigRepository racaPontosConfigRepository;
    private final RacaBonusAtributoRepository racaBonusAtributoRepository;
    private final RacaClassePermitidaRepository racaClassePermitidaRepository;
    private final RacaVantagemPreDefinidaRepository racaVantagemPreDefinidaRepository;
    private final DadoProspeccaoConfigRepository prospeccaoRepository;
    private final GeneroConfigRepository generoRepository;
    private final IndoleConfigRepository indoleRepository;
    private final PresencaConfigRepository presencaRepository;
    private final MembroCorpoConfigRepository membroCorpoRepository;
    private final VantagemConfigRepository vantagemRepository;
    private final VantagemEfeitoRepository vantagemEfeitoRepository;
    private final VantagemPreRequisitoRepository vantagemPreRequisitoRepository;
    private final BonusConfigRepository bonusConfigRepository;
    private final PontosVantagemConfigRepository pontosVantagemRepository;
    private final CategoriaVantagemRepository categoriaVantagemRepository;
    private final RaridadeItemConfigRepository raridadeItemConfigRepository;
    private final TipoItemConfigRepository tipoItemConfigRepository;
    private final ItemConfigRepository itemConfigRepository;

    // Provider de dados default
    private final GameDefaultConfigProvider defaultProvider;

    /**
     * Inicializa todas as configurações padrão para um jogo.
     *
     * <p><strong>Ordem de inicialização:</strong></p>
     * <ol>
     *   <li>AtributoConfig</li>
     *   <li>TipoAptidao</li>
     *   <li>AptidaoConfig</li>
     *   <li>BonusConfig (antes de ClasseBonus)</li>
     *   <li>NivelConfig</li>
     *   <li>PontosVantagemConfig</li>
     *   <li>CategoriaVantagem (antes de VantagemConfig)</li>
     *   <li>ClassePersonagem</li>
     *   <li>RacaConfig</li>
     *   <li>RacaBonusAtributo</li>
     *   <li>DadoProspeccaoConfig</li>
     *   <li>GeneroConfig</li>
     *   <li>IndoleConfig</li>
     *   <li>PresencaConfig</li>
     *   <li>MembroCorpoConfig</li>
     *   <li>VantagemConfig</li>
     *   <li>RaridadeItemConfig</li>
     *   <li>TipoItemConfig</li>
     *   <li>ItemConfig (com efeitos cascaded)</li>
     * </ol>
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

        // 6. Criar bônus calculados (antes de ClasseBonus para que ClasseBonus possa referenciar)
        log.debug("Criando bônus calculados...");
        Map<String, BonusConfig> bonusMap = createBonus(jogo, defaultProvider.getDefaultBonus());

        // 7. Criar níveis
        log.debug("Criando níveis...");
        createNiveis(jogo, defaultProvider.getDefaultNiveis());

        // 8. Criar pontos de vantagem por nível
        log.debug("Criando pontos de vantagem por nível...");
        createPontosVantagem(jogo, defaultProvider.getDefaultPontosVantagem());

        // 9. Criar categorias de vantagem (antes de VantagemConfig)
        log.debug("Criando categorias de vantagem...");
        Map<String, CategoriaVantagem> categorias = createCategoriasVantagem(jogo, defaultProvider.getDefaultCategoriasVantagem());

        // 10. Criar classes
        log.debug("Criando classes...");
        List<ClasseConfigDTO> classesDefault = defaultProvider.getDefaultClasses();
        List<ClassePersonagem> classes = createClasses(jogo, classesDefault);

        // 11. Criar raças
        log.debug("Criando raças...");
        List<RacaConfigDTO> racasDefault = defaultProvider.getDefaultRacas();
        List<Raca> racas = createRacas(jogo, racasDefault);

        // 12. Criar bônus raciais
        log.debug("Criando bônus raciais...");
        createBonusRaciais(racas, atributos, defaultProvider.getDefaultBonusRaciais());

        // 13. Criar prospecções
        log.debug("Criando dados de prospecção...");
        createProspeccoes(jogo, defaultProvider.getDefaultProspeccoes());

        // 14. Criar gêneros
        log.debug("Criando gêneros...");
        createGeneros(jogo, defaultProvider.getDefaultGeneros());

        // 15. Criar índoles
        log.debug("Criando índoles...");
        createIndoles(jogo, defaultProvider.getDefaultIndoles());

        // 16. Criar presenças
        log.debug("Criando presenças...");
        createPresencas(jogo, defaultProvider.getDefaultPresencas());

        // 17. Criar membros do corpo
        log.debug("Criando membros do corpo...");
        createMembrosCorpo(jogo, defaultProvider.getDefaultMembrosCorpo());

        // 18. Criar vantagens (após CategoriaVantagem)
        log.debug("Criando vantagens...");
        createVantagens(jogo, categorias, defaultProvider.getDefaultVantagens());

        // 18.1 Criar vantagens predefinidas de classes e raças (dependem de VantagemConfig)
        log.debug("Criando vantagens predefinidas de classes...");
        createClasseVantagensPreDefinidas(classes, classesDefault, jogo.getId());

        log.debug("Criando vantagens predefinidas de raças...");
        createRacaVantagensPreDefinidas(racas, racasDefault, jogo.getId());

        // 19. Criar raridades de itens
        log.debug("Criando raridades de itens...");
        Map<String, RaridadeItemConfig> raridadeMap = createRaridades(jogo, defaultProvider.getDefaultRaridades());

        // 20. Criar tipos de itens
        log.debug("Criando tipos de itens...");
        Map<String, TipoItemConfig> tipoMap = createTipos(jogo, defaultProvider.getDefaultTipos());

        // 21. Criar itens (com efeitos cascaded) — referencia bônus e atributos já criados
        log.debug("Criando itens...");
        Map<String, AtributoConfig> atributoMap = atributos.stream()
                .collect(Collectors.toMap(AtributoConfig::getAbreviacao, a -> a));
        createItens(jogo, raridadeMap, tipoMap, bonusMap, atributoMap, defaultProvider.getDefaultItens());

        log.info("Configuracoes padrao criadas com sucesso para jogo '{}'", jogo.getNome());
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
                        .valorMinimo(dto.getValorMinimo() != null ? dto.getValorMinimo() : 0)
                        .valorMaximo(dto.getValorMaximo() != null ? dto.getValorMaximo() : 999)
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        List<AtributoConfig> saved = atributoRepository.saveAll(entities);
        log.debug("{} atributos criados", saved.size());
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
        log.debug("{} tipos de aptidão criados", saved.size());

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
        log.debug("{} aptidões criadas", saved.size());
    }

    /**
     * Cria bônus calculados padrão para o jogo (B.B.A, B.B.M, Defesa, etc.).
     * Deve ser chamado ANTES de criar classes (para ClasseBonus poder referenciar BonusConfig).
     *
     * @return Mapa nome → BonusConfig salvo (usado por createItens para resolver efeitos)
     */
    private Map<String, BonusConfig> createBonus(Jogo jogo, List<BonusConfigDTO> dtos) {
        List<BonusConfig> entities = dtos.stream()
                .map(dto -> BonusConfig.builder()
                        .jogo(jogo)
                        .nome(dto.nome())
                        .sigla(dto.sigla())
                        .formulaBase(dto.formulaBase())
                        .descricao(dto.descricao())
                        .ordemExibicao(dto.ordemExibicao())
                        .build())
                .toList();

        List<BonusConfig> saved = bonusConfigRepository.saveAll(entities);
        log.debug("{} bônus criados", saved.size());
        return saved.stream().collect(Collectors.toMap(BonusConfig::getNome, b -> b));
    }

    /**
     * Cria níveis padrão para o jogo (0-35).
     * BUG-DC-02 corrigido: limitadorAtributo lido do DTO em vez de hardcoded 50.
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
                    nivel.setLimitadorAtributo(dto.getLimitadorAtributo() != null ? dto.getLimitadorAtributo() : 50);
                    nivel.setPermitirRenascimento(dto.getNivel() >= 31);
                    return nivel;
                })
                .toList();

        List<NivelConfig> saved = nivelRepository.saveAll(entities);
        log.debug("{} níveis criados", saved.size());
        return saved;
    }

    /**
     * Cria pontos de vantagem por nível padrão para o jogo.
     */
    private void createPontosVantagem(Jogo jogo, List<PontosVantagemConfigDTO> dtos) {
        List<PontosVantagemConfig> entities = dtos.stream()
                .map(dto -> PontosVantagemConfig.builder()
                        .jogo(jogo)
                        .nivel(dto.nivel())
                        .pontosGanhos(dto.pontos())
                        .build())
                .toList();

        pontosVantagemRepository.saveAll(entities);
        log.debug("{} pontos de vantagem criados", entities.size());
    }

    /**
     * Cria categorias de vantagem padrão para o jogo.
     * Deve ser chamado ANTES de criar vantagens.
     *
     * @return Mapa de nome da categoria → entidade salva (para uso no createVantagens)
     */
    private Map<String, CategoriaVantagem> createCategoriasVantagem(Jogo jogo, List<CategoriaVantagemDTO> dtos) {
        List<CategoriaVantagem> entities = dtos.stream()
                .map(dto -> CategoriaVantagem.builder()
                        .jogo(jogo)
                        .nome(dto.nome())
                        .cor(dto.cor())
                        .ordemExibicao(dto.ordemExibicao())
                        .build())
                .toList();

        List<CategoriaVantagem> saved = categoriaVantagemRepository.saveAll(entities);
        log.debug("{} categorias de vantagem criadas", saved.size());

        Map<String, CategoriaVantagem> map = new HashMap<>();
        for (CategoriaVantagem categoria : saved) {
            map.put(categoria.getNome(), categoria);
        }
        return map;
    }

    /**
     * Cria classes padrão para o jogo e seus pontos de bônus por nível.
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

        Map<String, ClassePersonagem> classeMap = saved.stream()
                .collect(Collectors.toMap(ClassePersonagem::getNome, c -> c));
        List<ClassePontosConfig> pontos = new ArrayList<>();
        for (ClasseConfigDTO dto : dtos) {
            ClassePersonagem classe = classeMap.get(dto.getNome());
            if (classe != null && dto.getPontosConfig() != null) {
                for (var p : dto.getPontosConfig()) {
                    pontos.add(ClassePontosConfig.builder()
                            .classePersonagem(classe)
                            .nivel(p.nivel())
                            .pontosAtributo(p.pontosAtributo())
                            .pontosVantagem(p.pontosVantagem())
                            .build());
                }
            }
        }
        if (!pontos.isEmpty()) {
            classePontosConfigRepository.saveAll(pontos);
            log.debug("{} ClassePontosConfig entries criadas", pontos.size());
        }

        Map<String, BonusConfig> bonusMap = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(BonusConfig::getNome, bonus -> bonus));
        List<ClasseBonus> bonusClasses = new ArrayList<>();
        for (ClasseConfigDTO dto : dtos) {
            ClassePersonagem classe = classeMap.get(dto.getNome());
            if (classe == null || dto.getBonusDefaults() == null) {
                continue;
            }

            for (ClasseBonusDefault bonusDefault : dto.getBonusDefaults()) {
                BonusConfig bonus = bonusMap.get(bonusDefault.bonusNome());
                if (bonus == null) {
                    log.warn("BonusConfig '{}' não encontrado para classe '{}'. Pulando bônus de classe.",
                            bonusDefault.bonusNome(), dto.getNome());
                    continue;
                }

                bonusClasses.add(ClasseBonus.builder()
                        .classe(classe)
                        .bonus(bonus)
                        .valorPorNivel(bonusDefault.valorPorNivel())
                        .build());
            }
        }

        if (!bonusClasses.isEmpty()) {
            classeBonusRepository.saveAll(bonusClasses);
            log.debug("{} ClasseBonus entries criadas", bonusClasses.size());
        }

        Map<String, AptidaoConfig> aptidaoMap = aptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(AptidaoConfig::getNome, aptidao -> aptidao));
        List<ClasseAptidaoBonus> aptidaoBonus = new ArrayList<>();
        for (ClasseConfigDTO dto : dtos) {
            ClassePersonagem classe = classeMap.get(dto.getNome());
            if (classe == null || dto.getAptidaoBonusDefaults() == null) {
                continue;
            }

            for (ClasseAptidaoBonusDefault aptidaoBonusDefault : dto.getAptidaoBonusDefaults()) {
                AptidaoConfig aptidao = aptidaoMap.get(aptidaoBonusDefault.aptidaoNome());
                if (aptidao == null) {
                    log.warn("AptidaoConfig '{}' não encontrada para classe '{}'. Pulando bônus de aptidão.",
                            aptidaoBonusDefault.aptidaoNome(), dto.getNome());
                    continue;
                }

                aptidaoBonus.add(ClasseAptidaoBonus.builder()
                        .classe(classe)
                        .aptidao(aptidao)
                        .bonus(aptidaoBonusDefault.bonus())
                        .build());
            }
        }

        if (!aptidaoBonus.isEmpty()) {
            classeAptidaoBonusRepository.saveAll(aptidaoBonus);
            log.debug("{} ClasseAptidaoBonus entries criadas", aptidaoBonus.size());
        }

        log.debug("{} classes criadas", saved.size());
        return saved;
    }

    /**
     * Cria raças padrão para o jogo e seus pontos de bônus por marcos de nível.
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

        Map<String, Raca> racaMap = saved.stream()
                .collect(Collectors.toMap(Raca::getNome, r -> r));
        List<RacaPontosConfig> pontos = new ArrayList<>();
        for (RacaConfigDTO dto : dtos) {
            Raca raca = racaMap.get(dto.getNome());
            if (raca != null && dto.getPontosConfig() != null) {
                for (var p : dto.getPontosConfig()) {
                    pontos.add(RacaPontosConfig.builder()
                            .raca(raca)
                            .nivel(p.nivel())
                            .pontosAtributo(p.pontosAtributo())
                            .pontosVantagem(p.pontosVantagem())
                            .build());
                }
            }
        }
        if (!pontos.isEmpty()) {
            racaPontosConfigRepository.saveAll(pontos);
            log.debug("{} RacaPontosConfig entries criadas", pontos.size());
        }

        Map<String, ClassePersonagem> classeMap = classeRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(ClassePersonagem::getNome, classe -> classe));
        List<RacaClassePermitida> classesPermitidas = new ArrayList<>();
        for (RacaConfigDTO dto : dtos) {
            Raca raca = racaMap.get(dto.getNome());
            if (raca == null || dto.getClassesPermitidas() == null) {
                continue;
            }

            for (RacaClassePermitidaDefault classePermitidaDefault : dto.getClassesPermitidas()) {
                ClassePersonagem classe = classeMap.get(classePermitidaDefault.classeNome());
                if (classe == null) {
                    log.warn("Classe '{}' não encontrada para raça '{}'. Pulando restrição de classe.",
                            classePermitidaDefault.classeNome(), dto.getNome());
                    continue;
                }

                classesPermitidas.add(RacaClassePermitida.builder()
                        .raca(raca)
                        .classe(classe)
                        .build());
            }
        }

        if (!classesPermitidas.isEmpty()) {
            racaClassePermitidaRepository.saveAll(classesPermitidas);
            log.debug("{} RacaClassePermitida entries criadas", classesPermitidas.size());
        }

        log.debug("{} raças criadas", saved.size());
        return saved;
    }

    private void createClasseVantagensPreDefinidas(List<ClassePersonagem> classes,
                                                   List<ClasseConfigDTO> dtos,
                                                   Long jogoId) {
        Map<String, ClassePersonagem> classeMap = classes.stream()
                .collect(Collectors.toMap(ClassePersonagem::getNome, classe -> classe));
        Map<String, VantagemConfig> vantagensPorNome = vantagemRepository.findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .collect(Collectors.toMap(VantagemConfig::getNome, vantagem -> vantagem));
        Map<String, VantagemConfig> vantagensPorSigla = vantagemRepository.findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .filter(vantagem -> vantagem.getSigla() != null)
                .collect(Collectors.toMap(VantagemConfig::getSigla, vantagem -> vantagem));

        List<ClasseVantagemPreDefinida> vantagensPreDefinidas = new ArrayList<>();
        for (ClasseConfigDTO dto : dtos) {
            ClassePersonagem classe = classeMap.get(dto.getNome());
            if (classe == null || dto.getVantagemPreDefinidaDefaults() == null) {
                continue;
            }

            for (ClasseVantagemPreDefinidaDefault vantagemDefault : dto.getVantagemPreDefinidaDefaults()) {
                VantagemConfig vantagem = resolveVantagem(vantagensPorNome, vantagensPorSigla,
                        vantagemDefault.vantagemNome(), vantagemDefault.vantagemSigla());
                if (vantagem == null) {
                    log.warn("Vantagem '{}'/'{}' não encontrada para classe '{}'. Pulando vantagem predefinida.",
                            vantagemDefault.vantagemNome(), vantagemDefault.vantagemSigla(), dto.getNome());
                    continue;
                }

                vantagensPreDefinidas.add(ClasseVantagemPreDefinida.builder()
                        .classePersonagem(classe)
                        .vantagemConfig(vantagem)
                        .nivel(vantagemDefault.nivel() != null ? vantagemDefault.nivel() : 1)
                        .build());
            }
        }

        if (!vantagensPreDefinidas.isEmpty()) {
            classeVantagemPreDefinidaRepository.saveAll(vantagensPreDefinidas);
            log.debug("{} ClasseVantagemPreDefinida entries criadas", vantagensPreDefinidas.size());
        }
    }

    private void createRacaVantagensPreDefinidas(List<Raca> racas,
                                                 List<RacaConfigDTO> dtos,
                                                 Long jogoId) {
        Map<String, Raca> racaMap = racas.stream()
                .collect(Collectors.toMap(Raca::getNome, raca -> raca));
        Map<String, VantagemConfig> vantagensPorNome = vantagemRepository.findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .collect(Collectors.toMap(VantagemConfig::getNome, vantagem -> vantagem));
        Map<String, VantagemConfig> vantagensPorSigla = vantagemRepository.findByJogoIdOrderByOrdemExibicao(jogoId).stream()
                .filter(vantagem -> vantagem.getSigla() != null)
                .collect(Collectors.toMap(VantagemConfig::getSigla, vantagem -> vantagem));

        List<RacaVantagemPreDefinida> vantagensPreDefinidas = new ArrayList<>();
        for (RacaConfigDTO dto : dtos) {
            Raca raca = racaMap.get(dto.getNome());
            if (raca == null || dto.getVantagemPreDefinidaDefaults() == null) {
                continue;
            }

            for (RacaVantagemPreDefinidaDefault vantagemDefault : dto.getVantagemPreDefinidaDefaults()) {
                VantagemConfig vantagem = resolveVantagem(vantagensPorNome, vantagensPorSigla,
                        vantagemDefault.vantagemNome(), vantagemDefault.vantagemSigla());
                if (vantagem == null) {
                    log.warn("Vantagem '{}'/'{}' não encontrada para raça '{}'. Pulando vantagem racial predefinida.",
                            vantagemDefault.vantagemNome(), vantagemDefault.vantagemSigla(), dto.getNome());
                    continue;
                }

                vantagensPreDefinidas.add(RacaVantagemPreDefinida.builder()
                        .raca(raca)
                        .vantagemConfig(vantagem)
                        .nivel(vantagemDefault.nivel() != null ? vantagemDefault.nivel() : 1)
                        .build());
            }
        }

        if (!vantagensPreDefinidas.isEmpty()) {
            racaVantagemPreDefinidaRepository.saveAll(vantagensPreDefinidas);
            log.debug("{} RacaVantagemPreDefinida entries criadas", vantagensPreDefinidas.size());
        }
    }

    private VantagemConfig resolveVantagem(Map<String, VantagemConfig> vantagensPorNome,
                                           Map<String, VantagemConfig> vantagensPorSigla,
                                           String vantagemNome,
                                           String vantagemSigla) {
        if (vantagemSigla != null && vantagensPorSigla.containsKey(vantagemSigla)) {
            return vantagensPorSigla.get(vantagemSigla);
        }

        if (vantagemNome != null) {
            return vantagensPorNome.get(vantagemNome);
        }

        return null;
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
        Map<String, Raca> racaMap = new HashMap<>();
        for (Raca raca : racas) {
            racaMap.put(raca.getNome(), raca);
        }

        Map<String, AtributoConfig> atributoMap = new HashMap<>();
        for (AtributoConfig attr : atributos) {
            atributoMap.put(attr.getAbreviacao(), attr);
        }

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

                RacaBonusAtributo bonus = RacaBonusAtributo.builder()
                        .raca(raca)
                        .atributo(atributo)
                        .bonus(bonusDTO.getBonus())
                        .build();

                bonusList.add(bonus);
            }
        }

        racaBonusAtributoRepository.saveAll(bonusList);
        log.debug("{} bônus raciais criados", bonusList.size());
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
                        .descricao(dto.getDescricao())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        prospeccaoRepository.saveAll(entities);
        log.debug("{} dados de prospecção criados", entities.size());
    }

    /**
     * Cria gêneros padrão para o jogo.
     */
    private void createGeneros(Jogo jogo, List<GeneroConfigDTO> dtos) {
        List<GeneroConfig> entities = dtos.stream()
                .map(dto -> GeneroConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        generoRepository.saveAll(entities);
        log.debug("{} gêneros criados", entities.size());
    }

    /**
     * Cria índoles padrão para o jogo.
     */
    private void createIndoles(Jogo jogo, List<IndoleConfigDTO> dtos) {
        List<IndoleConfig> entities = dtos.stream()
                .map(dto -> IndoleConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        indoleRepository.saveAll(entities);
        log.debug("{} índoles criadas", entities.size());
    }

    /**
     * Cria níveis de presença padrão para o jogo.
     */
    private void createPresencas(Jogo jogo, List<PresencaConfigDTO> dtos) {
        List<PresencaConfig> entities = dtos.stream()
                .map(dto -> PresencaConfig.builder()
                        .jogo(jogo)
                        .nome(dto.getNome())
                        .descricao(dto.getDescricao())
                        .ordemExibicao(dto.getOrdemExibicao())
                        .build())
                .toList();

        presencaRepository.saveAll(entities);
        log.debug("{} presenças criadas", entities.size());
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
        log.debug("{} membros do corpo criados", entities.size());
    }

    /**
     * Cria vantagens padrão para o jogo.
     * As vantagens são associadas às categorias criadas anteriormente.
     *
     * @param jogo        Jogo alvo
     * @param categorias  Mapa de nome → CategoriaVantagem (criadas por createCategoriasVantagem)
     * @param dtos        Lista de DTOs de vantagem
     */
    private void createVantagens(Jogo jogo, Map<String, CategoriaVantagem> categorias, List<VantagemConfigDTO> dtos) {
        List<VantagemConfig> entities = dtos.stream()
                .map(dto -> {
                    CategoriaVantagem categoria = dto.getCategoriaNome() != null
                            ? categorias.get(dto.getCategoriaNome())
                            : null;
                    TipoVantagem tipo = dto.getTipoVantagem() != null
                            ? TipoVantagem.valueOf(dto.getTipoVantagem())
                            : TipoVantagem.VANTAGEM;
                    return VantagemConfig.builder()
                            .jogo(jogo)
                            .nome(dto.getNome())
                            .sigla(dto.getSigla())
                            .descricao(dto.getDescricao())
                            .nivelMaximo(dto.getNivelMaximoVantagem() != null ? dto.getNivelMaximoVantagem() : 1)
                            .formulaCusto(dto.getFormulaCusto() != null ? dto.getFormulaCusto() : "0")
                            .descricaoEfeito(dto.getValorBonusFormula())
                            .tipoVantagem(tipo)
                            .categoriaVantagem(categoria)
                            .ordemExibicao(dto.getOrdemExibicao() != null ? dto.getOrdemExibicao() : 0)
                             .build();
                 })
                 .toList();

        List<VantagemConfig> saved = vantagemRepository.saveAll(entities);
        log.debug("{} vantagens criadas", saved.size());

        Map<String, VantagemConfig> vantagemMap = saved.stream()
                .collect(Collectors.toMap(VantagemConfig::getSigla, vantagem -> vantagem));
        Map<String, BonusConfig> bonusMap = bonusConfigRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(BonusConfig::getNome, bonus -> bonus));
        Map<String, AtributoConfig> atributoMap = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(AtributoConfig::getAbreviacao, atributo -> atributo));
        Map<String, AptidaoConfig> aptidaoMap = aptidaoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(AptidaoConfig::getNome, aptidao -> aptidao));
        Map<String, MembroCorpoConfig> membroMap = membroCorpoRepository.findByJogoIdOrderByOrdemExibicao(jogo.getId()).stream()
                .collect(Collectors.toMap(MembroCorpoConfig::getNome, membro -> membro));

        List<VantagemEfeito> efeitos = new ArrayList<>();
        for (VantagemConfigDTO dto : dtos) {
            VantagemConfig vantagem = vantagemMap.get(dto.getSigla());
            if (vantagem == null || dto.getEfeitos() == null || dto.getEfeitos().isEmpty()) {
                continue;
            }

            for (VantagemEfeitoDefault efeitoDto : dto.getEfeitos()) {
                VantagemEfeito efeito = buildVantagemEfeito(efeitoDto, vantagem, bonusMap, atributoMap, aptidaoMap, membroMap);
                if (efeito != null) {
                    efeitos.add(efeito);
                }
            }
        }

        if (!efeitos.isEmpty()) {
            vantagemEfeitoRepository.saveAll(efeitos);
            log.debug("{} efeitos de vantagem criados", efeitos.size());
        }

        List<VantagemPreRequisito> preRequisitos = new ArrayList<>();
        for (VantagemConfigDTO dto : dtos) {
            VantagemConfig vantagem = vantagemMap.get(dto.getSigla());
            if (vantagem == null || dto.getPreRequisitos() == null || dto.getPreRequisitos().isEmpty()) {
                continue;
            }

            for (VantagemPreRequisitoDefault preRequisitoDto : dto.getPreRequisitos()) {
                VantagemConfig requisito = vantagemMap.get(preRequisitoDto.requisitoSigla());
                if (requisito == null) {
                    log.warn("Vantagem requisito '{}' não encontrada para '{}'. Pulando pré-requisito.",
                            preRequisitoDto.requisitoSigla(), vantagem.getNome());
                    continue;
                }

                preRequisitos.add(VantagemPreRequisito.builder()
                        .vantagem(vantagem)
                        .requisito(requisito)
                        .nivelMinimo(preRequisitoDto.nivelMinimo() != null ? preRequisitoDto.nivelMinimo() : 1)
                        .build());
            }
        }

        if (!preRequisitos.isEmpty()) {
            vantagemPreRequisitoRepository.saveAll(preRequisitos);
            log.debug("{} pré-requisitos de vantagem criados", preRequisitos.size());
        }
    }

    private VantagemEfeito buildVantagemEfeito(VantagemEfeitoDefault dto,
                                               VantagemConfig vantagem,
                                               Map<String, BonusConfig> bonusMap,
                                               Map<String, AtributoConfig> atributoMap,
                                               Map<String, AptidaoConfig> aptidaoMap,
                                               Map<String, MembroCorpoConfig> membroMap) {
        VantagemEfeito.VantagemEfeitoBuilder builder = VantagemEfeito.builder()
                .vantagemConfig(vantagem)
                .tipoEfeito(dto.tipoEfeito())
                .valorFixo(dto.valorFixo())
                .valorPorNivel(dto.valorPorNivel())
                .formula(dto.formula())
                .descricaoEfeito(dto.descricaoEfeito());

        if (dto.bonusAlvoNome() != null) {
            BonusConfig bonus = bonusMap.get(dto.bonusAlvoNome());
            if (bonus == null) {
                log.warn("BonusConfig '{}' não encontrado para efeito da vantagem '{}'. Pulando efeito.",
                        dto.bonusAlvoNome(), vantagem.getNome());
                return null;
            }
            builder.bonusAlvo(bonus);
        }

        if (dto.atributoAlvoSigla() != null) {
            AtributoConfig atributo = atributoMap.get(dto.atributoAlvoSigla());
            if (atributo == null) {
                log.warn("AtributoConfig '{}' não encontrado para efeito da vantagem '{}'. Pulando efeito.",
                        dto.atributoAlvoSigla(), vantagem.getNome());
                return null;
            }
            builder.atributoAlvo(atributo);
        }

        if (dto.aptidaoAlvoNome() != null) {
            AptidaoConfig aptidao = aptidaoMap.get(dto.aptidaoAlvoNome());
            if (aptidao == null) {
                log.warn("AptidaoConfig '{}' não encontrada para efeito da vantagem '{}'. Pulando efeito.",
                        dto.aptidaoAlvoNome(), vantagem.getNome());
                return null;
            }
            builder.aptidaoAlvo(aptidao);
        }

        if (dto.membroAlvoNome() != null) {
            MembroCorpoConfig membro = membroMap.get(dto.membroAlvoNome());
            if (membro == null) {
                log.warn("MembroCorpoConfig '{}' não encontrado para efeito da vantagem '{}'. Pulando efeito.",
                        dto.membroAlvoNome(), vantagem.getNome());
                return null;
            }
            builder.membroAlvo(membro);
        }

        return builder.build();
    }

    /**
     * Cria raridades de itens padrão para o jogo.
     *
     * @return Mapa nome → RaridadeItemConfig salvo (para uso em createItens)
     */
    private Map<String, RaridadeItemConfig> createRaridades(Jogo jogo, List<RaridadeItemConfigDefault> dtos) {
        List<RaridadeItemConfig> entities = dtos.stream()
                .map(dto -> RaridadeItemConfig.builder()
                        .jogo(jogo)
                        .nome(dto.nome())
                        .cor(dto.cor())
                        .ordemExibicao(dto.ordemExibicao())
                        .podeJogadorAdicionar(dto.podeJogadorAdicionar())
                        .bonusAtributoMin(dto.bonusAtributoMin())
                        .bonusAtributoMax(dto.bonusAtributoMax())
                        .bonusDerivadoMin(dto.bonusDerivadoMin())
                        .bonusDerivadoMax(dto.bonusDerivadoMax())
                        .descricao(dto.descricao())
                        .build())
                .toList();

        List<RaridadeItemConfig> saved = raridadeItemConfigRepository.saveAll(entities);
        log.debug("{} raridades de itens criadas", saved.size());
        return saved.stream().collect(Collectors.toMap(RaridadeItemConfig::getNome, r -> r));
    }

    /**
     * Cria tipos de itens padrão para o jogo.
     *
     * @return Mapa nome → TipoItemConfig salvo (para uso em createItens)
     */
    private Map<String, TipoItemConfig> createTipos(Jogo jogo, List<TipoItemConfigDefault> dtos) {
        List<TipoItemConfig> entities = dtos.stream()
                .map(dto -> TipoItemConfig.builder()
                        .jogo(jogo)
                        .nome(dto.nome())
                        .categoria(dto.categoria())
                        .subcategoria(dto.subcategoria())
                        .requerDuasMaos(dto.requerDuasMaos())
                        .ordemExibicao(dto.ordemExibicao())
                        .build())
                .toList();

        List<TipoItemConfig> saved = tipoItemConfigRepository.saveAll(entities);
        log.debug("{} tipos de itens criados", saved.size());
        return saved.stream().collect(Collectors.toMap(TipoItemConfig::getNome, t -> t));
    }

    /**
     * Cria itens padrão para o jogo, resolvendo referências por nome a raridades, tipos, bônus e atributos.
     *
     * <p>Para efeitos do tipo BONUS_DERIVADO, busca o BonusConfig pelo nome no mapa de bônus.
     * Para efeitos do tipo BONUS_ATRIBUTO, busca o AtributoConfig pela abreviação no mapa de atributos.
     * Para BONUS_VIDA e BONUS_ESSENCIA, não há FK de alvo — apenas valorFixo.</p>
     *
     * @param raridadeMap  Mapa nome → RaridadeItemConfig (criado por createRaridades)
     * @param tipoMap      Mapa nome → TipoItemConfig (criado por createTipos)
     * @param bonusMap     Mapa nome → BonusConfig (criado por createBonus)
     * @param atributoMap  Mapa abreviação → AtributoConfig (construído de createAtributos)
     */
    private void createItens(Jogo jogo,
                              Map<String, RaridadeItemConfig> raridadeMap,
                              Map<String, TipoItemConfig> tipoMap,
                              Map<String, BonusConfig> bonusMap,
                              Map<String, AtributoConfig> atributoMap,
                              List<ItemConfigDefault> dtos) {
        List<ItemConfig> items = new ArrayList<>();

        for (ItemConfigDefault dto : dtos) {
            RaridadeItemConfig raridade = raridadeMap.get(dto.raridadeNome());
            if (raridade == null) {
                log.warn("Raridade '{}' não encontrada para item '{}'. Pulando.", dto.raridadeNome(), dto.nome());
                continue;
            }

            TipoItemConfig tipo = tipoMap.get(dto.tipoNome());
            if (tipo == null) {
                log.warn("Tipo '{}' não encontrado para item '{}'. Pulando.", dto.tipoNome(), dto.nome());
                continue;
            }

            ItemConfig item = ItemConfig.builder()
                    .jogo(jogo)
                    .nome(dto.nome())
                    .raridade(raridade)
                    .tipo(tipo)
                    .peso(dto.peso())
                    .valor(dto.valor())
                    .duracaoPadrao(dto.duracaoPadrao())
                    .nivelMinimo(dto.nivelMinimo())
                    .propriedades(dto.propriedades())
                    .ordemExibicao(dto.ordemExibicao())
                    .build();

            List<ItemEfeito> efeitos = new ArrayList<>();
            for (ItemEfeitoDefault efDto : dto.efeitos()) {
                ItemEfeito efeito = buildItemEfeito(efDto, item, bonusMap, atributoMap);
                if (efeito != null) {
                    efeitos.add(efeito);
                }
            }
            item.setEfeitos(efeitos);
            items.add(item);
        }

        List<ItemConfig> saved = itemConfigRepository.saveAll(items);
        log.debug("{} itens criados", saved.size());
    }

    /**
     * Constrói um ItemEfeito resolvendo as FKs de alvo por nome/abreviação.
     *
     * @return ItemEfeito construído, ou null se o alvo obrigatório não for encontrado
     */
    private ItemEfeito buildItemEfeito(ItemEfeitoDefault dto,
                                        ItemConfig itemConfig,
                                        Map<String, BonusConfig> bonusMap,
                                        Map<String, AtributoConfig> atributoMap) {
        ItemEfeito.ItemEfeitoBuilder builder = ItemEfeito.builder()
                .itemConfig(itemConfig)
                .tipoEfeito(dto.tipoEfeito())
                .valorFixo(dto.valorFixo());

        if (dto.tipoEfeito() == TipoItemEfeito.BONUS_DERIVADO) {
            BonusConfig bonus = bonusMap.get(dto.bonusAlvoNome());
            if (bonus == null) {
                log.warn("BonusConfig '{}' não encontrado para efeito de '{}'. Pulando efeito.",
                         dto.bonusAlvoNome(), itemConfig.getNome());
                return null;
            }
            builder.bonusAlvo(bonus);

        } else if (dto.tipoEfeito() == TipoItemEfeito.BONUS_ATRIBUTO) {
            AtributoConfig atributo = atributoMap.get(dto.atributoAlvoNome());
            if (atributo == null) {
                log.warn("AtributoConfig com abreviação '{}' não encontrado para efeito de '{}'. Pulando efeito.",
                         dto.atributoAlvoNome(), itemConfig.getNome());
                return null;
            }
            builder.atributoAlvo(atributo);
        }
        // BONUS_VIDA e BONUS_ESSENCIA não têm FK de alvo — apenas valorFixo já setado

        return builder.build();
    }
}
