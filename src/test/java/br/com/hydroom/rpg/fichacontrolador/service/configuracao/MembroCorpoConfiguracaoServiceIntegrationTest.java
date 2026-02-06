package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.MembroCorpoConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para MembroCorpoConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("MembroCorpoConfiguracaoService - Testes de Integração")
class MembroCorpoConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<MembroCorpoConfig, MembroCorpoConfiguracaoService, MembroCorpoConfigRepository> {

    @Autowired
    private MembroCorpoConfiguracaoService membroCorpoService;

    @Autowired
    private MembroCorpoConfigRepository membroCorpoRepository;

    @Override
    protected MembroCorpoConfiguracaoService getService() {
        return membroCorpoService;
    }

    @Override
    protected MembroCorpoConfigRepository getRepository() {
        return membroCorpoRepository;
    }

    @Override
    protected MembroCorpoConfig criarConfiguracaoValida(Jogo jogo) {
        return MembroCorpoConfig.builder()
            .jogo(jogo)
            .nome("Cabeça " + getUniqueSuffix())
            .porcentagemVida(new BigDecimal("0.25"))
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected MembroCorpoConfig criarConfiguracaoComNomeDuplicado(Jogo jogo, MembroCorpoConfig configuracaoExistente) {
        return null; // MembroCorpoConfig não valida nome duplicado
    }

    @Override
    protected void atualizarCamposParaTeste(MembroCorpoConfig configuracao) {
        configuracao.setNome("Cabeça Atualizada");
        configuracao.setPorcentagemVida(new BigDecimal("0.30"));
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(MembroCorpoConfig configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Cabeça Atualizada");
        assertThat(configuracao.getPorcentagemVida()).isEqualByComparingTo(new BigDecimal("0.30"));
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
