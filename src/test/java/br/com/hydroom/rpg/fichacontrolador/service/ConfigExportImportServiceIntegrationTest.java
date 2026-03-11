package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ConfigImportRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ConfigExportResponse;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("ConfigExportImportService — Testes de Integração")
class ConfigExportImportServiceIntegrationTest {

    @Autowired private ConfigExportImportService configExportImportService;
    @Autowired private JogoRepository jogoRepository;
    @Autowired private JogoParticipanteRepository jogoParticipanteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ConfiguracaoAtributoRepository atributoRepository;
    @Autowired private GeneroConfigRepository generoRepository;

    private static final AtomicInteger counter = new AtomicInteger();

    private Usuario mestre;
    private Jogo jogoA;
    private Jogo jogoB;

    @BeforeEach
    void setUp() {
        int id = counter.incrementAndGet();
        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Export " + id)
                .email("mestre-exp-" + id + "@test.com")
                .provider("google")
                .providerId("google-exp-" + id)
                .build());

        setAuthentication(mestre.getEmail(), "ROLE_MESTRE");

        jogoA = jogoRepository.save(Jogo.builder().nome("Jogo A").jogoAtivo(true).build());
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogoA).usuario(mestre).role(RoleJogo.MESTRE)
                .status(StatusParticipante.APROVADO).build());

        jogoB = jogoRepository.save(Jogo.builder().nome("Jogo B").jogoAtivo(false).build());
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogoB).usuario(mestre).role(RoleJogo.MESTRE)
                .status(StatusParticipante.APROVADO).build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("deve exportar configurações do jogo com nome correto")
    void deveExportarConfiguracoesDoJogo() {
        generoRepository.save(GeneroConfig.builder()
                .jogo(jogoA).nome("Masculino").ordemExibicao(1).build());

        ConfigExportResponse export = configExportImportService.exportar(jogoA.getId());

        assertThat(export).isNotNull();
        assertThat(export.jogoNome()).isEqualTo("Jogo A");
        assertThat(export.generos()).hasSize(1);
        assertThat(export.generos().get(0).nome()).isEqualTo("Masculino");
    }

    @Test
    @DisplayName("deve importar configurações de um jogo para outro")
    void deveImportarConfiguracoesParaOutroJogo() {
        atributoRepository.save(AtributoConfig.builder()
                .jogo(jogoA).nome("Força").abreviacao("FOR").ordemExibicao(1).build());

        ConfigExportResponse export = configExportImportService.exportar(jogoA.getId());

        ConfigImportRequest importRequest = new ConfigImportRequest(
                export.atributos(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        configExportImportService.importar(jogoB.getId(), importRequest);

        List<AtributoConfig> atributosB = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogoB.getId());
        assertThat(atributosB).hasSize(1);
        assertThat(atributosB.get(0).getNome()).isEqualTo("Força");
    }

    @Test
    @DisplayName("deve ignorar itens com nome duplicado na importação")
    void deveIgnorarNomesDuplicadosNaImportacao() {
        atributoRepository.save(AtributoConfig.builder()
                .jogo(jogoA).nome("Força").abreviacao("FOR").ordemExibicao(1).build());

        ConfigExportResponse export = configExportImportService.exportar(jogoA.getId());
        ConfigImportRequest importRequest = new ConfigImportRequest(
                export.atributos(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        // Importar duas vezes
        configExportImportService.importar(jogoB.getId(), importRequest);
        configExportImportService.importar(jogoB.getId(), importRequest);

        List<AtributoConfig> atributosB = atributoRepository.findByJogoIdOrderByOrdemExibicao(jogoB.getId());
        assertThat(atributosB).hasSize(1); // Não deve duplicar
    }

    private void setAuthentication(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority(role))));
    }
}
