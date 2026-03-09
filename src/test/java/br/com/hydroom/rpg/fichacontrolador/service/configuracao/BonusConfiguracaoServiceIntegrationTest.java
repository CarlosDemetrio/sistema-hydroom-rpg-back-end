package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para BonusConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("BonusConfiguracaoService - Testes de Integração")
class BonusConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<BonusConfig, BonusConfiguracaoService, BonusConfigRepository> {

    private final AtomicInteger siglaCounter = new AtomicInteger(0);

    @Autowired
    private BonusConfiguracaoService bonusService;

    @Autowired
    private BonusConfigRepository bonusRepository;

    @Override
    protected BonusConfiguracaoService getService() {
        return bonusService;
    }

    @Override
    protected BonusConfigRepository getRepository() {
        return bonusRepository;
    }

    private String getUniqueSigla() {
        return "B" + String.format("%02d", siglaCounter.incrementAndGet());
    }

    @Override
    protected BonusConfig criarConfiguracaoValida(Jogo jogo) {
        return BonusConfig.builder()
            .jogo(jogo)
            .nome("B.B.A " + getUniqueSuffix())
            .sigla(getUniqueSigla())
            .descricao("Bônus Base de Ataque")
            .formulaBase("nivel + base / 2")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected BonusConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, BonusConfig configuracaoExistente) {
        return BonusConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .sigla(getUniqueSigla())
            .descricao("Descrição diferente")
            .formulaBase("nivel")
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(BonusConfig configuracao) {
        configuracao.setNome("B.B.A Atualizado");
        configuracao.setSigla("BBAT");
        configuracao.setDescricao("Nova descrição");
        configuracao.setFormulaBase("nivel + base");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(BonusConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("B.B.A Atualizado");
        assertThat(configuracao.getSigla()).isEqualTo("BBAT");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getFormulaBase()).isEqualTo("nivel + base");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
