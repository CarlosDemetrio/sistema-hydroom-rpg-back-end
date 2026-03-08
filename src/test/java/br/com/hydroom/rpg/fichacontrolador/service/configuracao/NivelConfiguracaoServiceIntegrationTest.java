package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.NivelConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoNivelRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para NivelConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("NivelConfiguracaoService - Testes de Integração")
class NivelConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<NivelConfig, NivelConfiguracaoService, ConfiguracaoNivelRepository> {

    @Autowired
    private NivelConfiguracaoService nivelService;

    @Autowired
    private ConfiguracaoNivelRepository nivelRepository;

    // Counter para gerar níveis únicos
    private static final AtomicInteger nivelCounter = new AtomicInteger(1);

    @Override
    protected NivelConfiguracaoService getService() {
        return nivelService;
    }

    @Override
    protected ConfiguracaoNivelRepository getRepository() {
        return nivelRepository;
    }

    @Override
    protected NivelConfig criarConfiguracaoValida(Jogo jogo) {
        NivelConfig config = new NivelConfig();
        config.setJogo(jogo);
        config.setNivel(nivelCounter.getAndIncrement());
        config.setXpNecessaria(0L);
        config.setPontosAtributo(3);
        config.setPontosAptidao(3);
        config.setLimitadorAtributo(10);
        return config;
    }

    @Override
    protected NivelConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, NivelConfig configuracaoExistente) {
        NivelConfig config = new NivelConfig();
        config.setJogo(jogo);
        config.setNivel(configuracaoExistente.getNivel());
        config.setXpNecessaria(100L);
        config.setPontosAtributo(3);
        config.setPontosAptidao(3);
        config.setLimitadorAtributo(10);
        return config;
    }

    @Override
    protected void atualizarCamposParaTeste(NivelConfig configuracao) {
        // Não altera o nível (parte da chave única)
        configuracao.setXpNecessaria(1000L);
        configuracao.setPontosAtributo(5);
        configuracao.setLimitadorAtributo(15);
    }

    @Override
    protected void verificarCamposAtualizados(NivelConfig configuracao) {
        assertThat(configuracao.getXpNecessaria()).isEqualTo(1000L);
        assertThat(configuracao.getPontosAtributo()).isEqualTo(5);
        assertThat(configuracao.getLimitadorAtributo()).isEqualTo(15);
    }
}
