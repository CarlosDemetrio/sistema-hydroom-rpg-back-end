package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Testes de integração para AtributoConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("AtributoConfiguracaoService - Testes de Integração")
class AtributoConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<AtributoConfig, AtributoConfiguracaoService, ConfiguracaoAtributoRepository> {

    @Autowired
    private AtributoConfiguracaoService atributoService;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Override
    protected AtributoConfiguracaoService getService() {
        return atributoService;
    }

    @Override
    protected ConfiguracaoAtributoRepository getRepository() {
        return atributoRepository;
    }

    @Override
    protected AtributoConfig criarConfiguracaoValida(Jogo jogo) {
        String suffix = getUniqueSuffix();
        return AtributoConfig.builder()
            .jogo(jogo)
            .nome("Força " + suffix)
            .abreviacao("F" + suffix.substring(0, 3).toUpperCase())
            .descricao("Força física do personagem")
            .formulaImpeto("FOR * 2")
            .descricaoImpeto("Bônus de força no ímpeto")
            .valorMinimo(0)
            .valorMaximo(100)
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected AtributoConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, AtributoConfig configuracaoExistente) {
        return AtributoConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .abreviacao("STR")
            .descricao("Descrição diferente")
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(AtributoConfig configuracao) {
        configuracao.setNome("Força Atualizada");
        configuracao.setDescricao("Nova descrição");
        configuracao.setAbreviacao("FRC");
        configuracao.setFormulaImpeto("FOR * 3");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(AtributoConfig configuracao) {
        org.assertj.core.api.Assertions.assertThat(configuracao.getNome()).isEqualTo("Força Atualizada");
        org.assertj.core.api.Assertions.assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        org.assertj.core.api.Assertions.assertThat(configuracao.getAbreviacao()).isEqualTo("FRC");
        org.assertj.core.api.Assertions.assertThat(configuracao.getFormulaImpeto()).isEqualTo("FOR * 3");
        org.assertj.core.api.Assertions.assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
