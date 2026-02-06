package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.GeneroConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.GeneroConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para GeneroConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("GeneroConfiguracaoService - Testes de Integração")
class GeneroConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<GeneroConfig, GeneroConfiguracaoService, GeneroConfigRepository> {

    @Autowired
    private GeneroConfiguracaoService generoService;

    @Autowired
    private GeneroConfigRepository generoRepository;

    @Override
    protected GeneroConfiguracaoService getService() {
        return generoService;
    }

    @Override
    protected GeneroConfigRepository getRepository() {
        return generoRepository;
    }

    @Override
    protected GeneroConfig criarConfiguracaoValida(Jogo jogo) {
        return new GeneroConfig(
            null,
            jogo,
            "Masculino " + getUniqueSuffix(),
            "Gênero masculino",
            1
        );
    }

    @Override
    protected GeneroConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, GeneroConfig configuracaoExistente) {
        return null; // GeneroConfig não valida nome duplicado
    }

    @Override
    protected void atualizarCamposParaTeste(GeneroConfig configuracao) {
        configuracao.setNome("Masculino Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setOrdem(10);
    }

    @Override
    protected void verificarCamposAtualizados(GeneroConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Masculino Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getOrdem()).isEqualTo(10);
    }
}
