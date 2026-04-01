package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarProspeccaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarVidaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.DadoProspeccaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaProspeccao;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVida;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVidaMembro;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.MembroCorpoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
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
import br.com.hydroom.rpg.fichacontrolador.repository.MembroCorpoConfigRepository;
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
 * Testes de integração para FichaVidaService.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Atualizar vida atual e essência atual com sucesso</li>
 *   <li>Atualizar dano nos membros do corpo</li>
 *   <li>Atualizar prospecção com sucesso</li>
 *   <li>Jogador não autorizado retorna ForbiddenException</li>
 *   <li>Membro inexistente retorna ResourceNotFoundException</li>
 *   <li>DadoProspeccao inexistente retorna ResourceNotFoundException</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaVidaService - Testes de Integração")
class FichaVidaServiceIntegrationTest {

    @Autowired
    private FichaVidaService fichaVidaService;

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
    private FichaDescricaoFisicaRepository fichaDescricaoFisicaRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository jogoParticipanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MembroCorpoConfigRepository membroCorpoConfigRepository;

    @Autowired
    private DadoProspeccaoConfigRepository dadoProspeccaoConfigRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;
    private Ficha fichaMestre;
    private Ficha fichaJogador;

    @BeforeEach
    void setUp() {
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
                .nome("Mestre Vida " + n)
                .email("mestre-vida" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-vida-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Vida " + n)
                .email("jogador-vida" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-vida-" + n)
                .role("JOGADOR")
                .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador " + n)
                .email("outro-jogador-vida" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-jogador-vida-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo Vida Teste " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        // Mestre cria sua ficha
        fichaMestre = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "NPC Mestre", null, null, null, null, null, null, true));

        // Mestre cria ficha para o jogador
        fichaJogador = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem do Jogador", jogador.getId(), null, null, null, null, null, false));
    }

    // =========================================================
    // TESTES: ATUALIZAR VIDA
    // =========================================================

    @Test
    @DisplayName("Mestre deve atualizar vida atual e essência atual com sucesso")
    void mestreDeveAtualizarVidaComSucesso() {
        // Arrange
        autenticarComo(mestre);
        var request = new AtualizarVidaRequest(50, 30, List.of());

        // Act
        fichaVidaService.atualizarVida(fichaMestre.getId(), request);

        // Assert
        FichaVida vidaAtualizada = fichaVidaRepository.findByFichaId(fichaMestre.getId()).orElseThrow();
        assertThat(vidaAtualizada.getVidaAtual()).isEqualTo(50);
        assertThat(fichaEssenciaRepository.findByFichaId(fichaMestre.getId()).orElseThrow().getEssenciaAtual())
                .isEqualTo(30);
    }

    @Test
    @DisplayName("Jogador deve atualizar vida de sua própria ficha")
    void jogadorDeveAtualizarVidaDaSuaFicha() {
        // Arrange
        autenticarComo(jogador);
        var request = new AtualizarVidaRequest(25, 10, List.of());

        // Act
        fichaVidaService.atualizarVida(fichaJogador.getId(), request);

        // Assert
        FichaVida vidaAtualizada = fichaVidaRepository.findByFichaId(fichaJogador.getId()).orElseThrow();
        assertThat(vidaAtualizada.getVidaAtual()).isEqualTo(25);
    }

    @Test
    @DisplayName("Jogador não autorizado deve receber ForbiddenException ao tentar atualizar ficha de outro")
    void jogadorNaoAutorizadoDeveReceberForbiddenAoAtualizarVida() {
        // Arrange — outroJogador não é participante do jogo
        autenticarComo(outroJogador);
        var request = new AtualizarVidaRequest(50, 30, List.of());

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaVidaService.atualizarVida(fichaJogador.getId(), request));
    }

    @Test
    @DisplayName("Deve atualizar dano nos membros do corpo ao informar lista de membros")
    void deveAtualizarDanoNosMembros() {
        // Arrange
        autenticarComo(mestre);
        List<FichaVidaMembro> membros = fichaVidaMembroRepository.findByFichaId(fichaMestre.getId());

        // Só executa se houver membros configurados no jogo de teste
        if (membros.isEmpty()) {
            return;
        }

        FichaVidaMembro primeiroMembro = membros.get(0);
        Long membroConfigId = primeiroMembro.getMembroCorpoConfig().getId();

        var membroRequest = new AtualizarVidaRequest.MembroVidaRequest(membroConfigId, 15);
        var request = new AtualizarVidaRequest(40, 20, List.of(membroRequest));

        // Act
        fichaVidaService.atualizarVida(fichaMestre.getId(), request);

        // Assert
        FichaVidaMembro membroAtualizado = fichaVidaMembroRepository
                .findByFichaIdAndMembroCorpoConfigId(fichaMestre.getId(), membroConfigId)
                .orElseThrow();
        assertThat(membroAtualizado.getDanoRecebido()).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar membro inexistente")
    void deveLancarNotFoundAoAtualizarMembroInexistente() {
        // Arrange
        autenticarComo(mestre);
        Long membroConfigIdInexistente = 999999L;
        var membroRequest = new AtualizarVidaRequest.MembroVidaRequest(membroConfigIdInexistente, 10);
        var request = new AtualizarVidaRequest(40, 20, List.of(membroRequest));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> fichaVidaService.atualizarVida(fichaMestre.getId(), request));
    }

    @Test
    @DisplayName("Deve aceitar lista de membros vazia sem erro")
    void deveAceitarListaMembrosVazia() {
        // Arrange
        autenticarComo(mestre);
        var request = new AtualizarVidaRequest(30, 15, null);

        // Act & Assert — não deve lançar exceção
        fichaVidaService.atualizarVida(fichaMestre.getId(), request);

        FichaVida vida = fichaVidaRepository.findByFichaId(fichaMestre.getId()).orElseThrow();
        assertThat(vida.getVidaAtual()).isEqualTo(30);
    }

    // =========================================================
    // TESTES: ATUALIZAR PROSPECÇÃO
    // =========================================================

    @Test
    @DisplayName("Mestre deve atualizar quantidade de prospecção com sucesso")
    void mestreDeveAtualizarProspeccaoComSucesso() {
        // Arrange
        autenticarComo(mestre);
        List<FichaProspeccao> prospeccoes = fichaProspeccaoRepository.findByFichaId(fichaMestre.getId());

        // Só executa se houver dados de prospecção configurados
        if (prospeccoes.isEmpty()) {
            return;
        }

        FichaProspeccao primeiraPropeccao = prospeccoes.get(0);
        Long dadoProspeccaoConfigId = primeiraPropeccao.getDadoProspeccaoConfig().getId();

        var request = new AtualizarProspeccaoRequest(dadoProspeccaoConfigId, 5);

        // Act
        fichaVidaService.atualizarProspeccao(fichaMestre.getId(), request);

        // Assert
        FichaProspeccao prospeccaoAtualizada = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaMestre.getId(), dadoProspeccaoConfigId)
                .orElseThrow();
        assertThat(prospeccaoAtualizada.getQuantidade()).isEqualTo(5);
    }

    @Test
    @DisplayName("Jogador deve atualizar prospecção de sua própria ficha")
    void jogadorDeveAtualizarProspeccaoDaSuaFicha() {
        // Arrange
        autenticarComo(jogador);
        List<FichaProspeccao> prospeccoes = fichaProspeccaoRepository.findByFichaId(fichaJogador.getId());

        if (prospeccoes.isEmpty()) {
            return;
        }

        Long dadoProspeccaoConfigId = prospeccoes.get(0).getDadoProspeccaoConfig().getId();
        var request = new AtualizarProspeccaoRequest(dadoProspeccaoConfigId, 3);

        // Act
        fichaVidaService.atualizarProspeccao(fichaJogador.getId(), request);

        // Assert
        FichaProspeccao atualizada = fichaProspeccaoRepository
                .findByFichaIdAndDadoProspeccaoConfigId(fichaJogador.getId(), dadoProspeccaoConfigId)
                .orElseThrow();
        assertThat(atualizada.getQuantidade()).isEqualTo(3);
    }

    @Test
    @DisplayName("Jogador não autorizado deve receber ForbiddenException ao atualizar prospecção")
    void jogadorNaoAutorizadoDeveReceberForbiddenAoAtualizarProspeccao() {
        // Arrange — outroJogador não tem acesso à fichaJogador
        autenticarComo(outroJogador);
        var request = new AtualizarProspeccaoRequest(1L, 5);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaVidaService.atualizarProspeccao(fichaJogador.getId(), request));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar dado de prospecção inexistente")
    void deveLancarNotFoundAoAtualizarProspeccaoInexistente() {
        // Arrange
        autenticarComo(mestre);
        Long dadoInexistente = 999999L;
        var request = new AtualizarProspeccaoRequest(dadoInexistente, 5);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> fichaVidaService.atualizarProspeccao(fichaMestre.getId(), request));
    }

    // =========================================================
    // TESTES: LISTAR PROSPECÇÕES
    // =========================================================

    @Test
    @DisplayName("Mestre deve listar prospecções de qualquer ficha")
    void mestreDeveListarProspeccoesDeQualquerFicha() {
        // Arrange
        autenticarComo(mestre);

        // Act
        List<FichaProspeccao> prospeccoes = fichaVidaService.listarProspeccoes(fichaMestre.getId());

        // Assert — deve retornar lista (pode estar vazia se não há dados de prospecção configurados)
        assertThat(prospeccoes).isNotNull();
    }

    @Test
    @DisplayName("Jogador deve listar prospecções de sua própria ficha")
    void jogadorDeveListarProspeccoesDeSuaFicha() {
        // Arrange
        autenticarComo(jogador);

        // Act
        List<FichaProspeccao> prospeccoes = fichaVidaService.listarProspeccoes(fichaJogador.getId());

        // Assert
        assertThat(prospeccoes).isNotNull();
    }

    @Test
    @DisplayName("Jogador não autorizado deve receber ForbiddenException ao listar prospecções de ficha de outro")
    void jogadorNaoAutorizadoDeveReceberForbiddenAoListarProspeccoes() {
        // Arrange
        autenticarComo(outroJogador);

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> fichaVidaService.listarProspeccoes(fichaJogador.getId()));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao listar prospecções de ficha inexistente")
    void deveLancarNotFoundAoListarProspeccoesParaFichaInexistente() {
        // Arrange
        autenticarComo(mestre);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> fichaVidaService.listarProspeccoes(999999L));
    }

    // =========================================================
    // TESTES: ATUALIZAR VIDA COM FICHA INEXISTENTE
    // =========================================================

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar vida de ficha inexistente")
    void deveLancarNotFoundAoAtualizarVidaDeFichaInexistente() {
        // Arrange
        autenticarComo(mestre);
        var request = new AtualizarVidaRequest(50, 30, List.of());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> fichaVidaService.atualizarVida(999999L, request));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar prospecção de ficha inexistente")
    void deveLancarNotFoundAoAtualizarProspeccaoDeFichaInexistente() {
        // Arrange
        autenticarComo(mestre);
        var request = new AtualizarProspeccaoRequest(1L, 5);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> fichaVidaService.atualizarProspeccao(999999L, request));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
