package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.ConcederXpRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CreateFichaRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.CriarJogoRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
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
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para o endpoint PUT /fichas/{id}/xp (Spec 006 T4).
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Concessão básica de XP com retorno de FichaResumoResponse</li>
 *   <li>XP acumulativo (nunca substitui)</li>
 *   <li>Level up automático ao cruzar threshold</li>
 *   <li>Nível nunca desce mesmo após múltiplas concessões</li>
 *   <li>Motivo opcional — não persistido</li>
 *   <li>Jogador tenta conceder XP → ForbiddenException</li>
 *   <li>XP = 0 → violação de constraint @Min(1)</li>
 *   <li>Motivo com mais de 500 chars → violação de constraint @Size</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("Concessão de XP - Testes de Integração (Spec 006 T4)")
class ConcederXpIntegrationTest {

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
    private Validator validator;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Jogo jogo;
    private Ficha ficha;

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
                .nome("Mestre XP " + n)
                .email("mestre.xp" + n + "@test.com")
                .provider("google")
                .providerId("google-mestre-xp-" + n)
                .role("MESTRE")
                .build());

        jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador XP " + n)
                .email("jogador.xp" + n + "@test.com")
                .provider("google")
                .providerId("google-jogador-xp-" + n)
                .role("JOGADOR")
                .build());

        autenticarComo(mestre);

        jogo = jogoService.criarJogo(CriarJogoRequest.builder()
                .nome("Jogo XP Teste " + n)
                .build());

        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogo)
                .usuario(jogador)
                .role(RoleJogo.JOGADOR)
                .status(StatusParticipante.APROVADO)
                .build());

        autenticarComo(mestre);
        ficha = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Personagem Teste XP " + n, null, null, null, null, null, null, false));
    }

    // =========================================================
    // Concessão básica
    // =========================================================

    @Test
    @DisplayName("Mestre concede XP básico — retorna FichaResumoResponse com XP atualizado")
    void mestreConcedeXpBasico_deveRetornarResumoComXpAtualizado() {
        // Arrange
        assertThat(ficha.getXp()).isIn(0L, null);

        // Act
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(1000L, null));

        // Assert
        assertThat(resumo.xp()).isEqualTo(1000L);
        assertThat(resumo.nivel()).isGreaterThanOrEqualTo(1);

        Ficha fichaAtualizada = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaAtualizada.getXp()).isEqualTo(1000L);
    }

    // =========================================================
    // XP acumulativo
    // =========================================================

    @Test
    @DisplayName("Duas concessões de XP acumulam — 3000 + 2000 = 5000 (não substitui)")
    void duasConcessoes_xpDeveAcumular() {
        // Arrange
        fichaService.concederXp(ficha.getId(), new ConcederXpRequest(3000L, null));

        // Act: segunda concessão deve somar, não substituir
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(2000L, null));

        // Assert: 3000 + 2000 = 5000
        assertThat(resumo.xp()).isEqualTo(5000L);

        Ficha fichaFinal = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaFinal.getXp()).isEqualTo(5000L);
    }

    // =========================================================
    // Level up automático
    // =========================================================

    @Test
    @DisplayName("Level up automático — XP cruza threshold de nivel=2 (3000 XP) e nivel sobe")
    void levelUpAutomatico_nivelDeveSubirAoAtingirThreshold() {
        // Arrange: ficha começa em nivel=1
        assertThat(ficha.getNivel()).isEqualTo(1);

        // Act: nível 2 requer 3000 XP (DefaultGameConfigProvider)
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(3000L, null));

        // Assert
        assertThat(resumo.nivel()).isEqualTo(2);
        assertThat(resumo.xp()).isEqualTo(3000L);

        Ficha fichaAtualizada = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaAtualizada.getNivel()).isEqualTo(2);
    }

    @Test
    @DisplayName("Level up por acumulação — 4000 + 3000 = 7000 XP deve alcançar nivel=3 (threshold=6000)")
    void levelUpPorAcumulacao_nivelDeveSubirComXpSomado() {
        // Arrange: levar ficha para nivel=2 (4000 XP, ainda < 6000 para nivel=3)
        fichaService.concederXp(ficha.getId(), new ConcederXpRequest(4000L, null));
        Ficha fichaIntermediaria = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaIntermediaria.getNivel()).isEqualTo(2);

        // Act: conceder mais 3000 XP → total 7000 → nivel=3 (threshold=6000)
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(3000L, null));

        // Assert
        assertThat(resumo.xp()).isEqualTo(7000L);
        assertThat(resumo.nivel()).isEqualTo(3);

        Ficha fichaFinal = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaFinal.getNivel()).isEqualTo(3);
    }

    // =========================================================
    // Nível nunca desce
    // =========================================================

    @Test
    @DisplayName("Nível nunca desce — mesmo após múltiplas concessões pequenas, nivel permanece no máximo atingido")
    void nivelNuncaDesce_mesmoCom_XpAcumuladoEmNivelAlto() {
        // Arrange: levar ficha para nivel=2 com 3000 XP
        fichaService.concederXp(ficha.getId(), new ConcederXpRequest(3000L, null));
        Ficha fichaAposLevelUp = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaAposLevelUp.getNivel()).isEqualTo(2);

        // Act: conceder mais 500 XP — total = 3500, ainda no intervalo do nivel=2
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(500L, null));

        // Assert: nivel permanece 2, XP acumulou
        assertThat(resumo.nivel()).isEqualTo(2);
        assertThat(resumo.xp()).isEqualTo(3500L);

        Ficha fichaFinal = fichaRepository.findById(ficha.getId()).orElseThrow();
        assertThat(fichaFinal.getNivel()).isEqualTo(2);
        assertThat(fichaFinal.getXp()).isEqualTo(3500L);
    }

    // =========================================================
    // Motivo opcional
    // =========================================================

    @Test
    @DisplayName("Motivo ausente — concessão funciona normalmente sem motivo")
    void semMotivo_concessaoFuncionaNormalmente() {
        // Act: sem campo motivo
        FichaResumoResponse resumo = fichaService.concederXp(ficha.getId(), new ConcederXpRequest(500L, null));

        // Assert
        assertThat(resumo.xp()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Motivo presente — concessão funciona normalmente com motivo válido")
    void comMotivoValido_concessaoFuncionaNormalmente() {
        // Act
        FichaResumoResponse resumo = fichaService.concederXp(
                ficha.getId(), new ConcederXpRequest(500L, "Completou missão da taverna"));

        // Assert
        assertThat(resumo.xp()).isEqualTo(500L);
    }

    // =========================================================
    // Validação de Bean Constraints
    // =========================================================

    @Test
    @DisplayName("XP = 0 viola constraint @Min(1) — deve ter violação de validação")
    void xpZero_deveViolacionarConstraintMin1() {
        // Arrange
        ConcederXpRequest requestInvalido = new ConcederXpRequest(0L, null);

        // Act
        Set<ConstraintViolation<ConcederXpRequest>> violations = validator.validate(requestInvalido);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("xp")))
                .as("Violação deve ser no campo xp")
                .isTrue();
    }

    @Test
    @DisplayName("XP negativo viola constraint @Min(1) — deve ter violação de validação")
    void xpNegativo_deveViolacionarConstraintMin1() {
        // Arrange
        ConcederXpRequest requestInvalido = new ConcederXpRequest(-100L, null);

        // Act
        Set<ConstraintViolation<ConcederXpRequest>> violations = validator.validate(requestInvalido);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("xp")))
                .as("Violação deve ser no campo xp")
                .isTrue();
    }

    @Test
    @DisplayName("Motivo com mais de 500 caracteres viola constraint @Size(max=500)")
    void motivoLongo_deveViolacionarConstraintSize() {
        // Arrange: motivo com 501 caracteres
        String motivoLongo = "X".repeat(501);
        ConcederXpRequest requestInvalido = new ConcederXpRequest(100L, motivoLongo);

        // Act
        Set<ConstraintViolation<ConcederXpRequest>> violations = validator.validate(requestInvalido);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("motivo")))
                .as("Violação deve ser no campo motivo")
                .isTrue();
    }

    @Test
    @DisplayName("Motivo com exatamente 500 caracteres é válido")
    void motivoCom500Chars_deveSerValido() {
        // Arrange
        String motivoNaLimite = "X".repeat(500);
        ConcederXpRequest requestValido = new ConcederXpRequest(100L, motivoNaLimite);

        // Act
        Set<ConstraintViolation<ConcederXpRequest>> violations = validator.validate(requestValido);

        // Assert
        assertThat(violations).isEmpty();
    }

    // =========================================================
    // Segurança: Jogador não pode conceder XP
    // =========================================================

    @Test
    @DisplayName("Jogador tenta conceder XP — deve lançar ForbiddenException")
    void jogadorTentaConcederXp_deveLancarForbiddenException() {
        // Arrange
        autenticarComo(jogador);
        Ficha fichaDoJogador = fichaService.criar(new CreateFichaRequest(
                jogo.getId(), "Ficha do Jogador XP", jogador.getId(), null, null, null, null, null, false));

        // Act + Assert
        autenticarComo(jogador);
        assertThrows(ForbiddenException.class, () ->
                fichaService.concederXp(fichaDoJogador.getId(), new ConcederXpRequest(500L, null)));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void autenticarComo(Usuario usuario) {
        var auth = new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
