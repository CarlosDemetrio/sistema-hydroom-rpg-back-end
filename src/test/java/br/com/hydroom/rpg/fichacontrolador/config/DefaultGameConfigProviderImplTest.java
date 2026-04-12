package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.config.defaults.*;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@DisplayName("DefaultGameConfigProviderImpl - Testes Unitários")
class DefaultGameConfigProviderImplTest {

    private DefaultGameConfigProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultGameConfigProviderImpl(
                new DefaultAtributosProvider(),
                new DefaultAptidoesProvider(),
                new DefaultNiveisProvider(),
                new DefaultClassesProvider(),
                new DefaultRacasProvider(),
                new DefaultProspeccoesProvider(),
                new DefaultConfigSimpleProvider(),
                new DefaultBonusProvider(),
                new DefaultPontosVantagemProvider(),
                new DefaultVantagensProvider(),
                new DefaultItensProvider()
        );
    }

    @Test
    @DisplayName("T5-01: Provider retorna 9 BonusConfig defaults com nome, sigla e formula")
    void deveRetornarNoveBonusDefaults() {
        List<BonusConfigDTO> bonus = provider.getDefaultBonus();

        assertThat(bonus).hasSize(9);

        assertThat(bonus)
            .extracting(BonusConfigDTO::nome)
            .containsExactlyInAnyOrder(
                "B.B.A", "B.B.M", "Defesa", "Esquiva", "Iniciativa",
                "Percepção", "Raciocínio", "Bloqueio", "Reflexo"
            );

        bonus.forEach(b -> {
            assertThat(b.nome()).as("Nome de '%s' não pode ser vazio", b.sigla()).isNotBlank();
            assertThat(b.sigla()).as("Sigla de '%s' não pode ser vazia", b.nome()).isNotBlank();
            assertThat(b.formulaBase()).as("Fórmula de '%s' não pode ser vazia", b.nome()).isNotBlank();
        });
    }

    @Test
    @DisplayName("T5-02: Provider retorna 35 PontosVantagemConfig defaults com valores corretos")
    void deveRetornarOitoPontosVantagemDefaults() {
        List<PontosVantagemConfigDTO> pontos = provider.getDefaultPontosVantagem();

        assertThat(pontos).hasSize(35);

        assertThat(pontos)
            .filteredOn(p -> p.nivel() == 1)
            .first()
            .extracting(PontosVantagemConfigDTO::pontos)
            .isEqualTo(6);

        assertThat(pontos)
            .filteredOn(p -> p.nivel() == 10)
            .first()
            .extracting(PontosVantagemConfigDTO::pontos)
            .isEqualTo(10);

        assertThat(pontos)
            .filteredOn(p -> p.nivel() == 30)
            .first()
            .extracting(PontosVantagemConfigDTO::pontos)
            .isEqualTo(15);
    }

    @Test
    @DisplayName("T5-03: Cabeça deve ter 75% da vida (BUG-DC-06 corrigido)")
    void cabecaDeveTer75PorcentoDeVida() {
        List<MembroCorpoConfigDTO> membros = provider.getDefaultMembrosCorpo();

        MembroCorpoConfigDTO cabeca = membros.stream()
            .filter(m -> m.getNome().equals("Cabeça"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Membro 'Cabeça' não encontrado"));

        assertThat(cabeca.getPorcentagemVida())
            .as("Cabeça deve ter porcentagem 0.75 (BUG-DC-06 corrigido)")
            .isEqualByComparingTo(new BigDecimal("0.75"));
    }

    @Test
    @DisplayName("T5-04: Índole deve ter exatamente 3 valores Klayrah (BUG-DC-07 corrigido)")
    void indoleDeveTerTresValores() {
        List<IndoleConfigDTO> indoles = provider.getDefaultIndoles();

        assertThat(indoles).hasSize(3);

        assertThat(indoles)
            .extracting(IndoleConfigDTO::getNome)
            .containsExactlyInAnyOrder("Bom", "Mau", "Neutro");
    }

    @Test
    @DisplayName("T5-05: Presença deve ter exatamente 4 valores éticos (BUG-DC-08 corrigido)")
    void presencaDeveTerQuatroValores() {
        List<PresencaConfigDTO> presencas = provider.getDefaultPresencas();

        assertThat(presencas).hasSize(4);

        assertThat(presencas)
            .extracting(PresencaConfigDTO::getNome)
            .containsExactlyInAnyOrder("Bom", "Leal", "Caótico", "Neutro");
    }

    @Test
    @DisplayName("T5-06: Gênero deve ter exatamente 3 valores (BUG-DC-09 corrigido)")
    void generoDeveTerTresValores() {
        List<GeneroConfigDTO> generos = provider.getDefaultGeneros();

        assertThat(generos).hasSize(3);

        assertThat(generos)
            .extracting(GeneroConfigDTO::getNome)
            .containsExactlyInAnyOrder("Masculino", "Feminino", "Outro");
    }

    @Test
    @DisplayName("T5-07: Classe 'Necromante' existe e não há 'Necromance' (DIV-01 corrigido)")
    void deveConterNecromante() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();

        assertThat(classes)
            .extracting(ClasseConfigDTO::getNome)
            .as("Não deve conter 'Necromance'")
            .doesNotContain("Necromance");

        assertThat(classes)
            .extracting(ClasseConfigDTO::getNome)
            .as("Deve conter 'Necromante'")
            .contains("Necromante");
    }

    @Test
    @DisplayName("T5-08: Membro 'Sangue' existe com 100% da vida (DIV-05 corrigido)")
    void sangueDeveExistirCom100PorCento() {
        List<MembroCorpoConfigDTO> membros = provider.getDefaultMembrosCorpo();

        MembroCorpoConfigDTO sangue = membros.stream()
            .filter(m -> m.getNome().equals("Sangue"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Membro 'Sangue' não encontrado (DIV-05 não corrigido)"));

        assertThat(sangue.getPorcentagemVida())
            .as("Sangue deve ter porcentagem 1.00 (DIV-05 corrigido)")
            .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("T5-09: Provider retorna 9 CategoriaVantagem defaults com nomes e cores")
    void deveRetornarOitoCategoriasVantagemDefaults() {
        List<CategoriaVantagemDTO> categorias = provider.getDefaultCategoriasVantagem();

        assertThat(categorias).hasSize(9);

        assertThat(categorias)
            .extracting(CategoriaVantagemDTO::nome)
            .containsExactlyInAnyOrder(
                "Treinamento Físico",
                "Treinamento Mental",
                "Ação",
                "Reação",
                "Vantagem de Atributo",
                "Vantagem Geral",
                "Vantagem Histórica",
                "Vantagem de Renascimento",
                "Vantagem Racial"
            );

        categorias.forEach(c -> {
            assertThat(c.cor())
                .as("Categoria '%s' deve ter cor hex", c.nome())
                .isNotBlank()
                .startsWith("#")
                .hasSize(7);
        });
    }

    @Test
    @DisplayName("T5-10: Siglas de BonusConfig são únicas e têm 2-5 caracteres")
    void siglasBonusSaoUnicasEValidas() {
        List<BonusConfigDTO> bonus = provider.getDefaultBonus();

        assertThat(bonus)
            .extracting(BonusConfigDTO::sigla)
            .doesNotHaveDuplicates();

        bonus.forEach(b -> {
            assertThat(b.sigla().length())
                .as("Sigla '%s' deve ter 2-5 caracteres", b.sigla())
                .isBetween(2, 5);
        });
    }

    @Test
    @DisplayName("T5-11: getDefaultVantagens() retorna exatamente 65 vantagens")
    void deveRetornarSessentaECincoVantagens() {
        var vantagens = provider.getDefaultVantagens();
        assertThat(vantagens).hasSize(65);
    }

    @Test
    @DisplayName("T5-12: siglas das vantagens são únicas, têm 2-5 caracteres e começam com V")
    void siglasVantagensDevemSerUnicasE2a5CharsComPrefixoV() {
        var vantagens = provider.getDefaultVantagens();
        var siglas = vantagens.stream().map(VantagemConfigDTO::getSigla).toList();
        assertThat(siglas).doesNotContainNull();
        siglas.forEach(s -> assertThat(s.length()).isBetween(2, 5));
        siglas.forEach(s -> assertThat(s).startsWith("V"));
        assertThat(siglas).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("T5-13: todos os INSOLITUS têm formulaCusto = 0")
    void insolitusDeverTerFormulaCustoZero() {
        var insolitus = provider.getDefaultVantagens().stream()
                .filter(v -> "INSOLITUS".equals(v.getTipoVantagem()))
                .toList();
        assertThat(insolitus).hasSize(18);
        insolitus.forEach(v -> assertThat(v.getFormulaCusto()).isEqualTo("0"));
    }

    @Test
    @DisplayName("T5-14: todas as categoriaNome existem em getDefaultCategoriasVantagem()")
    void categoriaNomeDeveExistirNasCategorias() {
        var categoriasNomes = provider.getDefaultCategoriasVantagem().stream()
                .map(CategoriaVantagemDTO::nome)
                .collect(Collectors.toSet());
        provider.getDefaultVantagens().forEach(v ->
                assertThat(categoriasNomes).contains(v.getCategoriaNome())
        );
    }

    @Test
    @DisplayName("T5-15: nenhuma formulaCusto usa custo_base")
    void formulaCustoNaoDeveTerCustoBase() {
        provider.getDefaultVantagens().forEach(v ->
                assertThat(v.getFormulaCusto()).doesNotContain("custo_base")
        );
    }

    @Test
    @DisplayName("T5-16: 7 atributos com abreviações únicas")
    void deveRetornarSeteAtributosComAbreviacoesUnicas() {
        var atributos = provider.getDefaultAtributos();
        assertThat(atributos).hasSize(7);
        var abreviacoes = atributos.stream().map(AtributoConfigDTO::getAbreviacao).toList();
        assertThat(abreviacoes).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("T5-17: 6 raças canônicas com nomes, ordem e descrições do CSV atual")
    void deveRetornarSeisRacasCanonicas() {
        var racas = provider.getDefaultRacas();

        assertThat(racas)
                .hasSize(6)
                .extracting(RacaConfigDTO::getNome, RacaConfigDTO::getOrdemExibicao)
                .containsExactly(
                        tuple("Humano", 1),
                        tuple("Karzarcryer", 2),
                        tuple("Ikarúz", 3),
                        tuple("Hankráz", 4),
                        tuple("Atlas", 5),
                        tuple("Anakarys", 6)
                );

        Map<String, String> descricoesPorRaca = racas.stream()
                .collect(Collectors.toMap(RacaConfigDTO::getNome, RacaConfigDTO::getDescricao));

        assertThat(descricoesPorRaca).containsEntry("Humano",
                "Raça versátil e adaptável, capaz de aprender qualquer arte ou ofício. Sua maior força é a adaptabilidade e a capacidade de superação. Não possuem vantagens raciais físicas marcantes, mas compensam com 5 vantagens especiais que refletem a resiliência e o potencial ilimitado da humanidade.");
        assertThat(descricoesPorRaca).containsEntry("Karzarcryer",
                "Descendentes de dragões do plano do fogo, os Karzarcryer possuem escamas ígnicas e sangue quente. Bônus: +8 Resistência (+24 VIG), -3 Percepção (-9 INTU). São conhecidos por seu temperamento explosivo (Antecedente: Falta de autocontrole) e pela Ignomia inicial 5, que representa sua reputação de destruição instintiva. Dominam o elemento fogo e possuem resistência sobrenatural ao calor.");
        assertThat(descricoesPorRaca).containsEntry("Ikarúz",
                "Raça de seres alados com afinidade à sabedoria celestial. Possuem asas funcionais e adaptação a diferentes altitudes. Bônus: +5 Sabedoria (+5 SAB), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Nanismo — apesar das asas grandiosas, seu corpo é pequeno e delicado, tornando-os vulneráveis em combate corpo a corpo.");
        assertThat(descricoesPorRaca).containsEntry("Hankráz",
                "Seres esguios de inteligência aguçada que habitam entre planos de existência paralelos. Seus piercings mágicos amplificam capacidades sobrenaturais. Bônus: +5 Inteligência (+5 INT), +3 Percepção (+9 INTU), -3 Resistência (-9 VIG). Antecedente: Baixo vigor — corpos frágeis que compensam com mente analítica e habilidades multidimensionais.");
        assertThat(descricoesPorRaca).containsEntry("Atlas",
                "Gigantes de força incomparável com quatro braços plenamente funcionais, os Atlas são a raça mais fisicamente poderosa de Klayrah. Bônus: +8 Força (+8 FOR), -3 Inteligência (-3 INT). Antecedente: É burro — já representado pela penalidade em INT. São guerreiros natos que usam seus dois pares de braços para empunhar múltiplas armas ou escudos simultaneamente, mas carecem de sofisticação intelectual. Vantagens raciais: Membros Adicionais: Braços (que fundamenta Ambidestria e Ataque Adicional nativos), Capacidade de Força Máxima inata.");
        assertThat(descricoesPorRaca).containsEntry("Anakarys",
                "Raça ágil de predadores naturais com garras, presas e instintos aguçados. Bônus: +3 Agilidade (+3 AGI), +2 Percepção (+6 INTU). Possuem armas naturais aprimoradas, formas únicas de deslocamento e um ataque adicional racial com restrições específicas de uso.");
    }

    @Test
    @DisplayName("T5-18: 36 níveis (0-35) com XP crescente")
    void deveRetornar36NiveisComXpCrescente() {
        var niveis = provider.getDefaultNiveis();
        assertThat(niveis).hasSize(36);
        for (int i = 1; i < niveis.size(); i++) {
            assertThat(niveis.get(i).getExperienciaNecessaria())
                    .isGreaterThanOrEqualTo(niveis.get(i - 1).getExperienciaNecessaria());
        }
    }

    @Test
    @DisplayName("T5-19: 40 itens com raridade e tipo existentes")
    void deveRetornar40ItensComRaridadeETipoValidos() {
        var itens = provider.getDefaultItens();
        assertThat(itens).hasSize(40);
        var raridadeNomes = provider.getDefaultRaridades().stream()
                .map(RaridadeItemConfigDefault::nome).collect(Collectors.toSet());
        var tipoNomes = provider.getDefaultTipos().stream()
                .map(TipoItemConfigDefault::nome).collect(Collectors.toSet());
        itens.forEach(item -> {
            assertThat(raridadeNomes).contains(item.raridadeNome());
            assertThat(tipoNomes).contains(item.tipoNome());
        });
    }

    @Test
    @DisplayName("T5-20: Mapa de bônus raciais segue os valores canônicos do CSV")
    void mapaBonusRaciaisDeveConterValoresCanonicos() {
        var bonusMap = provider.getDefaultBonusRaciais();

        assertThat(bonusMap).containsOnlyKeys(
                "Humano", "Karzarcryer", "Ikarúz", "Hankráz", "Atlas", "Anakarys"
        );
        assertThat(bonusMap.get("Humano")).isEmpty();

        assertThat(bonusMap.get("Karzarcryer"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo, BonusAtributoDTO::getBonus)
                .containsExactlyInAnyOrder(tuple("VIG", 24), tuple("INTU", -9));

        assertThat(bonusMap.get("Ikarúz"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo, BonusAtributoDTO::getBonus)
                .containsExactlyInAnyOrder(tuple("SAB", 5), tuple("INTU", 9), tuple("VIG", -9));

        assertThat(bonusMap.get("Hankráz"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo, BonusAtributoDTO::getBonus)
                .containsExactlyInAnyOrder(tuple("INT", 5), tuple("INTU", 9), tuple("VIG", -9));

        assertThat(bonusMap.get("Anakarys"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo, BonusAtributoDTO::getBonus)
                .containsExactlyInAnyOrder(tuple("AGI", 3), tuple("INTU", 6));

        assertThat(bonusMap.get("Atlas"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo, BonusAtributoDTO::getBonus)
                .containsExactlyInAnyOrder(tuple("FOR", 8), tuple("INT", -3));
    }

    @Test
    @DisplayName("T3-01: Pontos raciais seguem os marcos canônicos e 16c permanece sem restrições")
    void racasDevemTerPontosCanonicosESemRestricoesDeClassePorPadrao() {
        var racas = provider.getDefaultRacas();

        assertThat(racas)
                .allSatisfy(raca -> assertThat(raca.getClassesPermitidas())
                        .as("16c vazio deve manter classes permitidas vazias para %s", raca.getNome())
                        .isEmpty());

        assertThat(racas.stream()
                .collect(Collectors.toMap(RacaConfigDTO::getNome, RacaConfigDTO::getPontosConfig)))
                .containsEntry("Karzarcryer", List.of())
                .containsEntry("Atlas", List.of())
                .satisfies(mapa -> {
                    assertThat(mapa.get("Humano"))
                            .extracting(PontosNivelConfigDTO::nivel, PontosNivelConfigDTO::pontosAtributo, PontosNivelConfigDTO::pontosVantagem)
                            .containsExactly(
                                    tuple(1, 0, 1), tuple(5, 0, 1), tuple(10, 0, 1), tuple(15, 0, 1),
                                    tuple(20, 0, 1), tuple(25, 0, 1), tuple(30, 0, 1), tuple(35, 0, 1)
                            );
                    assertThat(mapa.get("Ikarúz"))
                            .extracting(PontosNivelConfigDTO::nivel, PontosNivelConfigDTO::pontosAtributo, PontosNivelConfigDTO::pontosVantagem)
                            .containsExactly(
                                    tuple(1, 0, 1), tuple(5, 0, 1), tuple(10, 0, 1), tuple(15, 0, 1),
                                    tuple(20, 0, 1), tuple(25, 0, 1), tuple(30, 0, 1), tuple(35, 0, 1)
                            );
                    assertThat(mapa.get("Hankráz"))
                            .extracting(PontosNivelConfigDTO::nivel, PontosNivelConfigDTO::pontosAtributo, PontosNivelConfigDTO::pontosVantagem)
                            .containsExactly(
                                    tuple(1, 0, 1), tuple(5, 0, 1), tuple(10, 0, 1), tuple(15, 0, 1),
                                    tuple(20, 0, 1), tuple(25, 0, 1), tuple(30, 0, 1), tuple(35, 0, 1)
                            );
                    assertThat(mapa.get("Anakarys"))
                            .extracting(PontosNivelConfigDTO::nivel, PontosNivelConfigDTO::pontosAtributo, PontosNivelConfigDTO::pontosVantagem)
                            .containsExactly(
                                    tuple(1, 0, 1), tuple(5, 0, 1), tuple(10, 0, 1), tuple(15, 0, 1),
                                    tuple(20, 0, 1), tuple(25, 0, 1), tuple(30, 0, 1), tuple(35, 0, 1)
                            );
                });
    }

    @Test
    @DisplayName("T3-02: Vantagens raciais predefinidas seguem o CSV canônico, incluindo Atlas com VMAB")
    void racasDevemTerVantagensPredefinidasCanonicas() {
        var vantagensPorRaca = provider.getDefaultRacas().stream()
                .collect(Collectors.toMap(RacaConfigDTO::getNome, RacaConfigDTO::getVantagemPreDefinidaDefaults));

        assertThat(vantagensPorRaca).allSatisfy((raca, vantagens) ->
                assertThat(vantagens)
                        .as("Todas as vantagens raciais predefinidas de %s devem iniciar no nível 1", raca)
                        .extracting(RacaVantagemPreDefinidaDefault::nivel)
                        .containsOnly(1));

        assertThat(vantagensPorRaca.get("Humano"))
                .hasSize(5)
                .extracting(RacaVantagemPreDefinidaDefault::vantagemNome, RacaVantagemPreDefinidaDefault::vantagemSigla)
                .containsExactly(
                        tuple("Adaptabilidade Humana", "VAHU"),
                        tuple("Resiliência Humana", "VRHU"),
                        tuple("Versatilidade Humana", "VVHU"),
                        tuple("Espírito Inabalável", "VEIN"),
                        tuple("Legado de Civilização", "VLCI"));

        assertThat(vantagensPorRaca.get("Karzarcryer"))
                .hasSize(3)
                .extracting(RacaVantagemPreDefinidaDefault::vantagemNome, RacaVantagemPreDefinidaDefault::vantagemSigla)
                .containsExactly(
                        tuple("Elemento Natural: Fogo", "VENF"),
                        tuple("Imunidade Elemental: Fogo", "VIEF"),
                        tuple("Estômago de Dragão", "VESD"));

        assertThat(vantagensPorRaca.get("Ikarúz"))
                .hasSize(3)
                .extracting(RacaVantagemPreDefinidaDefault::vantagemNome, RacaVantagemPreDefinidaDefault::vantagemSigla)
                .containsExactly(
                        tuple("Membro Adicional: Asas", "VASA"),
                        tuple("Adaptação Atmosférica", "VADA"),
                        tuple("Combate Alado", "VCAL"));

        assertThat(vantagensPorRaca.get("Hankráz"))
                .hasSize(3)
                .extracting(RacaVantagemPreDefinidaDefault::vantagemNome, RacaVantagemPreDefinidaDefault::vantagemSigla)
                .containsExactly(
                        tuple("Piercings Raciais", "VPIR"),
                        tuple("Corpo Esguio", "VCEG"),
                        tuple("Vagante entre Mundos", "VVEM"));

        assertThat(vantagensPorRaca.get("Atlas"))
                .hasSize(4)
                .extracting(RacaVantagemPreDefinidaDefault::vantagemNome, RacaVantagemPreDefinidaDefault::vantagemSigla)
                .containsExactly(
                        tuple("Membros Adicionais: Braços", "VMAB"),
                        tuple("Capacidade de Força Máxima", "VCFM"),
                        tuple("Ambidestria", "VAMB"),
                        tuple("Ataque Adicional", "VAA")
                );

        assertThat(vantagensPorRaca.get("Anakarys"))
                .hasSize(3)
                .extracting(RacaVantagemPreDefinidaDefault::vantagemNome, RacaVantagemPreDefinidaDefault::vantagemSigla)
                .containsExactly(
                        tuple("Armas Naturais Aprimoradas", "VANA"),
                        tuple("Deslocamento Especial", "VDES"),
                        tuple("Ataque Adicional Racial", "VAAR"));
    }

    @Test
    @DisplayName("T5-21: 5 limitadores cobrem faixas de nível sem sobreposição ou gap")
    void limitadoresDevemCobrir5FaixasComCoerencia() {
        var limitadores = provider.getDefaultLimitadores();

        assertThat(limitadores).hasSize(5);

        assertThat(limitadores)
                .filteredOn(l -> l.getNivelInicio() == 0)
                .first()
                .extracting(LimitadorConfigDTO::getLimiteAtributo)
                .isEqualTo(10);

        assertThat(limitadores)
                .filteredOn(l -> l.getNivelInicio() == 31)
                .first()
                .extracting(LimitadorConfigDTO::getLimiteAtributo)
                .isEqualTo(120);
    }

    @Test
    @DisplayName("T5-22: 7 dados de prospecção com faces crescentes")
    void deveRetornarSeisDadosComFacesCrescentes() {
        var prospeccoes = provider.getDefaultProspeccoes();

        assertThat(prospeccoes).hasSize(7);
        assertThat(prospeccoes)
                .extracting(ProspeccaoConfigDTO::getNome)
                .containsExactly("d3", "d4", "d6", "d8", "d10", "d12", "d20");

        var faces = prospeccoes.stream().map(ProspeccaoConfigDTO::getNumLados).toList();
        for (int i = 1; i < faces.size(); i++) {
            assertThat(faces.get(i)).isGreaterThan(faces.get(i - 1));
        }
    }

    @Test
    @DisplayName("T5-23: 24 aptidões — 12 FISICA e 12 MENTAL sem outras categorias")
    void aptidoesDevemTer12FisicasE12Mentais() {
        var aptidoes = provider.getDefaultAptidoes();

        assertThat(aptidoes).hasSize(24);
        assertThat(aptidoes).filteredOn(a -> "FISICA".equals(a.getTipo())).hasSize(12);
        assertThat(aptidoes).filteredOn(a -> "MENTAL".equals(a.getTipo())).hasSize(12);

        assertThat(aptidoes)
                .extracting(AptidaoConfigDTO::getTipo)
                .containsOnly("FISICA", "MENTAL");
    }

    @Test
    @DisplayName("T2-01: Provider retorna exatamente 12 classes com nomes, descrições e ordem canônica")
    void deveRetornarDozeClassesComDadosCanonicos() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();

        assertThat(classes)
            .as("Deve retornar exatamente 12 classes conforme CSV")
            .hasSize(12);

        assertThat(classes)
            .extracting(ClasseConfigDTO::getNome)
            .containsExactly(
                "Guerreiro", "Arqueiro", "Monge", "Berserker", "Assassino",
                "Fauno (Herdeiro)", "Mago", "Feiticeiro", "Necromante",
                "Sacerdote", "Ladrão", "Negociante"
            );

        // Verificar que todas têm descrição e ordem de exibição
        classes.forEach(c -> {
            assertThat(c.getDescricao())
                .as("Classe '%s' deve ter descrição", c.getNome())
                .isNotBlank();
            assertThat(c.getOrdemExibicao())
                .as("Classe '%s' deve ter ordem de exibição", c.getNome())
                .isNotNull()
                .isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("T2-02: Guerreiro tem 3 bônus derivados corretos (BBA=2, Bloqueio=2, Defesa=1)")
    void guerreiroDeveTerTresBonusDerivados() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();
        ClasseConfigDTO guerreiro = classes.stream()
            .filter(c -> "Guerreiro".equals(c.getNome()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Guerreiro não encontrado"));

        List<ClasseBonusDefault> bonus = guerreiro.getBonusDefaults();
        assertThat(bonus)
            .as("Guerreiro deve ter 3 bônus derivados")
            .hasSize(3);

        assertThat(bonus)
            .extracting(ClasseBonusDefault::bonusNome)
            .containsExactlyInAnyOrder("B.B.A", "Bloqueio", "Defesa");

        // Verificar valores específicos
        assertThat(bonus.stream().filter(b -> "B.B.A".equals(b.bonusNome())).findFirst())
            .isPresent()
            .get()
            .extracting(ClasseBonusDefault::valorPorNivel)
            .isEqualTo(new BigDecimal("2"));

        assertThat(bonus.stream().filter(b -> "Bloqueio".equals(b.bonusNome())).findFirst())
            .isPresent()
            .get()
            .extracting(ClasseBonusDefault::valorPorNivel)
            .isEqualTo(new BigDecimal("2"));

        assertThat(bonus.stream().filter(b -> "Defesa".equals(b.bonusNome())).findFirst())
            .isPresent()
            .get()
            .extracting(ClasseBonusDefault::valorPorNivel)
            .isEqualTo(new BigDecimal("1"));
    }

    @Test
    @DisplayName("T2-03: Mago tem 4 aptidões bônus corretas (Investigar=3, Idiomas=3, Observação=2, Prontidão=2)")
    void magoDeveTerQuatroAptidoesBonus() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();
        ClasseConfigDTO mago = classes.stream()
            .filter(c -> "Mago".equals(c.getNome()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Mago não encontrado"));

        List<ClasseAptidaoBonusDefault> aptidaoBonus = mago.getAptidaoBonusDefaults();
        assertThat(aptidaoBonus)
            .as("Mago deve ter 4 aptidões bônus")
            .hasSize(4);

        assertThat(aptidaoBonus)
            .extracting(ClasseAptidaoBonusDefault::aptidaoNome)
            .containsExactlyInAnyOrder("Investigar", "Idiomas", "Observação", "Prontidão");

        // Verificar valores específicos
        assertThat(aptidaoBonus.stream().filter(a -> "Investigar".equals(a.aptidaoNome())).findFirst())
            .isPresent()
            .get()
            .extracting(ClasseAptidaoBonusDefault::bonus)
            .isEqualTo(3);

        assertThat(aptidaoBonus.stream().filter(a -> "Idiomas".equals(a.aptidaoNome())).findFirst())
            .isPresent()
            .get()
            .extracting(ClasseAptidaoBonusDefault::bonus)
            .isEqualTo(3);
    }

    @Test
    @DisplayName("T2-04: Todas as classes têm pontos config (ClassePontosConfig)")
    void todasClassesDevemTerPontosConfig() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();

        classes.forEach(c -> {
            assertThat(c.getPontosConfig())
                .as("Classe '%s' deve ter pontos config", c.getNome())
                .isNotNull()
                .isNotEmpty();
        });

        // Guerreiro deve ter 35 níveis com bônus (1 atributo por nível)
        ClasseConfigDTO guerreiro = classes.stream()
            .filter(c -> "Guerreiro".equals(c.getNome()))
            .findFirst()
            .orElseThrow();
        assertThat(guerreiro.getPontosConfig()).hasSize(35);
        assertThat(guerreiro.getPontosConfig().get(0).pontosAtributo()).isEqualTo(1);
        assertThat(guerreiro.getPontosConfig().get(0).pontosVantagem()).isEqualTo(0);

        // Arqueiro deve ter 7 níveis com bônus (marcos 5, 10, 15, 20, 25, 30, 35)
        ClasseConfigDTO arqueiro = classes.stream()
            .filter(c -> "Arqueiro".equals(c.getNome()))
            .findFirst()
            .orElseThrow();
        assertThat(arqueiro.getPontosConfig()).hasSize(7);
        assertThat(arqueiro.getPontosConfig().get(0).nivel()).isEqualTo(5);
    }

    @Test
    @DisplayName("T2-05: Assassino tem 3 vantagens predefinidas corretas")
    void assassinoDeveTerTresVantagensPreDefinidas() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();
        ClasseConfigDTO assassino = classes.stream()
            .filter(c -> "Assassino".equals(c.getNome()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Assassino não encontrado"));

        List<ClasseVantagemPreDefinidaDefault> vantagens = assassino.getVantagemPreDefinidaDefaults();
        assertThat(vantagens)
            .as("Assassino deve ter 3 vantagens predefinidas")
            .hasSize(3);

        assertThat(vantagens)
            .extracting(ClasseVantagemPreDefinidaDefault::vantagemNome)
            .containsExactlyInAnyOrder(
                "Treinamento em Combate Ofensivo",
                "Treinamento em Combate Evasivo",
                "Saque Rápido"
            );

        // Verificar nível inicial
        assertThat(vantagens.stream()
            .filter(v -> "Treinamento em Combate Ofensivo".equals(v.vantagemNome()))
            .findFirst())
            .isPresent()
            .get()
            .extracting(ClasseVantagemPreDefinidaDefault::nivel)
            .isEqualTo(1);

        assertThat(vantagens.stream()
            .filter(v -> "Saque Rápido".equals(v.vantagemNome()))
            .findFirst())
            .isPresent()
            .get()
            .extracting(ClasseVantagemPreDefinidaDefault::nivel)
            .isEqualTo(1);
    }

    @Test
    @DisplayName("T2-06: Todas as 12 classes têm pelo menos 1 bônus derivado")
    void todasClassesDevemTerPeloMenosUmBonusDerivado() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();

        classes.forEach(c -> {
            assertThat(c.getBonusDefaults())
                .as("Classe '%s' deve ter pelo menos 1 bônus derivado", c.getNome())
                .isNotEmpty();
        });
    }

    @Test
    @DisplayName("T2-07: Todas as 12 classes têm pelo menos 1 aptidão bônus")
    void todasClassesDevemTerPeloMenosUmaAptidaoBonus() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();

        classes.forEach(c -> {
            assertThat(c.getAptidaoBonusDefaults())
                .as("Classe '%s' deve ter pelo menos 1 aptidão bônus", c.getNome())
                .isNotEmpty();
        });
    }

    @Test
    @DisplayName("T2-08: Todas as 12 classes têm pelo menos 1 vantagem predefinida")
    void todasClassesDevemTerPeloMenosUmaVantagemPreDefinida() {
        List<ClasseConfigDTO> classes = provider.getDefaultClasses();

        classes.forEach(c -> {
            assertThat(c.getVantagemPreDefinidaDefaults())
                .as("Classe '%s' deve ter pelo menos 1 vantagem predefinida", c.getNome())
                .isNotEmpty();
        });
    }

    @Test
    @DisplayName("T2-09: Cada classe replica integralmente bônus, aptidões, pontos e vantagens predefinidas do CSV")
    void classesDevemReplicarIntegralmenteAsSubestruturasCanonicas() {
        Map<String, ClasseConfigDTO> classesPorNome = provider.getDefaultClasses().stream()
                .collect(Collectors.toMap(ClasseConfigDTO::getNome, classe -> classe));

        Map<String, ClasseDefaultsEsperados> esperados = Map.ofEntries(
                Map.entry("Guerreiro", new ClasseDefaultsEsperados(
                        List.of("B.B.A|2|1", "Bloqueio|2|2", "Defesa|1|3"),
                        List.of("Guarda|3|1", "Aparar|3|2", "Atletismo|2|3", "Resistência|2|4"),
                        pontosPorNivelEsperados(1, 0),
                        List.of("Treinamento em Combate Ofensivo|1", "Treinamento em Combate Defensivo|1")
                )),
                Map.entry("Arqueiro", new ClasseDefaultsEsperados(
                        List.of("B.B.A|2|1", "Reflexo|2|2", "Percepção|1|3"),
                        List.of("Perseguição|3|1", "Observação|3|2", "Atletismo|2|3", "Resvalar|2|4"),
                        pontosEmMarcosEsperados(1, 0),
                        List.of("Treinamento em Combate Ofensivo|1", "Treinamento em Combate Evasivo|1", "Memória Fotográfica|1")
                )),
                Map.entry("Monge", new ClasseDefaultsEsperados(
                        List.of("B.B.A|1|1", "Reflexo|2|2", "Esquiva|2|3"),
                        List.of("Auto Controle|4|1", "Arte da Fuga|3|2", "Atletismo|3|3", "Acrobacia|2|4"),
                        pontosEmMarcosEsperados(1, 0),
                        List.of("Treinamento em Combate Evasivo|2", "Treinamento em Combate Ofensivo|1", "Concentração|1")
                )),
                Map.entry("Berserker", new ClasseDefaultsEsperados(
                        List.of("B.B.A|3|1", "Iniciativa|1|2", "Bloqueio|1|3"),
                        List.of("Atletismo|4|1", "Resistência|3|2", "Resvalar|2|3", "Perseguição|2|4"),
                        pontosPorNivelEsperados(1, 0),
                        List.of("Treinamento em Combate Ofensivo|2", "Saúde de Ferro|1")
                )),
                Map.entry("Assassino", new ClasseDefaultsEsperados(
                        List.of("B.B.A|2|1", "Reflexo|2|2", "Esquiva|1|3"),
                        List.of("Furtividade|4|1", "Prestidigitação|3|2", "Arte da Fuga|2|3", "Resvalar|2|4", "Blefar|1|5"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento em Combate Ofensivo|1", "Treinamento em Combate Evasivo|1", "Saque Rápido|1")
                )),
                Map.entry("Fauno (Herdeiro)", new ClasseDefaultsEsperados(
                        List.of("B.B.M|2|1", "Percepção|2|2", "Raciocínio|1|3"),
                        List.of("Sentir Motivação|3|1", "Observação|3|2", "Auto Controle|2|3", "Prontidão|2|4"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento Mágico|1", "Treinamento em Percepção Mágica|1", "Herança|1")
                )),
                Map.entry("Mago", new ClasseDefaultsEsperados(
                        List.of("B.B.M|3|1", "Raciocínio|2|2", "Percepção|1|3"),
                        List.of("Investigar|3|1", "Idiomas|3|2", "Observação|2|3", "Prontidão|2|4"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento Mágico|2", "Treinamento Lógico|1", "Concentração|1")
                )),
                Map.entry("Feiticeiro", new ClasseDefaultsEsperados(
                        List.of("B.B.M|2|1", "Iniciativa|2|2", "Reflexo|1|3"),
                        List.of("Prontidão|3|1", "Auto Controle|2|2", "Observação|2|3", "Sentir Motivação|2|4"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento Mágico|1", "Treinamento em Percepção Mágica|1", "Concentração|1")
                )),
                Map.entry("Necromante", new ClasseDefaultsEsperados(
                        List.of("B.B.M|2|1", "Raciocínio|2|2", "Percepção|1|3"),
                        List.of("Investigar|3|1", "Falsificar|3|2", "Prontidão|2|3", "Sobrevivência|2|4"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento Mágico|1", "Treinamento Lógico|1", "Vínculo com Organização|1")
                )),
                Map.entry("Sacerdote", new ClasseDefaultsEsperados(
                        List.of("B.B.M|2|1", "Defesa|2|2", "Raciocínio|1|3"),
                        List.of("Diplomacia|3|1", "Auto Controle|3|2", "Sentir Motivação|3|3", "Observação|2|4"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento Mágico|1", "Treinamento em Manipulação|1", "Saúde de Ferro|1")
                )),
                Map.entry("Ladrão", new ClasseDefaultsEsperados(
                        List.of("Esquiva|2|1", "Reflexo|2|2", "Iniciativa|1|3"),
                        List.of("Furtividade|3|1", "Prestidigitação|3|2", "Arte da Fuga|3|3", "Operação de Mecanismos|2|4", "Blefar|2|5"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento em Combate Evasivo|1", "Saque Rápido|2", "Ofícios|1")
                )),
                Map.entry("Negociante", new ClasseDefaultsEsperados(
                        List.of("Raciocínio|2|1", "Percepção|2|2", "Iniciativa|1|3"),
                        List.of("Diplomacia|4|1", "Blefar|3|2", "Sentir Motivação|3|3", "Atuação|2|4"),
                        pontosPorNivelEsperados(0, 1),
                        List.of("Treinamento em Manipulação|1", "Ofícios|2", "Vínculo com Organização|1")
                ))
        );

        assertThat(classesPorNome.keySet()).containsExactlyInAnyOrderElementsOf(esperados.keySet());

        esperados.forEach((nomeClasse, esperado) -> {
            ClasseConfigDTO classe = classesPorNome.get(nomeClasse);

            assertThat(classe.getBonusDefaults().stream().map(this::chaveBonusClasse).toList())
                    .as("Bônus derivados da classe '%s' devem replicar o CSV 15b", nomeClasse)
                    .containsExactlyElementsOf(esperado.bonus());

            assertThat(classe.getAptidaoBonusDefaults().stream().map(this::chaveAptidaoBonusClasse).toList())
                    .as("Aptidões bônus da classe '%s' devem replicar o CSV 15c", nomeClasse)
                    .containsExactlyElementsOf(esperado.aptidoes());

            assertThat(classe.getPontosConfig().stream().map(this::chavePontosClasse).toList())
                    .as("Pontos da classe '%s' devem replicar o CSV 15d", nomeClasse)
                    .containsExactlyElementsOf(esperado.pontos());

            assertThat(classe.getVantagemPreDefinidaDefaults().stream().map(this::chaveVantagemClasse).toList())
                    .as("Vantagens predefinidas da classe '%s' devem replicar o CSV 15e", nomeClasse)
                    .containsExactlyElementsOf(esperado.vantagens());
        });
    }

    private String chaveBonusClasse(ClasseBonusDefault bonus) {
        return bonus.bonusNome() + "|" + bonus.valorPorNivel().stripTrailingZeros().toPlainString() + "|" + bonus.ordemExibicao();
    }

    private String chaveAptidaoBonusClasse(ClasseAptidaoBonusDefault aptidaoBonus) {
        return aptidaoBonus.aptidaoNome() + "|" + aptidaoBonus.bonus() + "|" + aptidaoBonus.ordemExibicao();
    }

    private String chavePontosClasse(PontosNivelConfigDTO pontos) {
        return pontos.nivel() + "|" + pontos.pontosAtributo() + "|" + pontos.pontosVantagem();
    }

    private String chaveVantagemClasse(ClasseVantagemPreDefinidaDefault vantagem) {
        return vantagem.vantagemNome() + "|" + vantagem.nivel();
    }

    private List<String> pontosPorNivelEsperados(int pontosAtributo, int pontosVantagem) {
        return IntStream.rangeClosed(1, 35)
                .mapToObj(nivel -> nivel + "|" + pontosAtributo + "|" + pontosVantagem)
                .toList();
    }

    private List<String> pontosEmMarcosEsperados(int pontosAtributo, int pontosVantagem) {
        return List.of(5, 10, 15, 20, 25, 30, 35).stream()
                .map(nivel -> nivel + "|" + pontosAtributo + "|" + pontosVantagem)
                .toList();
    }

    private record ClasseDefaultsEsperados(
            List<String> bonus,
            List<String> aptidoes,
            List<String> pontos,
            List<String> vantagens
    ) {}
}
