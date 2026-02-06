package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.BaseEntity;
import br.com.hydroom.rpg.fichacontrolador.model.ConfiguracaoEntity;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Classe base abstrata para testes de integração de services de configuração.
 *
 * <p>Fornece estrutura comum e testes genéricos para todos os services de configuração.</p>
 *
 * @param <T> Tipo da entidade de configuração
 * @param <S> Tipo do service
 * @param <R> Tipo do repository
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
public abstract class BaseConfiguracaoServiceIntegrationTest<
    T extends BaseEntity & ConfiguracaoEntity,
    S extends BaseConfiguracaoService<T>,
    R extends JpaRepository<@NonNull T, Long>
> {

    @Autowired
    protected JogoRepository jogoRepository;

    protected S service;
    protected R repository;
    protected Jogo jogo;
    protected Jogo outroJogo;

    /**
     * Retorna a instância do service a ser testado.
     */
    protected abstract S getService();

    /**
     * Retorna a instância do repository a ser testado.
     */
    protected abstract R getRepository();

    /**
     * Cria uma nova configuração válida para testes.
     */
    protected abstract T criarConfiguracaoValida(Jogo jogo);

    /**
     * Cria uma configuração com nome duplicado para teste de conflito.
     */
    protected abstract T criarConfiguracaoComNomeDuplicado(Jogo jogo, T configuracaoExistente);

    /**
     * Atualiza campos de uma configuração para teste de update.
     */
    protected abstract void atualizarCamposParaTeste(T configuracao);

    /**
     * Verifica se os campos foram atualizados corretamente.
     */
    protected abstract void verificarCamposAtualizados(T configuracao);

    @BeforeEach
    void setUp() {
        service = getService();
        repository = getRepository();

        // Limpar dados antes de cada teste
        repository.deleteAll();
        jogoRepository.deleteAll();

        // Criar jogos de teste
        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Teste")
            .descricao("Jogo para testes de configuração")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outra Campanha")
            .descricao("Outro jogo para isolamento de testes")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        jogoRepository.deleteAll();
    }

    /**
     * Gera um sufixo único para evitar conflitos de nome em testes.
     * Usa UUID para garantir unicidade.
     */
    protected String getUniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Deve listar configurações de um jogo específico")
    void deveListarConfiguracoesPorJogo() {
        // Arrange
        T config1 = criarConfiguracaoValida(jogo);
        T config2 = criarConfiguracaoValida(jogo);
        T configOutroJogo = criarConfiguracaoValida(outroJogo);

        repository.save(config1);
        repository.save(config2);
        repository.save(configOutroJogo);

        // Act
        List<T> resultado = service.listar(jogo.getId());

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting("jogo.id").containsOnly(jogo.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando jogo não tem configurações")
    void deveRetornarListaVaziaQuandoJogoSemConfiguracoes() {
        // Act
        List<T> resultado = service.listar(jogo.getId());

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar configuração por ID")
    void deveBuscarConfiguracaoPorId() {
        // Arrange
        T config = criarConfiguracaoValida(jogo);
        T saved = repository.save(config);

        // Act
        T resultado = service.buscarPorId(saved.getId());

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    void deveLancarExcecaoAoBuscarIdInexistente() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.buscarPorId(999L));
    }

    @Test
    @DisplayName("Deve criar configuração com sucesso")
    void deveCriarConfiguracaoComSucesso() {
        // Arrange
        T config = criarConfiguracaoValida(jogo);

        // Act
        T resultado = service.criar(config);

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getJogo().getId()).isEqualTo(jogo.getId());
        assertThat(resultado.isActive()).isTrue();

        // Verificar persistência
        T persistido = repository.findById(resultado.getId()).orElseThrow();
        assertThat(persistido).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar configuração com sucesso")
    void deveAtualizarConfiguracaoComSucesso() {
        // Arrange
        T config = criarConfiguracaoValida(jogo);
        T saved = repository.save(config);

        atualizarCamposParaTeste(saved);

        // Act
        T resultado = service.atualizar(saved.getId(), saved);

        // Assert
        assertThat(resultado.getId()).isEqualTo(saved.getId());
        verificarCamposAtualizados(resultado);

        // Verificar persistência
        T persistido = repository.findById(resultado.getId()).orElseThrow();
        verificarCamposAtualizados(persistido);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar ID inexistente")
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        // Arrange
        T config = criarConfiguracaoValida(jogo);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.atualizar(999L, config));
    }

    @Test
    @DisplayName("Deve deletar configuração (soft delete)")
    void deveDeletarConfiguracaoSoftDelete() {
        // Arrange
        T config = criarConfiguracaoValida(jogo);
        T saved = repository.save(config);

        // Act
        service.deletar(saved.getId());

        // Assert
        T deletado = repository.findById(saved.getId()).orElseThrow();
        assertThat(deletado.isActive()).isFalse();
        assertThat(deletado.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao deletar ID inexistente")
    void deveLancarExcecaoAoDeletarIdInexistente() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> service.deletar(999L));
    }

    @Test
    @DisplayName("Não deve listar configurações deletadas")
    void naoDeveListarConfiguracoesDeletedas() {
        // Arrange
        T config1 = criarConfiguracaoValida(jogo);
        T saved1 = repository.save(config1);

        T config2 = criarConfiguracaoValida(jogo);
        T saved2 = repository.save(config2);

        // Deletar uma configuração
        service.deletar(saved1.getId());

        // Act
        List<T> resultado = service.listar(jogo.getId());

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado).extracting("id").doesNotContain(saved1.getId());
    }

    /**
     * Teste de validação de nome duplicado (se aplicável).
     * Subclasses podem retornar null em criarConfiguracaoComNomeDuplicado para pular este teste.
     */
    @Test
    @DisplayName("Deve lançar ConflictException ao criar com nome duplicado")
    void deveLancarExcecaoAoCriarComNomeDuplicado() {
        // Arrange
        T config1 = criarConfiguracaoValida(jogo);
        T saved = repository.save(config1);

        T configDuplicada = criarConfiguracaoComNomeDuplicado(jogo, saved);

        // Skip test if duplicate validation is not supported
        assumeTrue(configDuplicada != null, "Service does not validate duplicate names");

        // Act & Assert
        assertThrows(ConflictException.class, () -> service.criar(configDuplicada));
    }

    @Test
    @DisplayName("Deve permitir mesmo nome em jogos diferentes")
    void devePermitirMesmoNomeEmJogosDiferentes() {
        // Arrange
        T config1 = criarConfiguracaoValida(jogo);
        T saved = repository.save(config1);

        T config2 = criarConfiguracaoComNomeDuplicado(outroJogo, saved);

        // Skip test if duplicate validation is not supported
        assumeTrue(config2 != null, "Service does not validate duplicate names");

        // Act - Deve criar sem exceção
        T resultado = service.criar(config2);

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getJogo().getId()).isEqualTo(outroJogo.getId());
    }
}
