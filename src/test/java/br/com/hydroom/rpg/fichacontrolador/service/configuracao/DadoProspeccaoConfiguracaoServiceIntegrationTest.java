package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.DadoProspeccaoConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para DadoProspeccaoConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("DadoProspeccaoConfiguracaoService - Testes de Integração")
class DadoProspeccaoConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<DadoProspeccaoConfig, DadoProspeccaoConfiguracaoService, DadoProspeccaoConfigRepository> {

    @Autowired
    private DadoProspeccaoConfiguracaoService dadoProspeccaoService;

    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoRepository;

    @Override
    protected DadoProspeccaoConfiguracaoService getService() {
        return dadoProspeccaoService;
    }

    @Override
    protected DadoProspeccaoConfigRepository getRepository() {
        return dadoProspeccaoRepository;
    }

    @Override
    protected DadoProspeccaoConfig criarConfiguracaoValida(Jogo jogo) {
        return DadoProspeccaoConfig.builder()
            .jogo(jogo)
            .nome("d6 " + getUniqueSuffix())
            .descricao("Dado de 6 faces")
            .numeroFaces(6)
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected DadoProspeccaoConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, DadoProspeccaoConfig configuracaoExistente) {
        return null; // DadoProspeccaoConfig não valida nome duplicado
    }

    @Override
    protected void atualizarCamposParaTeste(DadoProspeccaoConfig configuracao) {
        configuracao.setNome("d6 Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setNumeroFaces(8);
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(DadoProspeccaoConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("d6 Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getNumeroFaces()).isEqualTo(8);
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
