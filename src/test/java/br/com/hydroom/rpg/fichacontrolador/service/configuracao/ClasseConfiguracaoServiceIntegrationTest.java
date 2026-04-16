package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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

    // =========================================================
    // BUG-007: listar() deve inicializar bonusConfig e aptidaoBonus
    // para não causar LazyInitializationException no mapper
    // =========================================================

    @Test
    @DisplayName("BUG-007: listar classes deve retornar bonusConfig e aptidaoBonus inicializados")
    void deveListarClassesComCollectionsBonusInicializadas() {
        // Arrange
        ClassePersonagem classe = classeService.criar(
            ClassePersonagem.builder()
                .jogo(jogo)
                .nome("Paladino " + getUniqueSuffix())
                .descricao("Classe sagrada")
                .ordemExibicao(1)
                .build());

        // Act — listar() chamado fora de transação do service (simula contexto do controller)
        List<ClassePersonagem> resultado = classeService.listar(jogo.getId());

        // Assert — coleções devem estar acessíveis (não lançar LazyInitializationException)
        assertThat(resultado).hasSize(1);
        ClassePersonagem found = resultado.getFirst();
        assertThat(found.getBonusConfig()).isNotNull();
        assertThat(found.getAptidaoBonus()).isNotNull();
        // Acesso às coleções NÃO deve lançar LazyInitializationException
        assertThat(found.getBonusConfig().size()).isGreaterThanOrEqualTo(0);
        assertThat(found.getAptidaoBonus().size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("BUG-007: criar sem ordemExibicao deve preencher com MAX+1 automaticamente")
    void deveCriarClasseSemOrdemExibicaoComMaxMaisUm() {
        // Arrange — salvar classe com ordem explícita
        classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo).nome("Mago " + getUniqueSuffix()).ordemExibicao(3).build());

        // Act — criar sem fornecer ordemExibicao (null/0)
        ClassePersonagem nova = ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Clérigo " + getUniqueSuffix())
            .ordemExibicao(0)
            .build();
        ClassePersonagem criada = classeService.criar(nova);

        // Assert — ordemExibicao deve ser MAX+1 = 4
        assertThat(criada.getOrdemExibicao()).isEqualTo(4);
    }
}
