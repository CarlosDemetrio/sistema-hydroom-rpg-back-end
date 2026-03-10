package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FichaCalculationService - Testes Unitários")
class FichaCalculationServiceTest {

    @Mock
    private FormulaEvaluatorService formulaEvaluatorService;

    @InjectMocks
    private FichaCalculationService calculationService;

    private Ficha ficha;
    private AtributoConfig atributoConfig;
    private BonusConfig bonusConfig;

    @BeforeEach
    void setUp() {
        ficha = Ficha.builder()
                .id(1L)
                .nivel(5)
                .xp(1000L)
                .renascimentos(1)
                .build();

        atributoConfig = AtributoConfig.builder()
                .id(1L)
                .nome("Vigor")
                .abreviacao("VIG")
                .formulaImpeto("total * 2")
                .build();

        bonusConfig = BonusConfig.builder()
                .id(1L)
                .nome("Bônus de Batalha")
                .sigla("BBA")
                .formulaBase("VIG + FOR")
                .build();
    }

    // ==================== ATRIBUTOS ====================

    @Test
    @DisplayName("calcularTotalAtributo deve somar base + nivel + outros")
    void calcularTotalAtributo_deveSomarBaseNivelOutros() {
        // Arrange
        FichaAtributo atributo = FichaAtributo.builder()
                .base(3).nivel(2).outros(1).total(0).build();

        // Act
        int resultado = calculationService.calcularTotalAtributo(atributo);

        // Assert
        assertThat(resultado).isEqualTo(6);
        assertThat(atributo.getTotal()).isEqualTo(6);
    }

    @Test
    @DisplayName("calcularTotalAtributo deve tratar campos null como zero")
    void calcularTotalAtributo_deveTratar_null_como_zero() {
        // Arrange
        FichaAtributo atributo = FichaAtributo.builder()
                .base(5).nivel(null).outros(null).total(0).build();

        // Act
        int resultado = calculationService.calcularTotalAtributo(atributo);

        // Assert
        assertThat(resultado).isEqualTo(5);
    }

    @Test
    @DisplayName("calcularImpeto deve usar formulaEvaluatorService quando fórmula está presente")
    void calcularImpeto_deveUsarFormula_quandoPresente() {
        // Arrange
        FichaAtributo atributo = FichaAtributo.builder().base(5).nivel(0).outros(0).total(5).build();
        when(formulaEvaluatorService.calcularImpeto("total * 2", 5)).thenReturn(10.0);

        // Act
        double resultado = calculationService.calcularImpeto(atributo, atributoConfig);

        // Assert
        assertThat(resultado).isEqualTo(10.0);
        assertThat(atributo.getImpeto()).isEqualTo(10.0);
        verify(formulaEvaluatorService).calcularImpeto("total * 2", 5);
    }

    @Test
    @DisplayName("calcularImpeto deve retornar 0.0 quando fórmula é nula")
    void calcularImpeto_deveRetornarZero_quandoFormulaNula() {
        // Arrange
        FichaAtributo atributo = FichaAtributo.builder().base(5).nivel(0).outros(0).total(5).build();
        AtributoConfig configSemFormula = AtributoConfig.builder()
                .id(2L).nome("Força").abreviacao("FOR").formulaImpeto(null).build();

        // Act
        double resultado = calculationService.calcularImpeto(atributo, configSemFormula);

        // Assert
        assertThat(resultado).isEqualTo(0.0);
        assertThat(atributo.getImpeto()).isEqualTo(0.0);
        verifyNoInteractions(formulaEvaluatorService);
    }

    @Test
    @DisplayName("calcularImpeto deve retornar 0.0 quando fórmula é em branco")
    void calcularImpeto_deveRetornarZero_quandoFormulaEmBranco() {
        // Arrange
        FichaAtributo atributo = FichaAtributo.builder().base(5).nivel(0).outros(0).total(5).build();
        AtributoConfig configFormulaEmBranco = AtributoConfig.builder()
                .id(2L).nome("Força").abreviacao("FOR").formulaImpeto("   ").build();

        // Act
        double resultado = calculationService.calcularImpeto(atributo, configFormulaEmBranco);

        // Assert
        assertThat(resultado).isEqualTo(0.0);
        verifyNoInteractions(formulaEvaluatorService);
    }

    @Test
    @DisplayName("recalcularAtributos deve recalcular total e ímpeto de todos")
    void recalcularAtributos_deveRecalcularTodos() {
        // Arrange
        FichaAtributo a1 = FichaAtributo.builder()
                .base(3).nivel(2).outros(0).total(0).atributoConfig(atributoConfig).build();
        FichaAtributo a2 = FichaAtributo.builder()
                .base(1).nivel(1).outros(0).total(0)
                .atributoConfig(AtributoConfig.builder().id(2L).nome("Força").abreviacao("FOR").build()).build();

        when(formulaEvaluatorService.calcularImpeto("total * 2", 5)).thenReturn(10.0);

        // Act
        calculationService.recalcularAtributos(List.of(a1, a2));

        // Assert
        assertThat(a1.getTotal()).isEqualTo(5);
        assertThat(a1.getImpeto()).isEqualTo(10.0);
        assertThat(a2.getTotal()).isEqualTo(2);
        assertThat(a2.getImpeto()).isEqualTo(0.0); // sem fórmula
    }

    // ==================== BÔNUS ====================

    @Test
    @DisplayName("buildVariaveisAtributos deve mapear abreviação → total")
    void buildVariaveisAtributos_deveMapearCorretamente() {
        // Arrange
        FichaAtributo vig = FichaAtributo.builder().total(8)
                .atributoConfig(AtributoConfig.builder().abreviacao("VIG").build()).build();
        FichaAtributo forAtributo = FichaAtributo.builder().total(6)
                .atributoConfig(AtributoConfig.builder().abreviacao("FOR").build()).build();

        // Act
        Map<String, Integer> variaveis = calculationService.buildVariaveisAtributos(List.of(vig, forAtributo));

        // Assert
        assertThat(variaveis).containsEntry("VIG", 8).containsEntry("FOR", 6);
    }

    @Test
    @DisplayName("calcularBaseBonus deve usar fórmula quando presente")
    void calcularBaseBonus_deveUsarFormula_quandoPresente() {
        // Arrange
        FichaBonus fichaBonus = FichaBonus.builder().base(0).total(0).build();
        Map<String, Integer> variaveis = Map.of("VIG", 8, "FOR", 6);
        when(formulaEvaluatorService.calcularDerivado("VIG + FOR", variaveis)).thenReturn(14.0);

        // Act
        int resultado = calculationService.calcularBaseBonus(fichaBonus, bonusConfig, variaveis);

        // Assert
        assertThat(resultado).isEqualTo(14);
        assertThat(fichaBonus.getBase()).isEqualTo(14);
    }

    @Test
    @DisplayName("calcularBaseBonus deve retornar 0 quando fórmula é nula")
    void calcularBaseBonus_deveRetornarZero_quandoFormulaNula() {
        // Arrange
        FichaBonus fichaBonus = FichaBonus.builder().base(0).total(0).build();
        BonusConfig configSemFormula = BonusConfig.builder().nome("Teste").sigla("TST").formulaBase(null).build();

        // Act
        int resultado = calculationService.calcularBaseBonus(fichaBonus, configSemFormula, Map.of());

        // Assert
        assertThat(resultado).isEqualTo(0);
        verifyNoInteractions(formulaEvaluatorService);
    }

    @Test
    @DisplayName("calcularTotalBonus deve somar todos os modificadores")
    void calcularTotalBonus_deveSomarModificadores() {
        // Arrange
        FichaBonus fichaBonus = FichaBonus.builder()
                .base(10).vantagens(2).classe(3).itens(1).gloria(0).outros(0).total(0).build();

        // Act
        int resultado = calculationService.calcularTotalBonus(fichaBonus);

        // Assert
        assertThat(resultado).isEqualTo(16);
        assertThat(fichaBonus.getTotal()).isEqualTo(16);
    }

    // ==================== VIDA ====================

    @Test
    @DisplayName("calcularVidaTotal deve somar vigorTotal + nivel + vt + renascimentos + outros")
    void calcularVidaTotal_deveSomarCorretamente() {
        // Arrange
        FichaVida vida = FichaVida.builder().vt(5).outros(2).vidaTotal(0).build();
        // ficha: nivel=5, renascimentos=1
        // formula: vigorTotal(8) + nivel(5) + vt(5) + renascimentos(1) + outros(2) = 21

        // Act
        int resultado = calculationService.calcularVidaTotal(ficha, vida, 8);

        // Assert
        assertThat(resultado).isEqualTo(21);
        assertThat(vida.getVidaTotal()).isEqualTo(21);
    }

    @Test
    @DisplayName("calcularVidaMembro deve calcular floor(vidaTotal * porcentagem)")
    void calcularVidaMembro_deveCalcularFloor() {
        // Arrange
        FichaVidaMembro membro = FichaVidaMembro.builder().vida(0).build();
        BigDecimal porcentagem = new BigDecimal("0.30"); // 30%

        // Act: 21 * 0.30 = 6.3 → floor = 6
        int resultado = calculationService.calcularVidaMembro(membro, 21, porcentagem);

        // Assert
        assertThat(resultado).isEqualTo(6);
        assertThat(membro.getVida()).isEqualTo(6);
    }

    @Test
    @DisplayName("calcularVidaMembro deve retornar 0 quando porcentagem é nula")
    void calcularVidaMembro_deveRetornarZero_quandoPorcentagemNula() {
        // Arrange
        FichaVidaMembro membro = FichaVidaMembro.builder().vida(0).build();

        // Act
        int resultado = calculationService.calcularVidaMembro(membro, 20, null);

        // Assert
        assertThat(resultado).isEqualTo(0);
    }

    // ==================== ESSÊNCIA ====================

    @Test
    @DisplayName("calcularEssenciaTotal deve calcular floor((VIG+SAB)/2) + nivel + renascimentos + vantagens + outros")
    void calcularEssenciaTotal_deveSomarCorretamente() {
        // Arrange
        FichaEssencia essencia = FichaEssencia.builder()
                .renascimentos(0).vantagens(3).outros(1).total(0).build();
        // floor((8+6)/2) = floor(7) = 7
        // 7 + nivel(5) + renascimentos(1) + vantagens(3) + outros(1) = 17

        // Act
        int resultado = calculationService.calcularEssenciaTotal(ficha, essencia, 8, 6);

        // Assert
        assertThat(resultado).isEqualTo(17);
        assertThat(essencia.getTotal()).isEqualTo(17);
    }

    // ==================== AMEAÇA ====================

    @Test
    @DisplayName("calcularAmeacaTotal deve somar nivel + itens + titulos + renascimentos + outros")
    void calcularAmeacaTotal_deveSomarCorretamente() {
        // Arrange
        FichaAmeaca ameaca = FichaAmeaca.builder()
                .itens(2).titulos(1).renascimentos(1).outros(0).total(0).build();
        // nivel(5) + itens(2) + titulos(1) + renascimentos(1) + outros(0) = 9

        // Act
        int resultado = calculationService.calcularAmeacaTotal(ficha, ameaca);

        // Assert
        assertThat(resultado).isEqualTo(9);
        assertThat(ameaca.getTotal()).isEqualTo(9);
    }

    // ==================== RECALCULAR TUDO ====================

    @Test
    @DisplayName("recalcular deve chamar todos os submétodos na ordem correta")
    void recalcular_deveChamarTodosSubmétodos() {
        // Arrange
        AtributoConfig vigConfig = AtributoConfig.builder()
                .id(1L).nome("Vigor").abreviacao("VIG").build();
        AtributoConfig sabConfig = AtributoConfig.builder()
                .id(2L).nome("Sabedoria").abreviacao("SAB").build();

        FichaAtributo vigAtributo = FichaAtributo.builder()
                .base(5).nivel(0).outros(0).total(0).atributoConfig(vigConfig).build();
        FichaAtributo sabAtributo = FichaAtributo.builder()
                .base(4).nivel(0).outros(0).total(0).atributoConfig(sabConfig).build();

        FichaBonus fichaBonus = FichaBonus.builder()
                .base(0).vantagens(0).classe(0).itens(0).gloria(0).outros(0).total(0)
                .bonusConfig(BonusConfig.builder().formulaBase(null).build()).build();

        FichaVida vida = FichaVida.builder().vt(0).outros(0).vidaTotal(0).build();
        FichaEssencia essencia = FichaEssencia.builder()
                .renascimentos(0).vantagens(0).outros(0).total(0).build();
        FichaAmeaca ameaca = FichaAmeaca.builder()
                .itens(0).titulos(0).renascimentos(0).outros(0).total(0).build();

        // Act
        calculationService.recalcular(
                ficha,
                List.of(vigAtributo, sabAtributo),
                List.of(fichaBonus),
                vida,
                List.of(),
                essencia,
                ameaca
        );

        // Assert - atributos calculados
        assertThat(vigAtributo.getTotal()).isEqualTo(5);
        assertThat(sabAtributo.getTotal()).isEqualTo(4);

        // vida total: vigorTotal(5) + nivel(5) + vt(0) + renascimentos(1) + outros(0) = 11
        assertThat(vida.getVidaTotal()).isEqualTo(11);

        // essência: floor((5+4)/2) + 5 + 1 + 0 + 0 = 4 + 5 + 1 = 10
        assertThat(essencia.getTotal()).isEqualTo(10);

        // ameaça: nivel(5) + 0 + 0 + 0 + 0 = 5
        assertThat(ameaca.getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("recalcular deve usar 0 quando VIG ou SAB não encontrado")
    void recalcular_deveUsarZero_quandoVigOuSabNaoEncontrado() {
        // Arrange - atributos sem VIG ou SAB
        FichaAtributo forAtributo = FichaAtributo.builder()
                .base(5).nivel(0).outros(0).total(0)
                .atributoConfig(AtributoConfig.builder().abreviacao("FOR").build()).build();

        FichaVida vida = FichaVida.builder().vt(0).outros(0).vidaTotal(0).build();
        FichaEssencia essencia = FichaEssencia.builder()
                .renascimentos(0).vantagens(0).outros(0).total(0).build();
        FichaAmeaca ameaca = FichaAmeaca.builder()
                .itens(0).titulos(0).renascimentos(0).outros(0).total(0).build();

        // Act - deve completar sem erro
        calculationService.recalcular(
                ficha, List.of(forAtributo), List.of(),
                vida, List.of(), essencia, ameaca);

        // Assert - vigorTotal=0, sabedoriaTotal=0 usados como padrão
        // vida: 0 + 5 + 0 + 1 + 0 = 6
        assertThat(vida.getVidaTotal()).isEqualTo(6);
        // essência: floor((0+0)/2) + 5 + 1 + 0 + 0 = 6
        assertThat(essencia.getTotal()).isEqualTo(6);
    }
}
