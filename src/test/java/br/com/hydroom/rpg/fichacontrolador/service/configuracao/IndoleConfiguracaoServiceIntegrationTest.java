package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.IndoleConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.IndoleConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para IndoleConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("IndoleConfiguracaoService - Testes de Integração")
class IndoleConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<IndoleConfig, IndoleConfiguracaoService, IndoleConfigRepository> {

    @Autowired
    private IndoleConfiguracaoService indoleService;

    @Autowired
    private IndoleConfigRepository indoleRepository;

    @Override
    protected IndoleConfiguracaoService getService() {
        return indoleService;
    }

    @Override
    protected IndoleConfigRepository getRepository() {
        return indoleRepository;
    }

    @Override
    protected IndoleConfig criarConfiguracaoValida(Jogo jogo) {
        return IndoleConfig.builder()
            .jogo(jogo)
            .nome("Bom " + getUniqueSuffix())
            .descricao("Personagem com índole boa")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected IndoleConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, IndoleConfig configuracaoExistente) {
        return IndoleConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(IndoleConfig configuracao) {
        configuracao.setNome("Bom Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(IndoleConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Bom Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
