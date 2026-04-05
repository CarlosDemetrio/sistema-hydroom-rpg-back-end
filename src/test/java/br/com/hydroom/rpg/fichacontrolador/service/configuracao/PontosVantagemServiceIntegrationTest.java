package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.PontosVantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.PontosVantagemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para PontosVantagemService.
 */
@DisplayName("PontosVantagemService - Testes de Integração")
class PontosVantagemServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<PontosVantagemConfig, PontosVantagemService, PontosVantagemConfigRepository> {

    @Autowired
    private PontosVantagemService pontosVantagemService;

    @Autowired
    private PontosVantagemConfigRepository pontosRepository;

    private static final AtomicInteger nivelCounter = new AtomicInteger(1);

    @Override
    protected PontosVantagemService getService() {
        return pontosVantagemService;
    }

    @Override
    protected PontosVantagemConfigRepository getRepository() {
        return pontosRepository;
    }

    @Override
    protected PontosVantagemConfig criarConfiguracaoValida(Jogo jogo) {
        return PontosVantagemConfig.builder()
            .jogo(jogo)
            .nivel(nivelCounter.getAndIncrement())
            .pontosGanhos(1)
            .build();
    }

    @Override
    protected PontosVantagemConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, PontosVantagemConfig configuracaoExistente) {
        return PontosVantagemConfig.builder()
            .jogo(jogo)
            .nivel(configuracaoExistente.getNivel())
            .pontosGanhos(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(PontosVantagemConfig configuracao) {
        configuracao.setPontosGanhos(3);
    }

    @Override
    protected void verificarCamposAtualizados(PontosVantagemConfig configuracao) {
        assertThat(configuracao.getPontosGanhos()).isEqualTo(3);
    }
}
