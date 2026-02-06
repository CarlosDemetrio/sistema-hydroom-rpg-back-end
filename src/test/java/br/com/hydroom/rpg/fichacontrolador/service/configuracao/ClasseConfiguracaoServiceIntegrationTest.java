package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para ClasseConfiguracaoService.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@DisplayName("ClasseConfiguracaoService - Testes de Integração")
class ClasseConfiguracaoServiceIntegrationTest extends
    BaseConfiguracaoServiceIntegrationTest<ClassePersonagem, ClasseConfiguracaoService, ConfiguracaoClasseRepository> {

    @Autowired
    private ClasseConfiguracaoService classeService;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Override
    protected ClasseConfiguracaoService getService() {
        return classeService;
    }

    @Override
    protected ConfiguracaoClasseRepository getRepository() {
        return classeRepository;
    }

    @Override
    protected ClassePersonagem criarConfiguracaoValida(Jogo jogo) {
        return ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Guerreiro " + getUniqueSuffix())
            .descricao("Classe especializada em combate")
            .ordemExibicao(1)
            .build();
    }

    @Override
    protected ClassePersonagem criarConfiguracaoComNomeDuplicado(Jogo jogo, ClassePersonagem configuracaoExistente) {
        return ClassePersonagem.builder()
            .jogo(jogo)
            .nome(configuracaoExistente.getNome())
            .descricao("Descrição diferente")
            .ordemExibicao(2)
            .build();
    }

    @Override
    protected void atualizarCamposParaTeste(ClassePersonagem configuracao) {
        configuracao.setNome("Guerreiro Atualizado");
        configuracao.setDescricao("Nova descrição");
        configuracao.setOrdemExibicao(10);
    }

    @Override
    protected void verificarCamposAtualizados(ClassePersonagem configuracao) {
        assertThat(configuracao.getNome()).isEqualTo("Guerreiro Atualizado");
        assertThat(configuracao.getDescricao()).isEqualTo("Nova descrição");
        assertThat(configuracao.getOrdemExibicao()).isEqualTo(10);
    }
}
