package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.CategoriaVantagem;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.CategoriaVantagemRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para CategoriaVantagemService.
 */
@DisplayName("CategoriaVantagemService - Testes de Integração")
class CategoriaVantagemServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<CategoriaVantagem, CategoriaVantagemService, CategoriaVantagemRepository> {

    @Autowired
    private CategoriaVantagemService categoriaService;

    @Autowired
    private CategoriaVantagemRepository categoriaRepository;

    @Override
    protected CategoriaVantagemService getService() {
        return categoriaService;
    }

    @Override
    protected CategoriaVantagemRepository getRepository() {
        return categoriaRepository;
    }

    @Override
    protected CategoriaVantagem criarConfiguracaoValida(Jogo jogo) {
        return CategoriaVantagem.builder()
            .jogo(jogo)
            .nome("Combate " + getUniqueSuffix())
            .descricao("Vantagens de combate")
            .cor("#FF5733")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected CategoriaVantagem criarConfiguracaoComNomeDuplicado(Jogo jogo, CategoriaVantagem configuracaoExistente) {
        return CategoriaVantagem.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Outra descrição")
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(CategoriaVantagem configuracao) {
        configuracao.setNome("Magia Atualizada");
        configuracao.setDescricao("Vantagens de magia");
        configuracao.setCor("#3498DB");
        configuracao.setOrdemExibicao(5);
    }

    @Override
    protected void verificarCamposAtualizados(CategoriaVantagem configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Magia Atualizada");
        assertThat(configuracao.getDescricao()).isEqualTo("Vantagens de magia");
        assertThat(configuracao.getCor()).isEqualTo("#3498DB");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(5);
    }
}
