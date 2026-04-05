package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultGameConfigProviderImpl - Testes Unitários")
class DefaultGameConfigProviderImplTest {

    private DefaultGameConfigProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultGameConfigProviderImpl();
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
    @DisplayName("T5-02: Provider retorna 8 PontosVantagemConfig defaults com valores corretos")
    void deveRetornarOitoPontosVantagemDefaults() {
        List<PontosVantagemConfigDTO> pontos = provider.getDefaultPontosVantagem();

        assertThat(pontos).hasSize(8);

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
    @DisplayName("T5-09: Provider retorna 8 CategoriaVantagem defaults com nomes e cores")
    void deveRetornarOitoCategoriasVantagemDefaults() {
        List<CategoriaVantagemDTO> categorias = provider.getDefaultCategoriasVantagem();

        assertThat(categorias).hasSize(8);

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
                "Vantagem de Renascimento"
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
}
