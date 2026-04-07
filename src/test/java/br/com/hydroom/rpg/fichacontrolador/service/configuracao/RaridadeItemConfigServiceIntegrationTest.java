package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.RaridadeItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.TipoItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.CategoriaItem;
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
 * Testes de integração para RaridadeItemConfigService.
 */
@DisplayName("RaridadeItemConfigService - Testes de Integração")
class RaridadeItemConfigServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<RaridadeItemConfig, RaridadeItemConfigService, RaridadeItemConfigRepository> {

    @Autowired
    private RaridadeItemConfigService raridadeService;

    @Autowired
    private RaridadeItemConfigRepository raridadeRepository;

    @Autowired
    private ItemConfigRepository itemConfigRepository;

    @Autowired
    private TipoItemConfigRepository tipoItemConfigRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Limpa ItemConfig e TipoItemConfig antes do tearDown da classe base.
     * Necessário para evitar TransientPropertyValueException quando o base tearDown
     * deleta raridades enquanto ItemConfig (criado em RAR-03) ainda está no contexto de persistência.
     */
    @AfterEach
    void tearDownRaridadeTestData() {
        entityManager.flush();
        entityManager.clear();
        itemConfigRepository.deleteAll();
        tipoItemConfigRepository.deleteAll();
    }

    @Override
    protected RaridadeItemConfigService getService() {
        return raridadeService;
    }

    @Override
    protected RaridadeItemConfigRepository getRepository() {
        return raridadeRepository;
    }

    @Override
    protected RaridadeItemConfig criarConfiguracaoValida(Jogo jogo) {
        String suffix = getUniqueSuffix();
        return RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Comum " + suffix)
            .cor("#9d9d9d")
            .ordemExibicao(1)
            .podeJogadorAdicionar(true)
            .bonusAtributoMin(0)
            .bonusAtributoMax(0)
            .bonusDerivadoMin(0)
            .bonusDerivadoMax(0)
            .descricao("Raridade comum")
            .build();
    }

    @Override
    protected RaridadeItemConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, RaridadeItemConfig configuracaoExistente) {
        return RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .cor("#ff0000")
            .ordemExibicao(2)
            .podeJogadorAdicionar(false)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(RaridadeItemConfig configuracao) {
        configuracao.setNome("Raro Atualizado");
        configuracao.setCor("#0000ff");
        configuracao.setOrdemExibicao(5);
        configuracao.setPodeJogadorAdicionar(false);
    }

    @Override
    protected void verificarCamposAtualizados(RaridadeItemConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Raro Atualizado");
        assertThat(configuracao.getCor()).isEqualTo("#0000ff");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(5);
        assertThat(configuracao.isPodeJogadorAdicionar()).isFalse();
    }

    // =========================================================
    // CENÁRIOS ESPECÍFICOS DE RARIDADE
    // =========================================================

    @Test
    @DisplayName("RAR-01: Deve lançar ValidationException ao criar raridade com cor inválida")
    void deveLancarExcecaoAoCriarRaridadeComCorInvalida() {
        // Arrange
        RaridadeItemConfig raridade = RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Invalida " + getUniqueSuffix())
            .cor("#GGHHII") // cor inválida — não é hex válido
            .ordemExibicao(99)
            .podeJogadorAdicionar(false)
            .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> raridadeService.criar(raridade));
    }

    @Test
    @DisplayName("RAR-02: Deve lançar ConflictException ao criar raridade com ordemExibicao duplicado no mesmo jogo")
    void deveLancarExcecaoAoCriarRaridadeComOrdemExibicaoDuplicada() {
        // Arrange — salvar diretamente no repositório para não acionar validação de service
        raridadeRepository.save(RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Raridade Ordem 1 " + getUniqueSuffix())
            .cor("#aaaaaa")
            .ordemExibicao(77)
            .podeJogadorAdicionar(true)
            .build());

        RaridadeItemConfig duplicataOrdem = RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Raridade Ordem 1 Duplicada " + getUniqueSuffix())
            .cor("#bbbbbb")
            .ordemExibicao(77) // mesma ordemExibicao
            .podeJogadorAdicionar(false)
            .build();

        // Act & Assert
        assertThrows(ConflictException.class, () -> raridadeService.criar(duplicataOrdem));
    }

    @Test
    @DisplayName("RAR-03: Deve lançar ConflictException ao deletar raridade usada em ItemConfig")
    void deveLancarExcecaoAoDeletarRaridadeUsadaEmItemConfig() {
        // Arrange — criar raridade e item que a usa
        RaridadeItemConfig raridade = raridadeRepository.save(RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Raridade Em Uso " + getUniqueSuffix())
            .cor("#cc1234")
            .ordemExibicao(88)
            .podeJogadorAdicionar(false)
            .build());

        TipoItemConfig tipo = tipoItemConfigRepository.save(TipoItemConfig.builder()
            .jogo(jogo)
            .nome("Tipo Para RAR03 " + getUniqueSuffix())
            .categoria(CategoriaItem.ARMA)
            .requerDuasMaos(false)
            .ordemExibicao(1)
            .build());

        itemConfigRepository.save(ItemConfig.builder()
            .jogo(jogo)
            .nome("Item Usando Raridade " + getUniqueSuffix())
            .raridade(raridade)
            .tipo(tipo)
            .peso(BigDecimal.valueOf(1.00))
            .nivelMinimo(1)
            .ordemExibicao(1)
            .build());

        // Act & Assert
        assertThrows(ConflictException.class, () -> raridadeService.deletar(raridade.getId()));
    }

    @Test
    @DisplayName("RAR-04: Deve criar raridade com podeJogadorAdicionar=true corretamente")
    void deveCriarRaridadeComPodeJogadorAdicionarTrue() {
        // Arrange
        RaridadeItemConfig raridade = RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Comum Jogador " + getUniqueSuffix())
            .cor("#9d9d9d")
            .ordemExibicao(55)
            .podeJogadorAdicionar(true)
            .build();

        // Act
        RaridadeItemConfig criada = raridadeService.criar(raridade);

        // Assert
        assertThat(criada.getId()).isNotNull();
        assertThat(criada.isPodeJogadorAdicionar()).isTrue();
        assertThat(raridadeRepository.findById(criada.getId())).isPresent();
    }

    @Test
    @DisplayName("RAR-05: Deve lançar ValidationException ao criar raridade com bonusAtributoMin > bonusAtributoMax")
    void deveLancarExcecaoAoCriarRaridadeComBonusMinMaiorQueMax() {
        // Arrange
        RaridadeItemConfig raridade = RaridadeItemConfig.builder()
            .jogo(jogo)
            .nome("Bonus Invalido " + getUniqueSuffix())
            .cor("#112233")
            .ordemExibicao(44)
            .podeJogadorAdicionar(false)
            .bonusAtributoMin(10) // min > max — inválido
            .bonusAtributoMax(5)
            .build();

        // Act & Assert
        assertThrows(ValidationException.class, () -> raridadeService.criar(raridade));
    }
}
