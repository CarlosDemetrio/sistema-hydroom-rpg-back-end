package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoAptidaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para AptidaoConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("AptidaoConfiguracaoService - Testes de Integração")
class AptidaoConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<AptidaoConfig, AptidaoConfiguracaoService, ConfiguracaoAptidaoRepository> {

    @Autowired
    private AptidaoConfiguracaoService aptidaoService;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;

    private TipoAptidao tipoFisico;
    private TipoAptidao tipoMental;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        // Criar tipos de aptidão para os testes
        tipoFisico = tipoAptidaoRepository.save(TipoAptidao.builder()
            .jogo(jogo)
            .nome("Física " + getUniqueSuffix())
            .descricao("Aptidões físicas")
            .ordemExibicao(1)
            .build());

        tipoMental = tipoAptidaoRepository.save(TipoAptidao.builder()
            .jogo(jogo)
            .nome("Mental " + getUniqueSuffix())
            .descricao("Aptidões mentais")
            .ordemExibicao(2)
            .build());
    }

    @Override
    protected AptidaoConfiguracaoService getService() {
        return aptidaoService;
    }

    @Override
    protected ConfiguracaoAptidaoRepository getRepository() {
        return aptidaoRepository;
    }

    @Override
    protected AptidaoConfig criarConfiguracaoValida(Jogo jogo) {
        TipoAptidao tipo = tipoFisico;
        if (tipoFisico == null || !tipoFisico.getJogo().getId().equals(jogo.getId())) {
            tipo = tipoAptidaoRepository.save(TipoAptidao.builder()
                .jogo(jogo)
                .nome("Tipo Padrão " + getUniqueSuffix())
                .ordemExibicao(1)
                .build());
        }

        return AptidaoConfig.builder()
            .jogo(jogo)
            .tipoAptidao(tipo)
            .nome("Combate " + getUniqueSuffix())
            .descricao("Habilidade em combate corpo a corpo")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected AptidaoConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, AptidaoConfig configuracaoExistente) {
        TipoAptidao tipo = tipoMental;
        if (tipoMental == null || !tipoMental.getJogo().getId().equals(jogo.getId())) {
            tipo = tipoAptidaoRepository.save(TipoAptidao.builder()
                .jogo(jogo)
                .nome("Outro Tipo")
                .ordemExibicao(2)
                .build());
        }

        return AptidaoConfig.builder()
            .jogo(jogo)
            .tipoAptidao(tipo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(AptidaoConfig configuracao) {
        configuracao.setNome("Combate Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setTipoAptidao(tipoMental);
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(AptidaoConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Combate Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getTipoAptidao().getId()).isEqualTo(tipoMental.getId());
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve listar aptidões por tipo")
    void deveListarAptidoesPorTipo() {
        // Arrange
        AptidaoConfig apt1 = AptidaoConfig.builder()
            .jogo(jogo)
            .tipoAptidao(tipoFisico)
            .nome("Combate " + getUniqueSuffix())
            .ordemExibicao(1)
            .build();

        AptidaoConfig apt2 = AptidaoConfig.builder()
            .jogo(jogo)
            .tipoAptidao(tipoFisico)
            .nome("Atletismo " + getUniqueSuffix())
            .ordemExibicao(2)
            .build();

        AptidaoConfig apt3 = AptidaoConfig.builder()
            .jogo(jogo)
            .tipoAptidao(tipoMental)
            .nome("Conhecimento " + getUniqueSuffix())
            .ordemExibicao(1)
            .build();

        aptidaoRepository.save(apt1);
        aptidaoRepository.save(apt2);
        aptidaoRepository.save(apt3);

        // Act
        List<AptidaoConfig> resultado = aptidaoService.listarPorTipo(jogo.getId(), tipoFisico);

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting("tipoAptidao.id").containsOnly(tipoFisico.getId());
    }
}
