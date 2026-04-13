package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.HabilidadeConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.HabilidadeConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para HabilidadeConfigService.
 *
 * <p>Além dos ~10 testes herdados da base, cobre cenários específicos de habilidade:
 * campos opcionais nulos e ordenação por ordemExibicao.</p>
 */
@DisplayName("HabilidadeConfigService - Testes de Integração")
class HabilidadeConfigServiceIntegrationTest extends
        BaseConfiguracaoServiceIntegrationTest<HabilidadeConfig, HabilidadeConfigService, HabilidadeConfigRepository> {

    @Autowired
    private HabilidadeConfigService habilidadeService;

    @Autowired
    private HabilidadeConfigRepository habilidadeRepository;

    @Override
    protected HabilidadeConfigService getService() {
        return habilidadeService;
    }

    @Override
    protected HabilidadeConfigRepository getRepository() {
        return habilidadeRepository;
    }

    @Override
    protected HabilidadeConfig criarConfiguracaoValida(Jogo jogo) {
        String suffix = getUniqueSuffix();
        return HabilidadeConfig.builder()
            .jogo(jogo)
            .nome("Golpe Brutal " + suffix)
            .descricao("Um ataque poderoso que causa dano máximo")
            .danoEfeito("2D6+FOR de dano físico")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected HabilidadeConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, HabilidadeConfig configuracaoExistente) {
        return HabilidadeConfig.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(HabilidadeConfig configuracao) {
        configuracao.setNome("Golpe Devastador");
        configuracao.setDanoEfeito("3D8+FOR de dano físico");
    }

    @Override
    protected void verificarCamposAtualizados(HabilidadeConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Golpe Devastador");
        assertThat(configuracao.getDanoEfeito()).isEqualTo("3D8+FOR de dano físico");
    }

    // =========================================================
    // CENÁRIOS ESPECÍFICOS DE HABILIDADE
    // =========================================================

    @Test
    @DisplayName("HAB-01: Deve criar habilidade com campos opcionais nulos")
    void deveCriarHabilidadeComCamposOpcionaisNulos() {
        // Arrange
        HabilidadeConfig habilidade = HabilidadeConfig.builder()
            .jogo(jogo)
            .nome("Habilidade Simples " + getUniqueSuffix())
            .descricao(null)
            .danoEfeito(null)
            .ordemExibicao(10)
            .build();

        // Act
        HabilidadeConfig criada = habilidadeService.criar(habilidade);

        // Assert
        assertThat(criada.getId()).isNotNull();
        assertThat(criada.getNome()).startsWith("Habilidade Simples");
        assertThat(criada.getDescricao()).isNull();
        assertThat(criada.getDanoEfeito()).isNull();
    }

    @Test
    @DisplayName("HAB-02: Deve listar habilidades ordenadas por ordemExibicao")
    void deveListarHabilidadesOrdenadasPorOrdemExibicao() {
        // Arrange
        String suffix = getUniqueSuffix();
        habilidadeRepository.save(HabilidadeConfig.builder()
            .jogo(jogo).nome("Habilidade Z " + suffix).ordemExibicao(10).build());
        habilidadeRepository.save(HabilidadeConfig.builder()
            .jogo(jogo).nome("Habilidade A " + suffix).ordemExibicao(1).build());
        habilidadeRepository.save(HabilidadeConfig.builder()
            .jogo(jogo).nome("Habilidade M " + suffix).ordemExibicao(5).build());

        // Act
        List<HabilidadeConfig> lista = habilidadeService.listar(jogo.getId());

        // Assert
        assertThat(lista).hasSize(3);
        assertThat(lista.get(0).getOrdemExibicao()).isEqualTo(1);
        assertThat(lista.get(1).getOrdemExibicao()).isEqualTo(5);
        assertThat(lista.get(2).getOrdemExibicao()).isEqualTo(10);
    }

    @Test
    @DisplayName("HAB-03: Deve criar habilidade com todos os campos preenchidos")
    void deveCriarHabilidadeComTodosCampos() {
        // Arrange
        HabilidadeConfig habilidade = HabilidadeConfig.builder()
            .jogo(jogo)
            .nome("Investida Furiosa " + getUniqueSuffix())
            .descricao("O personagem avança e ataca com força total")
            .danoEfeito("1D12+FOR de dano físico, empurra alvo 2m")
            .ordemExibicao(3)
            .build();

        // Act
        HabilidadeConfig criada = habilidadeService.criar(habilidade);

        // Assert
        assertThat(criada.getId()).isNotNull();
        assertThat(criada.getDescricao()).isEqualTo("O personagem avança e ataca com força total");
        assertThat(criada.getDanoEfeito()).isEqualTo("1D12+FOR de dano físico, empurra alvo 2m");
        assertThat(criada.getOrdemExibicao()).isEqualTo(3);
    }
}
