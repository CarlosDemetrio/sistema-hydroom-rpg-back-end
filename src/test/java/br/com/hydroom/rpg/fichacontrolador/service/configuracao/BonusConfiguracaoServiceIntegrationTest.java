package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Override
    protected BonusConfig criarConfiguracaoValida(Jogo jogo) {
        return BonusConfig.builder()
            .jogo(jogo)
            .nome("B.B.A " + getUniqueSuffix())
            .descricao("Bônus Base de Ataque")
            .formulaBase("NIVEL + FOR/2")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected BonusConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, BonusConfig configuracaoExistente) {
        return BonusConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .formulaBase("NIVEL")
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(BonusConfig configuracao) {
        configuracao.setNome("B.B.A Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setFormulaBase("NIVEL + FOR");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(BonusConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("B.B.A Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getFormulaBase()).isEqualTo("NIVEL + FOR");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
