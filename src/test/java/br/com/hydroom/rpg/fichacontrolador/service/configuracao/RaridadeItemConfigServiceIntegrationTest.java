package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para RaridadeItemConfigService.
 */
@DisplayName("RaridadeItemConfigService - Testes de Integração")
class RaridadeItemConfigServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<RaridadeItemConfig, RaridadeItemConfigService, RaridadeItemConfigRepository> {

    @Autowired
    private RaridadeItemConfigService raridadeService;

    @Autowired
    private RaridadeItemConfigRepository raridadeRepository;

    @Override
    protected RaridadeItemConfigService getService() {
        return raridadeService;
    }

    @Override
    protected RaridadeItemConfigRepository getRepository() {
        return raridadeRepository;
    }

    @Override
    protected RaridadeItemConfig criarConfiguracaoValida(Jogo jogo) {
        String suffix = getUniqueSuffix();
        return RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Comum " + suffix)
            .cor("#9d9d9d")
            .ordemExibicao(1)
            .podeJogadorAdicionar(true)
            .bonusAtributoMin(0)
            .bonusAtributoMax(0)
            .bonusDerivadoMin(0)
            .bonusDerivadoMax(0)
            .descricao("Raridade comum")
            .build();
    }

    @Override
    protected RaridadeItemConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, RaridadeItemConfig configuracaoExistente) {
        return RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .cor("#ff0000")
            .ordemExibicao(2)
            .podeJogadorAdicionar(false)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(RaridadeItemConfig configuracao) {
        configuracao.setNome("Raro Atualizado");
        configuracao.setCor("#0000ff");
        configuracao.setOrdemExibicao(5);
        configuracao.setPodeJogadorAdicionar(false);
    }

    @Override
    protected void verificarCamposAtualizados(RaridadeItemConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Raro Atualizado");
        assertThat(configuracao.getCor()).isEqualTo("#0000ff");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(5);
        assertThat(configuracao.isPodeJogadorAdicionar()).isFalse();
    }
}
