package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.PresencaConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.PresencaConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para PresencaConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("PresencaConfiguracaoService - Testes de Integração")
class PresencaConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<PresencaConfig, PresencaConfiguracaoService, PresencaConfigRepository> {

    @Autowired
    private PresencaConfiguracaoService presencaService;

    @Autowired
    private PresencaConfigRepository presencaRepository;

    @Override
    protected PresencaConfiguracaoService getService() {
        return presencaService;
    }

    @Override
    protected PresencaConfigRepository getRepository() {
        return presencaRepository;
    }

    @Override
    protected PresencaConfig criarConfiguracaoValida(Jogo jogo) {
        return PresencaConfig.builder()
            .jogo(jogo)
            .nome("Leal " + getUniqueSuffix())
            .descricao("Personagem com presença leal")
            .ordem(1)
            .build();
    }

    @Override
    protected PresencaConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, PresencaConfig configuracaoExistente) {
        return null; // PresencaConfig não valida nome duplicado
    }

    @Override
    protected void atualizarCamposParaTeste(PresencaConfig configuracao) {
        configuracao.setNome("Leal Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setOrdem(10);
    }

    @Override
    protected void verificarCamposAtualizados(PresencaConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Leal Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getOrdem()).isEqualTo(10);
    }
}
