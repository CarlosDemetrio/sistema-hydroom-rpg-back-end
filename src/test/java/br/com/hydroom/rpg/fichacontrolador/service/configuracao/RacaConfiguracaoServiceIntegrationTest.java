package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para RacaConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("RacaConfiguracaoService - Testes de Integração")
class RacaConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<Raca, RacaConfiguracaoService, ConfiguracaoRacaRepository> {

    @Autowired
    private RacaConfiguracaoService racaService;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Override
    protected RacaConfiguracaoService getService() {
        return racaService;
    }

    @Override
    protected ConfiguracaoRacaRepository getRepository() {
        return racaRepository;
    }

    @Override
    protected Raca criarConfiguracaoValida(Jogo jogo) {
        return Raca.builder()
            .jogo(jogo)
            .nome("Humano " + getUniqueSuffix())
            .descricao("Raça humana versátil")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected Raca criarConfiguracaoComNomeDuplicado(Jogo jogo, Raca configuracaoExistente) {
        return Raca.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(Raca configuracao) {
        configuracao.setNome("Humano Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(Raca configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Humano Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
