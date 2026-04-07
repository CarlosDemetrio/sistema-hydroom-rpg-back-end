package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para TipoItemConfigService.
 */
@DisplayName("TipoItemConfigService - Testes de Integração")
class TipoItemConfigServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<TipoItemConfig, TipoItemConfigService, TipoItemConfigRepository> {

    @Autowired
    private TipoItemConfigService tipoItemService;

    @Autowired
    private TipoItemConfigRepository tipoItemRepository;

    @Override
    protected TipoItemConfigService getService() {
        return tipoItemService;
    }

    @Override
    protected TipoItemConfigRepository getRepository() {
        return tipoItemRepository;
    }

    @Override
    protected TipoItemConfig criarConfiguracaoValida(Jogo jogo) {
        String suffix = getUniqueSuffix();
        return TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Espada Longa " + suffix)
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .descricao("Espada longa de dois gumes")
            .build();
    }

    @Override
    protected TipoItemConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, TipoItemConfig configuracaoExistente) {
        return TipoItemConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(true)
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(TipoItemConfig configuracao) {
        configuracao.setNome("Machado de Guerra Atualizado");
        configuracao.setCategoria(CategoriaItem.ARMA);
        configuracao.setSubcategoria(SubcategoriaItem.MACHADO);
        configuracao.setRequerDuasMaos(true);
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(TipoItemConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Machado de Guerra Atualizado");
        assertThat(configuracao.getCategoria()).isEqualTo(CategoriaItem.ARMA);
        assertThat(configuracao.getSubcategoria()).isEqualTo(SubcategoriaItem.MACHADO);
        assertThat(configuracao.isRequerDuasMaos()).isTrue();
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
