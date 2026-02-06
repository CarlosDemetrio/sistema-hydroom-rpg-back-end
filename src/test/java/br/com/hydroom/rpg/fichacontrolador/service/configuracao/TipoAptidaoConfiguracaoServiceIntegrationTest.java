package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoAptidaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para TipoAptidaoConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("TipoAptidaoConfiguracaoService - Testes de Integração")
class TipoAptidaoConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<TipoAptidao, TipoAptidaoConfiguracaoService, TipoAptidaoRepository> {

    @Autowired
    private TipoAptidaoConfiguracaoService tipoAptidaoService;

    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;

    @Override
    protected TipoAptidaoConfiguracaoService getService() {
        return tipoAptidaoService;
    }

    @Override
    protected TipoAptidaoRepository getRepository() {
        return tipoAptidaoRepository;
    }

    @Override
    protected TipoAptidao criarConfiguracaoValida(Jogo jogo) {
        return TipoAptidao.builder()
            .jogo(jogo)
            .nome("Física " + getUniqueSuffix())
            .descricao("Aptidões físicas e combate")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected TipoAptidao criarConfiguracaoComNomeDuplicado(Jogo jogo, TipoAptidao configuracaoExistente) {
        return TipoAptidao.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(TipoAptidao configuracao) {
        configuracao.setNome("Física Atualizada");
        configuracao.setDescricao("Nova descrição");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(TipoAptidao configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Física Atualizada");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
