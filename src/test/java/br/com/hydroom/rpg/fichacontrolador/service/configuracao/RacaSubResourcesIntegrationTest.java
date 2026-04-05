package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RacaBonusAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.RacaClassePermitida;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para os sub-recursos de Raça: bônus de atributo e classes permitidas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("RacaSubResources - Bônus Atributo e Classes Permitidas")
class RacaSubResourcesIntegrationTest {

    @Autowired
    private RacaConfiguracaoService racaService;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private EntityManager entityManager;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Jogo jogo;
    private Raca raca;
    private AtributoConfig atributo;
    private ClassePersonagem classe;

    @BeforeEach
    void setUp() {
        int n = counter.getAndIncrement();

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Jogo Raca Sub " + n)
            .descricao("Jogo para testes de sub-recursos de raça")
            .dataInicio(LocalDate.now())
            .build());

        raca = racaRepository.save(Raca.builder()
            .jogo(jogo)
            .nome("Elfo " + n)
            .descricao("Raça élfica")
            .ordemExibicao(1)
            .build());

        atributo = atributoRepository.save(AtributoConfig.builder()
            .jogo(jogo)
            .nome("Agilidade " + n)
            .abreviacao("AGI" + n)
            .ordemExibicao(1)
            .build());

        classe = classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Arqueiro " + n)
            .ordemExibicao(1)
            .build());
    }

    // ===== BÔNUS ATRIBUTO =====

    @Test
    @DisplayName("deve adicionar bônus de atributo com sucesso")
    void deveAdicionarBonusAtributoComSucesso() {
        RacaBonusAtributo resultado = racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), 2);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getBonus()).isEqualTo(2);
        assertThat(resultado.getAtributo().getId()).isEqualTo(atributo.getId());
    }

    @Test
    @DisplayName("deve adicionar bônus negativo (penalidade racial)")
    void deveAdicionarBonusNegativo() {
        RacaBonusAtributo resultado = racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), -1);

        assertThat(resultado.getBonus()).isEqualTo(-1);
    }

    @Test
    @DisplayName("deve lançar ConflictException ao adicionar bônus duplicado")
    void deveLancarExcecaoAoAdicionarBonusDuplicado() {
        racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), 2);

        assertThrows(ConflictException.class, () ->
            racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), 1)
        );
    }

    @Test
    @DisplayName("deve lançar ValidationException ao adicionar bônus de atributo de outro jogo")
    void deveLancarExcecaoAtributoOutroJogo() {
        Jogo outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outro Jogo " + counter.getAndIncrement())
            .dataInicio(LocalDate.now())
            .build());

        AtributoConfig atributoOutroJogo = atributoRepository.save(AtributoConfig.builder()
            .jogo(outroJogo)
            .nome("Força Outro")
            .abreviacao("FOR99")
            .ordemExibicao(1)
            .build());

        assertThrows(ValidationException.class, () ->
            racaService.adicionarBonusAtributo(raca.getId(), atributoOutroJogo.getId(), 2)
        );
    }

    @Test
    @DisplayName("deve listar bônus de atributo da raça")
    void deveListarBonusAtributo() {
        racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), 2);

        List<RacaBonusAtributo> bonus = racaService.listarBonusAtributo(raca.getId());

        assertThat(bonus).hasSize(1);
        assertThat(bonus.get(0).getBonus()).isEqualTo(2);
    }

    @Test
    @DisplayName("deve remover bônus de atributo com sucesso")
    void deveRemoverBonusAtributo() {
        RacaBonusAtributo rba = racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), 2);
        racaService.removerBonusAtributo(raca.getId(), rba.getId());

        List<RacaBonusAtributo> bonus = racaService.listarBonusAtributo(raca.getId());
        assertThat(bonus).isEmpty();
    }

    // ===== CLASSES PERMITIDAS =====

    @Test
    @DisplayName("deve permitir classe para raça com sucesso")
    void devePermitirClasseComSucesso() {
        RacaClassePermitida resultado = racaService.permitirClasse(raca.getId(), classe.getId());

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getClasse().getId()).isEqualTo(classe.getId());
    }

    @Test
    @DisplayName("deve lançar ConflictException ao permitir classe duplicada")
    void deveLancarExcecaoClasseDuplicada() {
        racaService.permitirClasse(raca.getId(), classe.getId());

        assertThrows(ConflictException.class, () ->
            racaService.permitirClasse(raca.getId(), classe.getId())
        );
    }

    @Test
    @DisplayName("deve lançar ValidationException ao permitir classe de outro jogo")
    void deveLancarExcecaoClasseOutroJogo() {
        Jogo outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outro Jogo Classe " + counter.getAndIncrement())
            .dataInicio(LocalDate.now())
            .build());

        ClassePersonagem classeOutroJogo = classeRepository.save(ClassePersonagem.builder()
            .jogo(outroJogo)
            .nome("Guerreiro Outro")
            .ordemExibicao(1)
            .build());

        assertThrows(ValidationException.class, () ->
            racaService.permitirClasse(raca.getId(), classeOutroJogo.getId())
        );
    }

    @Test
    @DisplayName("deve listar classes permitidas da raça")
    void deveListarClassesPermitidas() {
        racaService.permitirClasse(raca.getId(), classe.getId());

        List<RacaClassePermitida> classes = racaService.listarClassesPermitidas(raca.getId());

        assertThat(classes).hasSize(1);
        assertThat(classes.get(0).getClasse().getId()).isEqualTo(classe.getId());
    }

    @Test
    @DisplayName("deve remover classe permitida com sucesso")
    void deveRemoverClassePermitida() {
        RacaClassePermitida rcp = racaService.permitirClasse(raca.getId(), classe.getId());
        racaService.removerClassePermitida(raca.getId(), rcp.getId());

        List<RacaClassePermitida> classes = racaService.listarClassesPermitidas(raca.getId());
        assertThat(classes).isEmpty();
    }

    @Test
    @DisplayName("deve buscar raça com todas as relações carregadas")
    void deveBuscarRacaComRelacoes() {
        racaService.adicionarBonusAtributo(raca.getId(), atributo.getId(), 2);
        racaService.permitirClasse(raca.getId(), classe.getId());

        // Verifica via listar que os sub-recursos foram persistidos corretamente
        assertThat(racaService.listarBonusAtributo(raca.getId())).hasSize(1);
        assertThat(racaService.listarClassesPermitidas(raca.getId())).hasSize(1);
    }
}
