package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.model.enums.SubcategoriaItem;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RaridadeItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoItemConfigRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    @Autowired
    private RaridadeItemConfigRepository raridadeItemConfigRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Limpa ItemConfig e RaridadeItemConfig antes do tearDown da classe base.
     * Necessário para evitar TransientPropertyValueException quando o base tearDown
     * deleta tipos enquanto ItemConfig (criado em TIPO-03) ainda está no contexto de persistência.
     */
    @AfterEach
    void tearDownTipoTestData() {
        entityManager.flush();
        entityManager.clear();
        itemConfigRepository.deleteAll();
        raridadeItemConfigRepository.deleteAll();
    }

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

    // =========================================================
    // CENÁRIOS ESPECÍFICOS DE TIPO DE ITEM
    // =========================================================

    @Test
    @DisplayName("TIPO-01: Deve criar tipo com categoriaItem=ARMA e subcategoria=ESPADA corretamente")
    void deveCriarTipoComCategoriaESubcategoria() {
        // Arrange
        TipoItemConfig tipo = TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Espada " + getUniqueSuffix())
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .build();

        // Act
        TipoItemConfig criado = tipoItemService.criar(tipo);

        // Assert
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getCategoria()).isEqualTo(CategoriaItem.ARMA);
        assertThat(criado.getSubcategoria()).isEqualTo(SubcategoriaItem.ESPADA);
    }

    @Test
    @DisplayName("TIPO-02: Deve criar tipo sem subcategoria (campo nullable)")
    void deveCriarTipoSemSubcategoria() {
        // Arrange
        TipoItemConfig tipo = TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Generico " + getUniqueSuffix())
            .categoria(CategoriaItem.CONSUMIVEL)
            .subcategoria(null) // nullable
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .build();

        // Act
        TipoItemConfig criado = tipoItemService.criar(tipo);

        // Assert
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getSubcategoria()).isNull();
    }

    @Test
    @DisplayName("TIPO-03: Deve lançar ConflictException ao deletar tipo usado em ItemConfig")
    void deveLancarExcecaoAoDeletarTipoUsadoEmItemConfig() {
        // Arrange — criar tipo e item que o usa
        TipoItemConfig tipo = tipoItemRepository.save(TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Tipo Em Uso " + getUniqueSuffix())
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.ESPADA)
            .requerDuasMaos(false)
            .ordemExibicao(77)
            .build());

        RaridadeItemConfig raridade = raridadeItemConfigRepository.save(RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Raridade Para TIPO03 " + getUniqueSuffix())
            .cor("#ab1234")
            .ordemExibicao(1)
            .podeJogadorAdicionar(true)
            .build());

        itemConfigRepository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("Item Usando Tipo " + getUniqueSuffix())
            .raridade(raridade)
            .tipo(tipo)
            .peso(BigDecimal.valueOf(1.50))
            .nivelMinimo(1)
            .ordemExibicao(1)
            .build());

        // Act & Assert
        assertThrows(ConflictException.class, () -> tipoItemService.deletar(tipo.getId()));
    }

    @Test
    @DisplayName("TIPO-04: Deve criar tipo com requerDuasMaos=true corretamente")
    void deveCriarTipoComRequerDuasMaosTrue() {
        // Arrange
        TipoItemConfig tipo = TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Machado Grande " + getUniqueSuffix())
            .categoria(CategoriaItem.ARMA)
            .subcategoria(SubcategoriaItem.MACHADO)
            .requerDuasMaos(true)
            .ordemExibicao(1)
            .build();

        // Act
        TipoItemConfig criado = tipoItemService.criar(tipo);

        // Assert
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.isRequerDuasMaos()).isTrue();
    }
}
