package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import br.com.hydroom.rpg.fichacontrolador.service.configuracao.RacaConfiguracaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integracao para validacao RacaClassePermitida na criacao de ficha.
 *
 * <p>Cenarios:</p>
 * <ul>
 *   <li>Sem restricoes (tabela vazia): qualquer classe e permitida</li>
 *   <li>Com restricoes e classe permitida: criacao sucede</li>
 *   <li>Com restricoes e classe NAO permitida: criacao falha com BusinessException</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("Ficha - Validacao RacaClassePermitida")
class FichaRacaClassePermitidaIntegrationTest {

    @Autowired private FichaService fichaService;
    @Autowired private JogoService jogoService;
    @Autowired private RacaConfiguracaoService racaService;
    @Autowired private FichaRepository fichaRepository;
    @Autowired private FichaAtributoRepository fichaAtributoRepository;
    @Autowired private FichaAptidaoRepository fichaAptidaoRepository;
    @Autowired private FichaBonusRepository fichaBonusRepository;
    @Autowired private FichaVidaRepository fichaVidaRepository;
    @Autowired private FichaVidaMembroRepository fichaVidaMembroRepository;
    @Autowired private FichaEssenciaRepository fichaEssenciaRepository;
    @Autowired private FichaAmeacaRepository fichaAmeacaRepository;
    @Autowired private FichaProspeccaoRepository fichaProspeccaoRepository;
    @Autowired private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;
    @Autowired private JogoRepository jogoRepository;
    @Autowired private JogoParticipanteRepository jogoParticipanteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ConfiguracaoRacaRepository racaRepository;
    @Autowired private ConfiguracaoClasseRepository classeRepository;
    @Autowired private RacaClassePermitidaRepository racaClassePermitidaRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Jogo jogo;
    private Raca raca;
    private ClassePersonagem classeGuerreiro;
    private ClassePersonagem classeMago;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta
        racaClassePermitidaRepository.deleteAll();
        fichaDescricaoFisicaRepository.deleteAll();
        fichaProspeccaoRepository.deleteAll();
        fichaAmeacaRepository.deleteAll();
        fichaEssenciaRepository.deleteAll();
        fichaVidaMembroRepository.deleteAll();
        fichaVidaRepository.deleteAll();
        fichaBonusRepository.deleteAll();
        fichaAptidaoRepository.deleteAll();
        fichaAtributoRepository.deleteAll();
        fichaRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre RCP")
                .email("mestre.rcp" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-rcp-" + n)
                .role("MESTRE")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo RCP " + n)
                .build());

        // Criar raca e classes
        raca = racaRepository.save(Raca.builder()
                .jogo(jogo).nome("Elfo " + n).ordemExibicao(0).build());
        classeGuerreiro = classeRepository.save(ClassePersonagem.builder()
                .jogo(jogo).nome("Guerreiro " + n).ordemExibicao(0).build());
        classeMago = classeRepository.save(ClassePersonagem.builder()
                .jogo(jogo).nome("Mago " + n).ordemExibicao(1).build());
    }

    @Test
    @DisplayName("Deve criar ficha quando nao ha restricao de classe para a raca (tabela vazia)")
    void deveCriarFichaSemRestricaoDeClasse() {
        // Arrange - nenhuma entrada em RacaClassePermitida para esta raca
        var request = new CreateFichaRequest(
                jogo.getId(), "Personagem Livre", null,
                raca.getId(), classeGuerreiro.getId(), null, null, null, false);

        // Act & Assert - deve criar normalmente
        Ficha ficha = assertDoesNotThrow(() -> fichaService.criar(request));
        assertThat(ficha.getId()).isNotNull();
        assertThat(ficha.getRaca().getId()).isEqualTo(raca.getId());
        assertThat(ficha.getClasse().getId()).isEqualTo(classeGuerreiro.getId());
    }

    @Test
    @DisplayName("Deve criar ficha quando classe esta na lista de classes permitidas da raca")
    void deveCriarFichaComClassePermitida() {
        // Arrange - permitir apenas Guerreiro para Elfo
        racaService.permitirClasse(raca.getId(), classeGuerreiro.getId());

        var request = new CreateFichaRequest(
                jogo.getId(), "Elfo Guerreiro", null,
                raca.getId(), classeGuerreiro.getId(), null, null, null, false);

        // Act & Assert
        Ficha ficha = assertDoesNotThrow(() -> fichaService.criar(request));
        assertThat(ficha.getRaca().getId()).isEqualTo(raca.getId());
        assertThat(ficha.getClasse().getId()).isEqualTo(classeGuerreiro.getId());
    }

    @Test
    @DisplayName("Deve rejeitar ficha quando classe NAO esta na lista de classes permitidas da raca")
    void deveRejeitarFichaComClasseNaoPermitida() {
        // Arrange - permitir apenas Guerreiro para Elfo (Mago nao esta na lista)
        racaService.permitirClasse(raca.getId(), classeGuerreiro.getId());

        var request = new CreateFichaRequest(
                jogo.getId(), "Elfo Mago Proibido", null,
                raca.getId(), classeMago.getId(), null, null, null, false);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> fichaService.criar(request));
        assertThat(ex.getMessage()).contains("não é permitida para a raça");
    }

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
