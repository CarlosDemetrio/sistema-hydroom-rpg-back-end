package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para ParticipanteSecurityService.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("ParticipanteSecurityService - Testes de Integração")
class ParticipanteSecurityServiceTest {

    @Autowired
    private ParticipanteSecurityService securityService;

    @Autowired
    private JogoParticipanteRepository participanteRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Jogo jogo;
    private Usuario mestre;
    private Usuario jogadorAprovado;
    private Usuario jogadorPendente;
    private Usuario semParticipacao;

    @BeforeEach
    void setUp() {
        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
            .nome("Mestre " + n).email("mestre.sec" + n + "@test.com")
            .provider("google").providerId("sec-mestre-" + n).build());

        jogadorAprovado = usuarioRepository.save(Usuario.builder()
            .nome("Aprovado " + n).email("aprovado.sec" + n + "@test.com")
            .provider("google").providerId("sec-aprovado-" + n).build());

        jogadorPendente = usuarioRepository.save(Usuario.builder()
            .nome("Pendente " + n).email("pendente.sec" + n + "@test.com")
            .provider("google").providerId("sec-pendente-" + n).build());

        semParticipacao = usuarioRepository.save(Usuario.builder()
            .nome("Sem " + n).email("sem.sec" + n + "@test.com")
            .provider("google").providerId("sec-sem-" + n).build());

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Jogo Sec " + n).dataInicio(LocalDate.now()).build());

        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(mestre).role(RoleJogo.MESTRE).status(StatusParticipante.APROVADO).build());

        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogadorAprovado).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogadorPendente).role(RoleJogo.JOGADOR).status(StatusParticipante.PENDENTE).build());
    }

    @Test
    @DisplayName("deve permitir acesso ao Mestre do jogo")
    void devePermitirAcessoParaMestre() {
        assertThat(securityService.canAccessJogo(jogo.getId(), mestre.getId())).isTrue();
        assertThat(securityService.isMestreDoJogo(jogo.getId(), mestre.getId())).isTrue();
    }

    @Test
    @DisplayName("deve permitir acesso ao participante APROVADO")
    void devePermitirAcessoParaParticipanteAprovado() {
        assertThat(securityService.canAccessJogo(jogo.getId(), jogadorAprovado.getId())).isTrue();
        assertThat(securityService.isParticipanteAprovado(jogo.getId(), jogadorAprovado.getId())).isTrue();
    }

    @Test
    @DisplayName("deve negar acesso ao participante PENDENTE")
    void deveNegarAcessoParaParticipantePendente() {
        assertThat(securityService.canAccessJogo(jogo.getId(), jogadorPendente.getId())).isFalse();
        assertThat(securityService.isParticipanteAprovado(jogo.getId(), jogadorPendente.getId())).isFalse();
    }

    @Test
    @DisplayName("deve negar acesso a usuário sem participação")
    void deveNegarAcessoParaUsuarioSemParticipacao() {
        assertThat(securityService.canAccessJogo(jogo.getId(), semParticipacao.getId())).isFalse();
    }

    @Test
    @DisplayName("assertCanAccessJogo não lança exceção para Mestre")
    void assertCanAccessJogoNaoLancaParaMestre() {
        assertDoesNotThrow(() -> securityService.assertCanAccessJogo(jogo.getId(), mestre.getId()));
    }

    @Test
    @DisplayName("assertCanAccessJogo lança ForbiddenException para usuário sem acesso")
    void assertCanAccessJogoLancaParaSemAcesso() {
        assertThrows(ForbiddenException.class, () ->
            securityService.assertCanAccessJogo(jogo.getId(), semParticipacao.getId()));
    }

    @Test
    @DisplayName("assertMestreDoJogo lança ForbiddenException para não-Mestre")
    void assertMestreDoJogoLancaParaNaoMestre() {
        assertThrows(ForbiddenException.class, () ->
            securityService.assertMestreDoJogo(jogo.getId(), jogadorAprovado.getId()));
    }
}
