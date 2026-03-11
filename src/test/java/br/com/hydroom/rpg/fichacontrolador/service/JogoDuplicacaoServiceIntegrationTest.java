package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
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
@DisplayName("JogoDuplicacaoService — Testes de Integração")
class JogoDuplicacaoServiceIntegrationTest {

    @Autowired private JogoDuplicacaoService jogoDuplicacaoService;
    @Autowired private JogoRepository jogoRepository;
    @Autowired private JogoParticipanteRepository jogoParticipanteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ConfiguracaoAtributoRepository atributoRepository;
    @Autowired private BonusConfigRepository bonusRepository;

    private static final AtomicInteger counter = new AtomicInteger();

    private Usuario mestre;
    private Jogo jogoOrigem;

    @BeforeEach
    void setUp() {
        int id = counter.incrementAndGet();
        mestre = usuarioRepository.save(Usuario.builder()
                .nome("Mestre Dup " + id)
                .email("mestre-dup-" + id + "@test.com")
                .provider("google")
                .providerId("google-dup-" + id)
                .build());

        setAuthentication(mestre.getEmail(), "ROLE_MESTRE");

        jogoOrigem = jogoRepository.save(Jogo.builder().nome("Jogo Original").jogoAtivo(true).build());
        jogoParticipanteRepository.save(JogoParticipante.builder()
                .jogo(jogoOrigem).usuario(mestre).role(RoleJogo.MESTRE)
                .status(StatusParticipante.APROVADO).build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("deve duplicar jogo criando novo com mesmo nome do mestre como participante")
    void deveDuplicarJogoComNovoNome() {
        Jogo novo = jogoDuplicacaoService.duplicar(jogoOrigem.getId(), "Jogo Cópia");

        assertThat(novo).isNotNull();
        assertThat(novo.getId()).isNotEqualTo(jogoOrigem.getId());
        assertThat(novo.getNome()).isEqualTo("Jogo Cópia");
    }

    @Test
    @DisplayName("deve criar participação do mestre no novo jogo")
    void deveCriarParticipacaoMestreNoNovoJogo() {
        Jogo novo = jogoDuplicacaoService.duplicar(jogoOrigem.getId(), "Cópia");

        boolean mestreParticipa = jogoParticipanteRepository
                .existsByJogoIdAndUsuarioIdAndRole(novo.getId(), mestre.getId(), RoleJogo.MESTRE);
        assertThat(mestreParticipa).isTrue();
    }

    @Test
    @DisplayName("deve copiar atributos do jogo original para o novo jogo")
    void deveCopiarAtributosParaNovoJogo() {
        atributoRepository.save(AtributoConfig.builder()
                .jogo(jogoOrigem).nome("Força").abreviacao("FOR").ordemExibicao(1).build());

        Jogo novo = jogoDuplicacaoService.duplicar(jogoOrigem.getId(), "Cópia Com Configs");

        List<AtributoConfig> atributos = atributoRepository.findByJogoIdOrderByOrdemExibicao(novo.getId());
        assertThat(atributos).hasSize(1);
        assertThat(atributos.get(0).getNome()).isEqualTo("Força");
    }

    @Test
    @DisplayName("deve lançar ForbiddenException se usuário não for Mestre do jogo")
    void deveLancarErroSeNaoForMestre() {
        int id = counter.incrementAndGet();
        Usuario jogador = usuarioRepository.save(Usuario.builder()
                .nome("Jogador " + id).email("jogador-" + id + "@test.com")
                .provider("google").providerId("g-j-" + id).build());

        setAuthentication(jogador.getEmail(), "ROLE_JOGADOR");

        assertThatThrownBy(() -> jogoDuplicacaoService.duplicar(jogoOrigem.getId(), "Tentativa"))
                .isInstanceOf(ForbiddenException.class);
    }

    private void setAuthentication(String email, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null,
                        List.of(new SimpleGrantedAuthority(role))));
    }
}
