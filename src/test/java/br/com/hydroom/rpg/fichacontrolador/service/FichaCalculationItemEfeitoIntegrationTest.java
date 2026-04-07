package br.com.hydroom.rpg.fichacontrolador.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração para cálculo de fichas considerando efeitos de itens equipados.
 *
 * <p><b>BLOQUEADO:</b> Esta suite depende da implementação da Spec 016 T5
 * (FichaCalculationService - integração com ItemEfeito), que ainda não foi entregue.</p>
 *
 * <p>Cenários previstos:</p>
 * <ul>
 *   <li>CALC-ITEM-01: Equipar item com BONUS_ATRIBUTO deve incrementar atributo da ficha</li>
 *   <li>CALC-ITEM-02: Desequipar item deve reverter bônus de atributo</li>
 *   <li>CALC-ITEM-03: Equipar item com FORMULA_CUSTOMIZADA deve recalcular derivado corretamente</li>
 *   <li>CALC-ITEM-04: Múltiplos itens com mesmo BONUS_DERIVADO devem se acumular</li>
 *   <li>CALC-ITEM-05: Item com EFEITO_DADO não afeta cálculo de atributos (somente rollable)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaCalculationService + ItemEfeito - Testes de Integração (Spec 016 T5)")
class FichaCalculationItemEfeitoIntegrationTest {

    @Test
    @Disabled("Spec 016 T5 bloqueada: FichaCalculationService ainda não integra ItemEfeito de itens equipados")
    @DisplayName("TODO: Implementar testes de cálculo com efeitos de itens após entrega da Spec 016 T5")
    void todoImplementarTestesCalculoComEfeitosDeItens() {
        // TODO: Implementar após entrega da Spec 016 T5
        // Ver cenários CALC-ITEM-01 a CALC-ITEM-05 no Javadoc da classe
    }
}
