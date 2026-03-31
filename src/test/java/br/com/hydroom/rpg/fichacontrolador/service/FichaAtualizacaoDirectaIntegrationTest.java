package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAptidaoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.AtualizarAtributoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.UpdateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
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
 * Testes de integração para FichaService.atualizarAtributos() e atualizarAptidoes().
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Atualização de base de atributo e recálculo do total</li>
 *   <li>Rejeição de atributo acima do limitador de nível</li>
 *   <li>Atualização de base de aptidão e recálculo do total</li>
 *   <li>MESTRE pode editar atributos de ficha de outro jogador</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("FichaService - Atualização Direta de Atributos e Aptidões")
class FichaAtualizacaoDirectaIntegrationTest {

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
    private ConfiguracaoNivelRepository nivelConfigRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;
    private Ficha ficha;

    @BeforeEach
    void setUp() {
        // Limpar na ordem correta (dependências primeiro)
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
        nivelConfigRepository.deleteAll();
        jogoParticipanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Atualizacao")
                .email("mestre.atu" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-atu-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador Atualizacao")
                .email("jogador.atu" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-atu-" + n)
                .role("JOGADOR")
                .build());

        // Criar jogo como Mestre (inicializa configs automaticamente, incluindo NivelConfig)
        autenticarComo(mestre);
        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Campanha Atualizacao " + n)
                .build());

        // Adicionar jogador como participante aprovado
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        // Criar ficha com o jogador como dono
        ficha = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Herói de Teste",
                        jogador.getId(), null, null, null, null, null, false));

        // Avançar para nível 1 (xp=1000) para que o NivelConfig de nivel=1 seja usado nas validações
        fichaService.atualizar(ficha.getId(), new UpdateFichaRequest(
                null, null, null, null, null, null, 1000L, null));
    }

    // =========================================================
    // TESTES DE ATUALIZAÇÃO DE ATRIBUTOS
    // =========================================================

    @Test
    @DisplayName("Deve atualizar base do atributo com sucesso e total deve ser recalculado")
    void deveAtualizarAtributoComSucesso() {
        // Arrange - buscar o primeiro atributo da ficha
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(ficha.getId());
        assertThat(atributos).isNotEmpty();
        FichaAtributo atributo = atributos.get(0);

        // Configurar limitador alto para não bloquear
        NivelConfig nivelConfig1 = nivelConfigRepository.findByJogoIdAndNivel(jogo.getId(), 1).orElseThrow();
        nivelConfig1.setLimitadorAtributo(100);
        nivelConfigRepository.save(nivelConfig1);

        int novaBase = 5;

        // Act
        autenticarComo(jogador);
        List<FichaAtributo> resultado = fichaService.atualizarAtributos(ficha.getId(),
                List.of(new AtualizarAtributoRequest(atributo.getAtributoConfig().getId(), novaBase, null, null)));

        // Assert - base atualizada
        FichaAtributo atributoAtualizado = resultado.stream()
                .filter(a -> a.getId().equals(atributo.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(atributoAtualizado.getBase()).isEqualTo(novaBase);

        // Assert - total recalculado (base + nivel + outros = 5 + 0 + 0 = 5)
        assertThat(atributoAtualizado.getTotal()).isEqualTo(novaBase);
    }

    @Test
    @DisplayName("Deve rejeitar atributo com base acima do limitador do nível com ValidationException")
    void deveRejeitarAtributoAcimaDoLimitador() {
        // Arrange - configurar limitador baixo no nível 1
        NivelConfig nivelConfig1 = nivelConfigRepository.findByJogoIdAndNivel(jogo.getId(), 1).orElseThrow();
        nivelConfig1.setLimitadorAtributo(3); // máximo 3 por atributo
        nivelConfigRepository.save(nivelConfig1);

        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(ficha.getId());
        assertThat(atributos).isNotEmpty();
        FichaAtributo atributo = atributos.get(0);

        int baseAcimaDoLimitador = 5; // acima do limitador de 3

        // Act & Assert
        autenticarComo(jogador);
        assertThrows(ValidationException.class,
                () -> fichaService.atualizarAtributos(ficha.getId(),
                        List.of(new AtualizarAtributoRequest(
                                atributo.getAtributoConfig().getId(), baseAcimaDoLimitador, null, null))));
    }

    @Test
    @DisplayName("Mestre pode atualizar atributos de ficha de outro jogador")
    void deveMestrePoderAtualizarAtributoDeFichaDeOutro() {
        // Arrange - ficha pertence ao jogador, mestre vai editar
        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(ficha.getId());
        assertThat(atributos).isNotEmpty();
        FichaAtributo atributo = atributos.get(0);

        // Garantir limitador alto
        NivelConfig nivelConfig1 = nivelConfigRepository.findByJogoIdAndNivel(jogo.getId(), 1).orElseThrow();
        nivelConfig1.setLimitadorAtributo(100);
        nivelConfigRepository.save(nivelConfig1);

        // Act - Mestre atualiza atributo da ficha do jogador
        autenticarComo(mestre);
        List<FichaAtributo> resultado = fichaService.atualizarAtributos(ficha.getId(),
                List.of(new AtualizarAtributoRequest(atributo.getAtributoConfig().getId(), 7, null, null)));

        // Assert
        FichaAtributo atributoAtualizado = resultado.stream()
                .filter(a -> a.getId().equals(atributo.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(atributoAtualizado.getBase()).isEqualTo(7);
    }

    @Test
    @DisplayName("Jogador não pode atualizar atributos de ficha de outro jogador")
    void deveImpedirJogadorDeAtualizarAtributoDeFichaDeOutro() {
        // Arrange - criar outro jogador e sua ficha
        int n = counter.get();
        Usuario outroJogador = usuarioRepository.save(Usuario.builder()
                .nome("Outro Jogador")
                .email("outro.atu" + n + "@test.com")
                .provider("google")
                .providerId("google-outro-atu-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(outroJogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        Ficha fichaDeOutro = fichaService.criar(
                new CreateFichaRequest(jogo.getId(), "Ficha do Outro Jogador",
                        outroJogador.getId(), null, null, null, null, null, false));

        List<FichaAtributo> atributos = fichaAtributoRepository.findByFichaId(fichaDeOutro.getId());
        assertThat(atributos).isNotEmpty();
        FichaAtributo atributo = atributos.get(0);

        // Act - jogador tenta atualizar atributo da ficha de outroJogador
        autenticarComo(jogador);

        // Assert
        assertThrows(ForbiddenException.class,
                () -> fichaService.atualizarAtributos(fichaDeOutro.getId(),
                        List.of(new AtualizarAtributoRequest(atributo.getAtributoConfig().getId(), 3, null, null))));
    }

    // =========================================================
    // TESTES DE ATUALIZAÇÃO DE APTIDÕES
    // =========================================================

    @Test
    @DisplayName("Deve atualizar base de aptidão com sucesso")
    void deveAtualizarAptidaoComSucesso() {
        // Arrange - buscar a primeira aptidão da ficha
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaId(ficha.getId());
        assertThat(aptidoes).isNotEmpty();
        FichaAptidao aptidao = aptidoes.get(0);

        int novaBase = 4;

        // Act
        autenticarComo(jogador);
        List<FichaAptidao> resultado = fichaService.atualizarAptidoes(ficha.getId(),
                List.of(new AtualizarAptidaoRequest(aptidao.getAptidaoConfig().getId(), novaBase, null, null)));

        // Assert - base atualizada na entidade retornada
        FichaAptidao aptidaoAtualizada = resultado.stream()
                .filter(a -> a.getId().equals(aptidao.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(aptidaoAtualizada.getBase()).isEqualTo(novaBase);

        // Assert - persistido no banco
        FichaAptidao persistida = fichaAptidaoRepository.findById(aptidao.getId()).orElseThrow();
        assertThat(persistida.getBase()).isEqualTo(novaBase);
    }

    @Test
    @DisplayName("Deve atualizar múltiplos campos da aptidão (base, sorte, classe)")
    void deveAtualizarMultiplosCamposDaAptidao() {
        // Arrange
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaId(ficha.getId());
        assertThat(aptidoes).isNotEmpty();
        FichaAptidao aptidao = aptidoes.get(0);

        // Act
        autenticarComo(jogador);
        List<FichaAptidao> resultado = fichaService.atualizarAptidoes(ficha.getId(),
                List.of(new AtualizarAptidaoRequest(aptidao.getAptidaoConfig().getId(), 3, 2, 1)));

        // Assert - campos individuais atualizados
        FichaAptidao aptidaoAtualizada = resultado.stream()
                .filter(a -> a.getId().equals(aptidao.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(aptidaoAtualizada.getBase()).isEqualTo(3);
        assertThat(aptidaoAtualizada.getSorte()).isEqualTo(2);
        assertThat(aptidaoAtualizada.getClasse()).isEqualTo(1);

        // Assert - persistido no banco
        FichaAptidao persistida = fichaAptidaoRepository.findById(aptidao.getId()).orElseThrow();
        assertThat(persistida.getBase()).isEqualTo(3);
        assertThat(persistida.getSorte()).isEqualTo(2);
        assertThat(persistida.getClasse()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mestre pode atualizar aptidões de ficha de outro jogador")
    void deveMestrePoderAtualizarAptidaoDeFichaDeOutro() {
        // Arrange - ficha pertence ao jogador, mestre vai editar
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaId(ficha.getId());
        assertThat(aptidoes).isNotEmpty();
        FichaAptidao aptidao = aptidoes.get(0);

        // Act - Mestre atualiza aptidão da ficha do jogador
        autenticarComo(mestre);
        List<FichaAptidao> resultado = fichaService.atualizarAptidoes(ficha.getId(),
                List.of(new AtualizarAptidaoRequest(aptidao.getAptidaoConfig().getId(), 6, null, null)));

        // Assert
        FichaAptidao aptidaoAtualizada = resultado.stream()
                .filter(a -> a.getId().equals(aptidao.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(aptidaoAtualizada.getBase()).isEqualTo(6);
    }

    @Test
    @DisplayName("Campos null na atualização de aptidão não devem alterar valores existentes")
    void naoDeveAlterarCamposNullNaAtualizacaoDeAptidao() {
        // Arrange - definir valores iniciais
        List<FichaAptidao> aptidoes = fichaAptidaoRepository.findByFichaId(ficha.getId());
        assertThat(aptidoes).isNotEmpty();
        FichaAptidao aptidao = aptidoes.get(0);

        autenticarComo(jogador);
        // Primeiro update: definir base=3
        fichaService.atualizarAptidoes(ficha.getId(),
                List.of(new AtualizarAptidaoRequest(aptidao.getAptidaoConfig().getId(), 3, null, null)));

        // Act - segundo update: apenas sorte, base deve ser mantida
        List<FichaAptidao> resultado = fichaService.atualizarAptidoes(ficha.getId(),
                List.of(new AtualizarAptidaoRequest(aptidao.getAptidaoConfig().getId(), null, 2, null)));

        // Assert - base mantida, sorte atualizada
        FichaAptidao aptidaoAtualizada = resultado.stream()
                .filter(a -> a.getId().equals(aptidao.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(aptidaoAtualizada.getBase()).isEqualTo(3);
        assertThat(aptidaoAtualizada.getSorte()).isEqualTo(2);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
