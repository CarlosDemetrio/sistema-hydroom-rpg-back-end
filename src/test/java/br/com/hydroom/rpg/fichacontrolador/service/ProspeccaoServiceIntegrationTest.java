package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UsarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.ProspeccaoUsoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.ProspeccaoUsoStatus;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.DadoProspeccaoConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAmeacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaBonusRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaDescricaoFisicaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaEssenciaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaProspeccaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaMembroRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ProspeccaoUsoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para ProspeccaoService.
 *
 * <p>Cobre o fluxo semântico completo de prospecção:</p>
 * <ul>
 *   <li>Jogador usa dado disponível — cria ProspeccaoUso PENDENTE e decrementa quantidade</li>
 *   <li>Jogador usa dado com quantidade=0 — ValidationException</li>
 *   <li>Jogador não pode usar dado de ficha de outro jogador</li>
 *   <li>Mestre reverte uso PENDENTE — status REVERTIDO, quantidade restaurada</li>
 *   <li>Mestre confirma uso PENDENTE — status CONFIRMADO, quantidade inalterada</li>
 *   <li>Tentativa de reverter uso CONFIRMADO — ValidationException</li>
 *   <li>Tentativa de confirmar uso REVERTIDO — ValidationException</li>
 *   <li>Jogador não pode reverter próprio uso</li>
 *   <li>Mestre concede dado — incrementa quantidade</li>
 *   <li>Listar usos por Jogador — apenas da própria ficha</li>
 *   <li>Listar pendentes do jogo — todos os PENDENTES do jogo</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("ProspeccaoService — Testes de Integração")
class ProspeccaoServiceIntegrationTest {

    @Autowired
    private ProspeccaoService prospeccaoService;

    @Autowired
    private FichaService fichaService;

    @Autowired
    private JogoService jogoService;

    @Autowired
    private FichaRepository fichaRepository;

    @Autowired
    private FichaAtributoRepository fichaAtributoRepository;

    @Autowired
    private FichaAptidaoRepository fichaAptidaoRepository;

    @Autowired
    private FichaBonusRepository fichaBonusRepository;

    @Autowired
    private FichaVidaRepository fichaVidaRepository;

    @Autowired
    private FichaVidaMembroRepository fichaVidaMembroRepository;

    @Autowired
    private FichaEssenciaRepository fichaEssenciaRepository;

    @Autowired
    private FichaAmeacaRepository fichaAmeacaRepository;

    @Autowired
    private FichaProspeccaoRepository fichaProspeccaoRepository;

    @Autowired
    private ProspeccaoUsoRepository prospeccaoUsoRepository;

    @Autowired
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoConfigRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;
    private Ficha fichaJogador;
    private Ficha fichaOutroJogador;
    private DadoProspeccaoConfig dadoD6;

    @BeforeEach
    void setUp() {
        prospeccaoUsoRepository.deleteAll();
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
                .nome("Mestre Prosp " + n)
                .email("mestre-prosp" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-prosp-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Prosp " + n)
                .email("jogador-prosp" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-prosp-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Prosp " + n)
                .email("outro-prosp" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-prosp-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Prosp " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        fichaJogador = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Prosp", jogador.getId(), null, null, null, null, null, false));

        fichaOutroJogador = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Outro", outroJogador.getId(), null, null, null, null, null, false));

        // Pegar o DadoProspeccaoConfig criado pelo GameConfigInitializer
        dadoD6 = dadoProspeccaoConfigRepository.findAll().stream()
                .filter(d -> d.getJogo().getId().equals(jogo.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum DadoProspeccaoConfig encontrado para o jogo"));
    }

    // =========================================================
    // USAR — FLUXO FELIZ
    // =========================================================

    @Test
    @DisplayName("Jogador usa dado disponível — cria ProspeccaoUso PENDENTE e decrementa quantidade")
    void deveUsarDadoDisponivelCriarPendenteEDecrementar() {
        // Arrange: conceder dado ao jogador
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(),
                new ConcederProspeccaoRequest(dadoD6.getId(), 3));

        FichaProspeccao antes = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        int quantidadeAntes = antes.getQuantidade();

        // Act
        autenticarComo(jogador);
        ProspeccaoUsoResponse uso = prospeccaoService.usar(fichaJogador.getId(),
                new UsarProspeccaoRequest(dadoD6.getId()));

        // Assert
        assertThat(uso.usoId()).isNotNull();
        assertThat(uso.status()).isEqualTo(ProspeccaoUsoStatus.PENDENTE);
        assertThat(uso.fichaId()).isEqualTo(fichaJogador.getId());
        assertThat(uso.dadoProspeccaoConfigId()).isEqualTo(dadoD6.getId());

        FichaProspeccao depois = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        assertThat(depois.getQuantidade()).isEqualTo(quantidadeAntes - 1);
    }

    @Test
    @DisplayName("Jogador usa dado com quantidade=0 — retorna ValidationException")
    void deveLancarValidationExceptionQuandoQuantidadeZero() {
        // Arrange: garantir que a quantidade esteja em 0
        autenticarComo(mestre);
        FichaProspeccao fp = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseGet(() -> {
                    FichaProspeccao novo = FichaProspeccao.builder()
                            .ficha(fichaJogador)
                            .dadoProspeccaoConfig(dadoD6)
                            .quantidade(0)
                            .build();
                    return fichaProspeccaoRepository.save(novo);
                });
        fp.setQuantidade(0);
        fichaProspeccaoRepository.save(fp);

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ValidationException.class, () ->
                prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId())));
    }

    @Test
    @DisplayName("Jogador não pode usar dado de prospecção da ficha de outro jogador")
    void deveNegarUsoDeProspeccaoDeOutraFicha() {
        // Arrange: conceder dado à ficha do outroJogador
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaOutroJogador.getId(),
                new ConcederProspeccaoRequest(dadoD6.getId(), 3));

        // Act & Assert: jogador tentando usar dado da ficha de outroJogador
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () ->
                prospeccaoService.usar(fichaOutroJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId())));
    }

    // =========================================================
    // REVERTER
    // =========================================================

    @Test
    @DisplayName("Mestre reverte uso PENDENTE — status REVERTIDO e quantidade restaurada")
    void deveReverterUsoPendente() {
        // Arrange
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));

        autenticarComo(jogador);
        ProspeccaoUsoResponse uso = prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        FichaProspeccao apesUso = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        int quantidadeAposUso = apesUso.getQuantidade();

        // Act
        autenticarComo(mestre);
        ProspeccaoUsoResponse revertido = prospeccaoService.reverter(fichaJogador.getId(), uso.usoId());

        // Assert
        assertThat(revertido.status()).isEqualTo(ProspeccaoUsoStatus.REVERTIDO);

        FichaProspeccao aposReverter = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        assertThat(aposReverter.getQuantidade()).isEqualTo(quantidadeAposUso + 1);
    }

    @Test
    @DisplayName("Tentativa de reverter uso CONFIRMADO retorna ValidationException")
    void deveLancarExcecaoAoReverterUsoConfirmado() {
        // Arrange
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));

        autenticarComo(jogador);
        ProspeccaoUsoResponse uso = prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        autenticarComo(mestre);
        prospeccaoService.confirmar(fichaJogador.getId(), uso.usoId());

        // Act & Assert
        assertThrows(ValidationException.class, () ->
                prospeccaoService.reverter(fichaJogador.getId(), uso.usoId()));
    }

    @Test
    @DisplayName("Jogador não pode reverter próprio uso")
    void deveNegarReversaoDeUsoAoJogador() {
        // Arrange
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));

        autenticarComo(jogador);
        ProspeccaoUsoResponse uso = prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                prospeccaoService.reverter(fichaJogador.getId(), uso.usoId()));
    }

    // =========================================================
    // CONFIRMAR
    // =========================================================

    @Test
    @DisplayName("Mestre confirma uso PENDENTE — status CONFIRMADO, quantidade inalterada")
    void deveConfirmarUsoPendente() {
        // Arrange
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));

        autenticarComo(jogador);
        ProspeccaoUsoResponse uso = prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        FichaProspeccao aposUso = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        int quantidadeAposUso = aposUso.getQuantidade();

        // Act
        autenticarComo(mestre);
        ProspeccaoUsoResponse confirmado = prospeccaoService.confirmar(fichaJogador.getId(), uso.usoId());

        // Assert
        assertThat(confirmado.status()).isEqualTo(ProspeccaoUsoStatus.CONFIRMADO);

        FichaProspeccao aposConfirmar = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        assertThat(aposConfirmar.getQuantidade()).isEqualTo(quantidadeAposUso); // quantidade inalterada
    }

    @Test
    @DisplayName("Tentativa de confirmar uso REVERTIDO retorna ValidationException")
    void deveLancarExcecaoAoConfirmarUsoRevertido() {
        // Arrange
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));

        autenticarComo(jogador);
        ProspeccaoUsoResponse uso = prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        autenticarComo(mestre);
        prospeccaoService.reverter(fichaJogador.getId(), uso.usoId());

        // Act & Assert
        assertThrows(ValidationException.class, () ->
                prospeccaoService.confirmar(fichaJogador.getId(), uso.usoId()));
    }

    // =========================================================
    // CONCEDER
    // =========================================================

    @Test
    @DisplayName("Mestre concede dado de prospecção — incrementa quantidade")
    void deveConcederDadoEIncrementarQuantidade() {
        // Arrange: garantir que existe registro com quantidade conhecida
        autenticarComo(mestre);
        int quantidadeInicial = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .map(FichaProspeccao::getQuantidade)
                .orElse(0);

        // Act
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 5));

        // Assert
        FichaProspeccao depois = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoD6.getId())
                .orElseThrow();
        assertThat(depois.getQuantidade()).isEqualTo(quantidadeInicial + 5);
    }

    // =========================================================
    // LISTAR USOS
    // =========================================================

    @Test
    @DisplayName("GET /fichas/{id}/prospeccao/usos por Jogador retorna apenas usos da própria ficha")
    void deveRetornarApenasUsosDaPropriasichaParaJogador() {
        // Arrange: criar usos para as duas fichas
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));
        prospeccaoService.conceder(fichaOutroJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 2));

        autenticarComo(jogador);
        prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        autenticarComo(outroJogador);
        prospeccaoService.usar(fichaOutroJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        // Act: jogador lista usos da própria ficha
        autenticarComo(jogador);
        List<ProspeccaoUsoResponse> usos = prospeccaoService.listarUsos(fichaJogador.getId());

        // Assert: apenas usos da ficha do jogador
        assertThat(usos).hasSize(1);
        assertThat(usos.get(0).fichaId()).isEqualTo(fichaJogador.getId());
    }

    @Test
    @DisplayName("GET /jogos/{jogoId}/prospeccao/pendentes retorna todos os PENDENTES do jogo")
    void deveListarTodosPendentesDoJogo() {
        // Arrange: criar usos em duas fichas
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 3));
        prospeccaoService.conceder(fichaOutroJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 3));

        autenticarComo(jogador);
        prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));
        prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        autenticarComo(outroJogador);
        prospeccaoService.usar(fichaOutroJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        // Act
        autenticarComo(mestre);
        List<ProspeccaoUsoResponse> pendentes = prospeccaoService.listarPendentesJogo(jogo.getId());

        // Assert: 3 usos pendentes (2 do jogador, 1 do outroJogador)
        assertThat(pendentes).hasSize(3);
        assertThat(pendentes).allMatch(u -> u.status() == ProspeccaoUsoStatus.PENDENTE);
    }

    @Test
    @DisplayName("GET /jogos/{jogoId}/prospeccao/pendentes não retorna usos confirmados")
    void deveExcluirUsosConfirmadosDosLista() {
        // Arrange
        autenticarComo(mestre);
        prospeccaoService.conceder(fichaJogador.getId(), new ConcederProspeccaoRequest(dadoD6.getId(), 3));

        autenticarComo(jogador);
        ProspeccaoUsoResponse uso1 = prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));
        prospeccaoService.usar(fichaJogador.getId(), new UsarProspeccaoRequest(dadoD6.getId()));

        autenticarComo(mestre);
        prospeccaoService.confirmar(fichaJogador.getId(), uso1.usoId()); // confirma apenas o primeiro

        // Act
        List<ProspeccaoUsoResponse> pendentes = prospeccaoService.listarPendentesJogo(jogo.getId());

        // Assert: apenas 1 pendente (o segundo uso)
        assertThat(pendentes).hasSize(1);
        assertThat(pendentes.get(0).status()).isEqualTo(ProspeccaoUsoStatus.PENDENTE);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
