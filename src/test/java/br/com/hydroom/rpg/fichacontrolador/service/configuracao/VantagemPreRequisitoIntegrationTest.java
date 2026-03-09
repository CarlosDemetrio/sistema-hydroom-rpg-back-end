package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemPreRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para lógica de pré-requisitos e detecção de ciclos em vantagens.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("VantagemPreRequisito - Detecção de Ciclos")
class VantagemPreRequisitoIntegrationTest {

    @Autowired
    private VantagemConfiguracaoService vantagemService;

    @Autowired
    private VantagemConfigRepository vantagemRepository;

    @Autowired
    private VantagemPreRequisitoRepository prerequisitoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Jogo jogo;

    @BeforeEach
    void setUp() {
        prerequisitoRepository.deleteAll();
        vantagemRepository.deleteAll();
        jogoRepository.deleteAll();

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Jogo Teste PR " + counter.getAndIncrement())
            .jogoAtivo(true)
            .dataInicio(LocalDate.now())
            .build());
    }

    private VantagemConfig criarVantagem(String nome) {
        return vantagemService.criar(VantagemConfig.builder()
            .jogo(jogo)
            .nome(nome)
            .nivelMaximo(5)
            .formulaCusto("custo_base")
            .build());
    }

    @Test
    @DisplayName("deve adicionar pré-requisito válido sem ciclo")
    void deveAdicionarPreRequisitoSemCiclo() {
        VantagemConfig a = criarVantagem("Vantagem A");
        VantagemConfig b = criarVantagem("Vantagem B");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);

        assertThat(pr.getId()).isNotNull();
        assertThat(pr.getVantagem().getId()).isEqualTo(a.getId());
        assertThat(pr.getRequisito().getId()).isEqualTo(b.getId());
        assertThat(pr.getNivelMinimo()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve rejeitar auto-referência")
    void deveRejeitarAutoReferencia() {
        VantagemConfig a = criarVantagem("Vantagem Auto");

        assertThrows(ConflictException.class,
            () -> vantagemService.adicionarPreRequisito(a.getId(), a.getId(), 1));
    }

    @Test
    @DisplayName("deve rejeitar ciclo direto: A requer B, B requer A")
    void deveRejeitarCicloDireto() {
        VantagemConfig a = criarVantagem("Vantagem Ciclo A");
        VantagemConfig b = criarVantagem("Vantagem Ciclo B");

        vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);

        assertThrows(ConflictException.class,
            () -> vantagemService.adicionarPreRequisito(b.getId(), a.getId(), 1));
    }

    @Test
    @DisplayName("deve rejeitar ciclo transitivo: A requer B, B requer C, C requer A")
    void deveRejeitarCicloTransitivo() {
        VantagemConfig a = criarVantagem("Vantagem Trans A");
        VantagemConfig b = criarVantagem("Vantagem Trans B");
        VantagemConfig c = criarVantagem("Vantagem Trans C");

        vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);
        vantagemService.adicionarPreRequisito(b.getId(), c.getId(), 1);

        assertThrows(ConflictException.class,
            () -> vantagemService.adicionarPreRequisito(c.getId(), a.getId(), 1));
    }

    @Test
    @DisplayName("deve rejeitar pré-requisito duplicado")
    void deveRejeitarDuplicado() {
        VantagemConfig a = criarVantagem("Vantagem Dup A");
        VantagemConfig b = criarVantagem("Vantagem Dup B");

        vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);

        assertThrows(ConflictException.class,
            () -> vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 2));
    }

    @Test
    @DisplayName("deve rejeitar pré-requisito de outro jogo")
    void deveRejeitarPreRequisitoDeOutroJogo() {
        Jogo outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outro Jogo " + counter.getAndIncrement())
            .jogoAtivo(true)
            .dataInicio(LocalDate.now())
            .build());

        VantagemConfig a = criarVantagem("Vantagem Jogo1");
        VantagemConfig b = vantagemService.criar(VantagemConfig.builder()
            .jogo(outroJogo)
            .nome("Vantagem Jogo2")
            .nivelMaximo(3)
            .formulaCusto("custo_base")
            .build());

        assertThrows(ValidationException.class,
            () -> vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1));
    }

    @Test
    @DisplayName("deve listar pré-requisitos de uma vantagem")
    void deveListarPreRequisitos() {
        VantagemConfig a = criarVantagem("Vantagem Lista A");
        VantagemConfig b = criarVantagem("Vantagem Lista B");
        VantagemConfig c = criarVantagem("Vantagem Lista C");

        vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);
        vantagemService.adicionarPreRequisito(a.getId(), c.getId(), 2);

        List<VantagemPreRequisito> lista = vantagemService.listarPreRequisitos(a.getId());

        assertThat(lista).hasSize(2);
    }

    @Test
    @DisplayName("deve remover pré-requisito")
    void deveRemoverPreRequisito() {
        VantagemConfig a = criarVantagem("Vantagem Rem A");
        VantagemConfig b = criarVantagem("Vantagem Rem B");

        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);

        vantagemService.removerPreRequisito(pr.getId());

        assertThat(vantagemService.listarPreRequisitos(a.getId())).isEmpty();
    }

    @Test
    @DisplayName("deve aceitar D requer A quando A requer B (sem ciclo)")
    void deveAceitarNovaDependenciaSemCiclo() {
        VantagemConfig a = criarVantagem("Vantagem NC A");
        VantagemConfig b = criarVantagem("Vantagem NC B");
        VantagemConfig d = criarVantagem("Vantagem NC D");

        vantagemService.adicionarPreRequisito(a.getId(), b.getId(), 1);

        // D requer A — não forma ciclo
        VantagemPreRequisito pr = vantagemService.adicionarPreRequisito(d.getId(), a.getId(), 1);
        assertThat(pr.getId()).isNotNull();
    }
}
