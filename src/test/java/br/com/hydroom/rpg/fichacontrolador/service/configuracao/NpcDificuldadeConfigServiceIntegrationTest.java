package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.FocoNpc;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.NpcDificuldadeConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.NpcDificuldadeConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para NpcDificuldadeConfiguracaoService.
 *
 * <p>Inclui testes base do CRUD (herdados) e testes específicos para:
 * - Criação com valoresAtributo populados
 * - Sincronização de valoresAtributo no update
 * - Filtro por foco (FISICO/MAGICO)</p>
 */
@DisplayName("NpcDificuldadeConfiguracaoService - Testes de Integração")
class NpcDificuldadeConfigServiceIntegrationTest extends
        BaseConfiguracaoServiceIntegrationTest<NpcDificuldadeConfig, NpcDificuldadeConfiguracaoService, NpcDificuldadeConfigRepository> {

    @Autowired
    private NpcDificuldadeConfiguracaoService npcDificuldadeService;

    @Autowired
    private NpcDificuldadeConfigRepository npcDificuldadeRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    private AtributoConfig atributoForca;
    private AtributoConfig atributoAgilidade;

    @Override
    protected NpcDificuldadeConfiguracaoService getService() {
        return npcDificuldadeService;
    }

    @Override
    protected NpcDificuldadeConfigRepository getRepository() {
        return npcDificuldadeRepository;
    }

    @Override
    protected NpcDificuldadeConfig criarConfiguracaoValida(Jogo jogo) {
        return NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Fácil " + getUniqueSuffix())
                .descricao("NPC de baixa dificuldade")
                .foco(FocoNpc.FISICO)
                .ordemExibicao(1)
                .build();
    }

    @Override
    protected NpcDificuldadeConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, NpcDificuldadeConfig configuracaoExistente) {
        return NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome(configuracaoExistente.getNome())
                .descricao("Descrição diferente")
                .foco(FocoNpc.MAGICO)
                .ordemExibicao(2)
                .build();
    }

    @Override
    protected void atualizarCamposParaTeste(NpcDificuldadeConfig configuracao) {
        configuracao.setNome("Médio Atualizado");
        configuracao.setDescricao("NPC de dificuldade média");
        configuracao.setFoco(FocoNpc.MAGICO);
        configuracao.setOrdemExibicao(5);
    }

    @Override
    protected void verificarCamposAtualizados(NpcDificuldadeConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Médio Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("NPC de dificuldade média");
        assertThat(configuracao.getFoco()).isEqualTo(FocoNpc.MAGICO);
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(5);
    }

    @BeforeEach
    void setUpAtributos() {
        atributoRepository.deleteAll();

        atributoForca = atributoRepository.save(AtributoConfig.builder()
                .jogo(jogo)
                .nome("Força")
                .abreviacao("FOR")
                .ordemExibicao(1)
                .build());

        atributoAgilidade = atributoRepository.save(AtributoConfig.builder()
                .jogo(jogo)
                .nome("Agilidade")
                .abreviacao("AGI")
                .ordemExibicao(2)
                .build());
    }

    // ===== TESTES ESPECÍFICOS =====

    @Test
    @DisplayName("Deve criar nível de dificuldade com valoresAtributo populados")
    void deveCriarComValoresAtributoPopulados() {
        // Arrange
        NpcDificuldadeConfig config = NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Elite " + getUniqueSuffix())
                .foco(FocoNpc.FISICO)
                .ordemExibicao(4)
                .build();

        List<NpcDificuldadeAtributo> valores = new ArrayList<>();
        valores.add(NpcDificuldadeAtributo.builder()
                .npcDificuldadeConfig(config)
                .atributoConfig(atributoForca)
                .valorBase(80)
                .build());
        valores.add(NpcDificuldadeAtributo.builder()
                .npcDificuldadeConfig(config)
                .atributoConfig(atributoAgilidade)
                .valorBase(60)
                .build());
        config.setValoresAtributo(valores);

        // Act
        NpcDificuldadeConfig criado = npcDificuldadeService.criar(config);

        // Assert
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.isActive()).isTrue();

        // Buscar com valores para validar persistência (evita L1 cache)
        NpcDificuldadeConfig persistido = npcDificuldadeService.buscarPorId(criado.getId());
        assertThat(persistido.getValoresAtributo()).hasSize(2);
        assertThat(persistido.getValoresAtributo())
                .extracting(v -> v.getAtributoConfig().getNome())
                .containsExactlyInAnyOrder("Força", "Agilidade");
        assertThat(persistido.getValoresAtributo())
                .extracting(NpcDificuldadeAtributo::getValorBase)
                .containsExactlyInAnyOrder(80, 60);
    }

    @Test
    @DisplayName("Deve atualizar sincronizando valoresAtributo — adiciona novo e remove excluído")
    void deveAtualizarSincronizandoValoresAtributo() {
        // Arrange — criar com apenas Força
        NpcDificuldadeConfig config = NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Difícil " + getUniqueSuffix())
                .foco(FocoNpc.FISICO)
                .ordemExibicao(3)
                .build();

        NpcDificuldadeAtributo valorForca = NpcDificuldadeAtributo.builder()
                .npcDificuldadeConfig(config)
                .atributoConfig(atributoForca)
                .valorBase(70)
                .build();
        config.setValoresAtributo(new ArrayList<>(List.of(valorForca)));

        NpcDificuldadeConfig criado = npcDificuldadeService.criar(config);

        // Buscar fresh (evita L1 cache)
        NpcDificuldadeConfig existente = npcDificuldadeService.buscarPorId(criado.getId());
        assertThat(existente.getValoresAtributo()).hasSize(1);

        // Act — atualizar substituindo Força por Agilidade
        // Usa substituirValoresAtributo para preservar a referência da coleção gerenciada pelo Hibernate
        NpcDificuldadeAtributo novoValorAgilidade = NpcDificuldadeAtributo.builder()
                .npcDificuldadeConfig(existente)
                .atributoConfig(atributoAgilidade)
                .valorBase(55)
                .build();
        npcDificuldadeService.substituirValoresAtributo(existente, List.of(novoValorAgilidade));

        NpcDificuldadeConfig atualizado = npcDificuldadeService.atualizar(existente.getId(), existente);

        // Assert — buscar fresh para evitar cache
        NpcDificuldadeConfig verificado = npcDificuldadeService.buscarPorId(atualizado.getId());
        assertThat(verificado.getValoresAtributo()).hasSize(1);
        assertThat(verificado.getValoresAtributo().get(0).getAtributoConfig().getNome()).isEqualTo("Agilidade");
        assertThat(verificado.getValoresAtributo().get(0).getValorBase()).isEqualTo(55);
    }

    @Test
    @DisplayName("Deve filtrar níveis de dificuldade por foco FISICO")
    void deveFiltrarPorFocoFisico() {
        // Arrange
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Guerreiro Fraco " + getUniqueSuffix())
                .foco(FocoNpc.FISICO)
                .ordemExibicao(1)
                .build());
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Mago Fraco " + getUniqueSuffix())
                .foco(FocoNpc.MAGICO)
                .ordemExibicao(2)
                .build());
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Guerreiro Elite " + getUniqueSuffix())
                .foco(FocoNpc.FISICO)
                .ordemExibicao(3)
                .build());

        // Act
        List<NpcDificuldadeConfig> fisicos = npcDificuldadeService.listarPorFoco(jogo.getId(), FocoNpc.FISICO);
        List<NpcDificuldadeConfig> magicos = npcDificuldadeService.listarPorFoco(jogo.getId(), FocoNpc.MAGICO);

        // Assert
        assertThat(fisicos).hasSize(2);
        assertThat(fisicos).extracting(NpcDificuldadeConfig::getFoco).containsOnly(FocoNpc.FISICO);
        assertThat(magicos).hasSize(1);
        assertThat(magicos).extracting(NpcDificuldadeConfig::getFoco).containsOnly(FocoNpc.MAGICO);
    }

    @Test
    @DisplayName("Deve filtrar por foco MAGICO corretamente")
    void deveFiltrarPorFocoMagico() {
        // Arrange
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Físico " + getUniqueSuffix())
                .foco(FocoNpc.FISICO)
                .ordemExibicao(1)
                .build());
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Mágico Chefe " + getUniqueSuffix())
                .foco(FocoNpc.MAGICO)
                .ordemExibicao(2)
                .build());

        // Act
        List<NpcDificuldadeConfig> magicos = npcDificuldadeService.listarPorFoco(jogo.getId(), FocoNpc.MAGICO);

        // Assert
        assertThat(magicos).hasSize(1);
        assertThat(magicos.get(0).getFoco()).isEqualTo(FocoNpc.MAGICO);
    }

    @Test
    @DisplayName("Deve lançar ConflictException ao criar com nome duplicado no mesmo jogo")
    void deveLancarConflictAoCriarNomeDuplicado() {
        // Arrange
        NpcDificuldadeConfig primeiro = npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Nível Único")
                .foco(FocoNpc.FISICO)
                .ordemExibicao(1)
                .build());

        NpcDificuldadeConfig duplicado = NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome(primeiro.getNome())
                .foco(FocoNpc.MAGICO)
                .ordemExibicao(2)
                .build();

        // Act & Assert
        assertThrows(ConflictException.class, () -> npcDificuldadeService.criar(duplicado));
    }

    @Test
    @DisplayName("Deve permitir mesmo nome de nível em jogos diferentes")
    void devePermitirMesmoNomeEmJogosDiferentes() {
        // Arrange
        Jogo outroJogoLocal = jogoRepository.save(Jogo.builder()
                .nome("Campanha Extra " + getUniqueSuffix())
                .dataInicio(LocalDate.now())
                .jogoAtivo(true)
                .build());

        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Chefe")
                .foco(FocoNpc.FISICO)
                .ordemExibicao(1)
                .build());

        NpcDificuldadeConfig outroJogoConfig = NpcDificuldadeConfig.builder()
                .jogo(outroJogoLocal)
                .nome("Chefe")
                .foco(FocoNpc.MAGICO)
                .ordemExibicao(1)
                .build();

        // Act
        NpcDificuldadeConfig criado = npcDificuldadeService.criar(outroJogoConfig);

        // Assert
        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getJogo().getId()).isEqualTo(outroJogoLocal.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao filtrar por foco quando não há registros do foco")
    void deveRetornarListaVaziaFiltrandoPorFocoSemRegistros() {
        // Arrange
        npcDificuldadeRepository.save(NpcDificuldadeConfig.builder()
                .jogo(jogo)
                .nome("Físico " + getUniqueSuffix())
                .foco(FocoNpc.FISICO)
                .ordemExibicao(1)
                .build());

        // Act
        List<NpcDificuldadeConfig> magicos = npcDificuldadeService.listarPorFoco(jogo.getId(), FocoNpc.MAGICO);

        // Assert
        assertThat(magicos).isEmpty();
    }
}
