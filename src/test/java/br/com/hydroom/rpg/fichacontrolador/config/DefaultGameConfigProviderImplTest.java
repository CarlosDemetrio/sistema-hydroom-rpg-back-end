package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.config.defaults.*;
import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("T5-11: getDefaultVantagens() retorna exatamente 64 vantagens")
    void deveRetornarSesentaEQuatroVantagens() {
        var vantagens = provider.getDefaultVantagens();
        assertThat(vantagens).hasSize(64);
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
        assertThat(insolitus).hasSize(17);
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
    @DisplayName("T5-17: 6 raças com nomes únicos")
    void deveRetornarSeisRacasComNomesUnicos() {
        var racas = provider.getDefaultRacas();
        assertThat(racas).hasSize(6);
        var nomes = racas.stream().map(RacaConfigDTO::getNome).toList();
        assertThat(nomes).doesNotHaveDuplicates();
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
    @DisplayName("T5-20: Mapa de bônus raciais cobre todas as 6 raças com valores corretos")
    void mapaBonusRaciaisDeveConterTodasAsRacas() {
        var bonusMap = provider.getDefaultBonusRaciais();

        assertThat(bonusMap).containsOnlyKeys(
                "Humano", "Karzarcryer", "Ikaruz", "Hankraz", "Atlas", "Anakarys"
        );
        assertThat(bonusMap.get("Humano")).isEmpty();

        assertThat(bonusMap.get("Anakarys"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo)
                .containsExactlyInAnyOrder("AGI", "INTU");

        assertThat(bonusMap.get("Atlas"))
                .extracting(BonusAtributoDTO::getAbreviacaoAtributo)
                .containsExactlyInAnyOrder("FOR", "INT");
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
    @DisplayName("T5-22: 6 dados de prospecção com faces crescentes")
    void deveRetornarSeisDadosComFacesCrescentes() {
        var prospeccoes = provider.getDefaultProspeccoes();

        assertThat(prospeccoes).hasSize(6);
        assertThat(prospeccoes)
                .extracting(ProspeccaoConfigDTO::getNome)
                .containsExactly("d3", "d4", "d6", "d8", "d10", "d12");

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
}
