package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para VantagemConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("VantagemConfiguracaoService - Testes de Integração")
class VantagemConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<VantagemConfig, VantagemConfiguracaoService, VantagemConfigRepository> {

    @Autowired
    private VantagemConfiguracaoService vantagemService;

    @Autowired
    private VantagemConfigRepository vantagemRepository;

    @Override
    protected VantagemConfiguracaoService getService() {
        return vantagemService;
    }

    @Override
    protected VantagemConfigRepository getRepository() {
        return vantagemRepository;
    }

    @Override
    protected VantagemConfig criarConfiguracaoValida(Jogo jogo) {
        return VantagemConfig.builder()
            .jogo(jogo)
            .nome("Ambidestria " + getUniqueSuffix())
            .descricao("Pode usar ambas as mãos igualmente")
            .nivelMaximo(5)
            .formulaCusto("custo_base * nivel_vantagem")
            .descricaoEfeito("Reduz penalidades de usar armas em ambas as mãos")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected VantagemConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, VantagemConfig configuracaoExistente) {
        return VantagemConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .nivelMaximo(3)
            .formulaCusto("custo_base")
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(VantagemConfig configuracao) {
        configuracao.setNome("Ambidestria Atualizada");
        configuracao.setDescricao("Nova descrição");
        configuracao.setNivelMaximo(10);
        configuracao.setFormulaCusto("custo_base * nivel_vantagem * 3");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(VantagemConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Ambidestria Atualizada");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getNivelMaximo()).isEqualTo(10);
        assertThat(configuracao.getFormulaCusto()).isEqualTo("custo_base * nivel_vantagem * 3");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
