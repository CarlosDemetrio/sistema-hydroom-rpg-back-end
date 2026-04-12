package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.dto.response.configuracao.ClasseResponse;
import br.com.hydroom.rpg.fichacontrolador.mapper.configuracao.ClassePersonagemMapper;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseAptidaoBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.TipoAptidao;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.TipoAptidaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para os sub-recursos de ClasseConfiguracaoService:
 * adicionarBonus, listarBonus, removerBonus, adicionarAptidaoBonus,
 * listarAptidaoBonus, removerAptidaoBonus e branch de nome duplicado na atualização.
 *
 * @author Ficha Controlador Team
 * @since 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("ClasseConfiguracaoService - Sub-Recursos e Branches de Atualização")
class ClasseSubRecursosIntegrationTest {

    private static final AtomicInteger counter = new AtomicInteger(1);

    @Autowired
    private ClasseConfiguracaoService classeService;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private BonusConfigRepository bonusRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

    @Autowired
    private TipoAptidaoRepository tipoAptidaoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ClassePersonagemMapper classeMapper;

    private Jogo jogo;
    private Jogo outroJogo;
    private ClassePersonagem classe;
    private BonusConfig bonus;
    private AptidaoConfig aptidao;

    @BeforeEach
    void setUp() {
        int n = counter.getAndIncrement();

        jogo = jogoRepository.save(Jogo.builder()
                .nome("Jogo Classe Sub " + n)
                .descricao("Jogo para testes de sub-recursos de classe")
                .dataInicio(LocalDate.now())
                .jogoAtivo(true)
                .build());

        outroJogo = jogoRepository.save(Jogo.builder()
                .nome("Outro Jogo Classe " + n)
                .descricao("Outro jogo para teste de isolamento")
                .dataInicio(LocalDate.now())
                .jogoAtivo(true)
                .build());

        TipoAptidao tipo = tipoAptidaoRepository.save(TipoAptidao.builder()
                .jogo(jogo)
                .nome("Físico " + n)
                .ordemExibicao(1)
                .build());

        classe = classeRepository.save(ClassePersonagem.builder()
                .jogo(jogo)
                .nome("Guerreiro " + n)
                .ordemExibicao(1)
                .build());

        bonus = bonusRepository.save(BonusConfig.builder()
                .jogo(jogo)
                .nome("BBA " + n)
                .sigla("B" + (n % 1000))
                .formulaBase("nivel")
                .ordemExibicao(1)
                .build());

        aptidao = aptidaoRepository.save(AptidaoConfig.builder()
                .jogo(jogo)
                .tipoAptidao(tipo)
                .nome("Espada " + n)
                .ordemExibicao(1)
                .build());
    }

    // =========================================================================
    // validarAntesAtualizar - branch nome diferente com conflito
    // =========================================================================

    @Test
    @DisplayName("deve lançar ConflictException ao atualizar classe com nome já existente")
    void deveLancarConflictAoAtualizarParaNomeDuplicado() {
        // Arrange
        int n = counter.getAndIncrement();
        ClassePersonagem outraClasse = classeService.criar(ClassePersonagem.builder()
                .jogo(jogo)
                .nome("Mago " + n)
                .ordemExibicao(2)
                .build());

        ClassePersonagem atualizacao = ClassePersonagem.builder()
                .nome(outraClasse.getNome()) // nome já existe
                .descricao("desc")
                .ordemExibicao(1)
                .build();

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> classeService.atualizar(classe.getId(), atualizacao));
    }

    // =========================================================================
    // adicionarBonus
    // =========================================================================

    @Test
    @DisplayName("deve adicionar bônus à classe com sucesso")
    void deveAdicionarBonusComSucesso() {
        // Act
        ClasseBonus cb = classeService.adicionarBonus(classe.getId(), bonus.getId(), BigDecimal.ONE);

        // Assert
        assertThat(cb.getId()).isNotNull();
        assertThat(cb.getClasse().getId()).isEqualTo(classe.getId());
        assertThat(cb.getBonus().getId()).isEqualTo(bonus.getId());
        assertThat(cb.getValorPorNivel()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException ao adicionar bônus com bonusId inexistente")
    void deveLancarNotFoundAoAdicionarBonusInexistente() {
        assertThrows(ResourceNotFoundException.class,
                () -> classeService.adicionarBonus(classe.getId(), 99999L, BigDecimal.ONE));
    }

    @Test
    @DisplayName("deve lançar ValidationException ao adicionar bônus de outro jogo")
    void deveLancarValidationAoAdicionarBonusDeOutroJogo() {
        // Arrange
        int n = counter.getAndIncrement();
        BonusConfig bonusOutroJogo = bonusRepository.save(BonusConfig.builder()
                .jogo(outroJogo)
                .nome("BBM Outro " + n)
                .sigla("OT" + (n % 100))
                .formulaBase("nivel")
                .ordemExibicao(1)
                .build());

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> classeService.adicionarBonus(classe.getId(), bonusOutroJogo.getId(), BigDecimal.ONE));
    }

    @Test
    @DisplayName("deve lançar ConflictException ao adicionar bônus duplicado à classe")
    void deveLancarConflictAoAdicionarBonusDuplicado() {
        // Arrange
        classeService.adicionarBonus(classe.getId(), bonus.getId(), BigDecimal.ONE);

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> classeService.adicionarBonus(classe.getId(), bonus.getId(), BigDecimal.TEN));
    }

    // =========================================================================
    // listarBonus
    // =========================================================================

    @Test
    @DisplayName("deve listar bônus da classe")
    void deveListarBonusDaClasse() {
        // Arrange
        classeService.adicionarBonus(classe.getId(), bonus.getId(), BigDecimal.ONE);

        // Act
        List<ClasseBonus> lista = classeService.listarBonus(classe.getId());

        // Assert
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getBonus().getId()).isEqualTo(bonus.getId());
    }

    @Test
    @DisplayName("deve retornar lista vazia quando classe não tem bônus")
    void deveRetornarListaVaziaQuandoClasseSemBonus() {
        List<ClasseBonus> lista = classeService.listarBonus(classe.getId());
        assertThat(lista).isEmpty();
    }

    // =========================================================================
    // removerBonus
    // =========================================================================

    @Test
    @DisplayName("deve remover bônus da classe com sucesso")
    void deveRemoverBonusDaClasse() {
        // Arrange
        ClasseBonus cb = classeService.adicionarBonus(classe.getId(), bonus.getId(), BigDecimal.ONE);

        // Act
        classeService.removerBonus(classe.getId(), cb.getId());

        // Assert
        assertThat(classeService.listarBonus(classe.getId())).isEmpty();
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException ao remover bônus com id inexistente")
    void deveLancarNotFoundAoRemoverBonusInexistente() {
        assertThrows(ResourceNotFoundException.class,
                () -> classeService.removerBonus(classe.getId(), 99999L));
    }

    @Test
    @DisplayName("deve lançar ValidationException ao remover bônus que pertence a outra classe")
    void deveLancarValidationAoRemoverBonusDeOutraClasse() {
        // Arrange
        int n = counter.getAndIncrement();
        ClassePersonagem outraClasse = classeService.criar(ClassePersonagem.builder()
                .jogo(jogo)
                .nome("Ladino " + n)
                .ordemExibicao(2)
                .build());
        ClasseBonus cb = classeService.adicionarBonus(outraClasse.getId(), bonus.getId(), BigDecimal.ONE);

        // Act & Assert — tenta remover pelo id da classe errada
        assertThrows(ValidationException.class,
                () -> classeService.removerBonus(classe.getId(), cb.getId()));
    }

    // =========================================================================
    // adicionarAptidaoBonus
    // =========================================================================

    @Test
    @DisplayName("deve adicionar aptidão-bônus à classe com sucesso")
    void deveAdicionarAptidaoBonusComSucesso() {
        // Act
        ClasseAptidaoBonus cab = classeService.adicionarAptidaoBonus(classe.getId(), aptidao.getId(), 3);

        // Assert
        assertThat(cab.getId()).isNotNull();
        assertThat(cab.getClasse().getId()).isEqualTo(classe.getId());
        assertThat(cab.getAptidao().getId()).isEqualTo(aptidao.getId());
        assertThat(cab.getBonus()).isEqualTo(3);
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException ao adicionar aptidão-bônus com aptidaoId inexistente")
    void deveLancarNotFoundAoAdicionarAptidaoInexistente() {
        assertThrows(ResourceNotFoundException.class,
                () -> classeService.adicionarAptidaoBonus(classe.getId(), 99999L, 2));
    }

    @Test
    @DisplayName("deve lançar ValidationException ao adicionar aptidão-bônus de outro jogo")
    void deveLancarValidationAoAdicionarAptidaoBonusDeOutroJogo() {
        // Arrange
        int n = counter.getAndIncrement();
        TipoAptidao tipoOutroJogo = tipoAptidaoRepository.save(TipoAptidao.builder()
                .jogo(outroJogo)
                .nome("Mental Outro " + n)
                .ordemExibicao(1)
                .build());
        AptidaoConfig aptidaoOutroJogo = aptidaoRepository.save(AptidaoConfig.builder()
                .jogo(outroJogo)
                .tipoAptidao(tipoOutroJogo)
                .nome("Furtividade " + n)
                .ordemExibicao(1)
                .build());

        // Act & Assert
        assertThrows(ValidationException.class,
                () -> classeService.adicionarAptidaoBonus(classe.getId(), aptidaoOutroJogo.getId(), 2));
    }

    @Test
    @DisplayName("deve lançar ConflictException ao adicionar aptidão-bônus duplicado")
    void deveLancarConflictAoAdicionarAptidaoBonusDuplicado() {
        // Arrange
        classeService.adicionarAptidaoBonus(classe.getId(), aptidao.getId(), 3);

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> classeService.adicionarAptidaoBonus(classe.getId(), aptidao.getId(), 5));
    }

    // =========================================================================
    // listarAptidaoBonus
    // =========================================================================

    @Test
    @DisplayName("deve listar aptidão-bônus da classe")
    void deveListarAptidaoBonusDaClasse() {
        // Arrange
        classeService.adicionarAptidaoBonus(classe.getId(), aptidao.getId(), 3);

        // Act
        List<ClasseAptidaoBonus> lista = classeService.listarAptidaoBonus(classe.getId());

        // Assert
        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getBonus()).isEqualTo(3);
    }

    @Test
    @DisplayName("deve retornar lista vazia quando classe não tem aptidão-bônus")
    void deveRetornarListaVaziaQuandoClasseSemAptidaoBonus() {
        List<ClasseAptidaoBonus> lista = classeService.listarAptidaoBonus(classe.getId());
        assertThat(lista).isEmpty();
    }

    // =========================================================================
    // removerAptidaoBonus
    // =========================================================================

    @Test
    @DisplayName("deve remover aptidão-bônus da classe com sucesso")
    void deveRemoverAptidaoBonusDaClasse() {
        // Arrange
        ClasseAptidaoBonus cab = classeService.adicionarAptidaoBonus(classe.getId(), aptidao.getId(), 3);

        // Act
        classeService.removerAptidaoBonus(classe.getId(), cab.getId());

        // Assert
        assertThat(classeService.listarAptidaoBonus(classe.getId())).isEmpty();
    }

    @Test
    @DisplayName("deve lançar ResourceNotFoundException ao remover aptidão-bônus com id inexistente")
    void deveLancarNotFoundAoRemoverAptidaoBonusInexistente() {
        assertThrows(ResourceNotFoundException.class,
                () -> classeService.removerAptidaoBonus(classe.getId(), 99999L));
    }

    @Test
    @DisplayName("deve lançar ValidationException ao remover aptidão-bônus que pertence a outra classe")
    void deveLancarValidationAoRemoverAptidaoBonusDeOutraClasse() {
        // Arrange
        int n = counter.getAndIncrement();
        ClassePersonagem outraClasse = classeService.criar(ClassePersonagem.builder()
                .jogo(jogo)
                .nome("Paladino " + n)
                .ordemExibicao(3)
                .build());
        ClasseAptidaoBonus cab = classeService.adicionarAptidaoBonus(outraClasse.getId(), aptidao.getId(), 2);

        // Act & Assert — tenta remover pelo id da classe errada
        assertThrows(ValidationException.class,
                () -> classeService.removerAptidaoBonus(classe.getId(), cab.getId()));
    }

    @Test
    @DisplayName("deve mapear listagem de classes com bonus e aptidao sem erro de concorrencia")
    void deveMapearListagemDeClassesSemErroDeConcorrencia() {
        classeService.adicionarBonus(classe.getId(), bonus.getId(), BigDecimal.ONE);
        classeService.adicionarAptidaoBonus(classe.getId(), aptidao.getId(), 3);
        entityManager.flush();
        entityManager.clear();

        List<ClasseResponse> responses = classeService.listar(jogo.getId()).stream()
            .map(classeMapper::toResponse)
            .toList();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).bonusConfig()).hasSize(1);
        assertThat(responses.get(0).aptidaoBonus()).hasSize(1);
    }
}
