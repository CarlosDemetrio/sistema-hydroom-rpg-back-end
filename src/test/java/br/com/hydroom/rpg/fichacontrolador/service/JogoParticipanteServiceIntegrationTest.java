package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.model.enums.StatusParticipante;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de integração para o fluxo de participação em jogos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("JogoParticipanteService - Testes de Integração")
class JogoParticipanteServiceIntegrationTest {

    @Autowired
    private JogoParticipanteService participanteService;

    @Autowired
    private JogoParticipanteRepository participanteRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final AtomicInteger counter = new AtomicInteger(1);

    private Usuario mestre;
    private Usuario jogador;
    private Usuario outroJogador;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        int n = counter.getAndIncrement();

        mestre = usuarioRepository.save(Usuario.builder()
            .nome("Mestre " + n)
            .email("mestre" + n + "@test.com")
            .provider("google")
            .providerId("google-mestre-" + n)
            .build());

        jogador = usuarioRepository.save(Usuario.builder()
            .nome("Jogador " + n)
            .email("jogador" + n + "@test.com")
            .provider("google")
            .providerId("google-jogador-" + n)
            .build());

        outroJogador = usuarioRepository.save(Usuario.builder()
            .nome("Outro " + n)
            .email("outro" + n + "@test.com")
            .provider("google")
            .providerId("google-outro-" + n)
            .build());

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha " + n)
            .dataInicio(LocalDate.now())
            .build());

        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .status(StatusParticipante.APROVADO)
            .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(usuario.getEmail(), "n/a")
        );
    }

    // ===== SOLICITAR =====

    @Test
    @DisplayName("deve permitir solicitação de entrada no jogo")
    void devePermitirSolicitacaoDeEntrada() {
        setAuth(jogador);
        JogoParticipante resultado = participanteService.solicitar(jogo.getId());

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
        assertThat(resultado.getRole()).isEqualTo(RoleJogo.JOGADOR);
        assertThat(resultado.getUsuario().getId()).isEqualTo(jogador.getId());
    }

    @Test
    @DisplayName("deve rejeitar solicitação duplicada")
    void deveRejeitarSolicitacaoDuplicada() {
        setAuth(jogador);
        participanteService.solicitar(jogo.getId());

        assertThrows(ConflictException.class, () -> participanteService.solicitar(jogo.getId()));
    }

    @Test
    @DisplayName("Mestre não pode solicitar entrada no próprio jogo")
    void naoDevePermitirSolicitacaoDoProprioMestre() {
        setAuth(mestre);
        assertThrows(BusinessException.class, () -> participanteService.solicitar(jogo.getId()));
    }

    // ===== APROVAR =====

    @Test
    @DisplayName("Mestre deve aprovar solicitação pendente")
    void deveMestreAprovarSolicitacao() {
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        setAuth(mestre);
        JogoParticipante aprovado = participanteService.aprovar(jogo.getId(), pendente.getId());

        assertThat(aprovado.getStatus()).isEqualTo(StatusParticipante.APROVADO);
    }

    @Test
    @DisplayName("não-Mestre não pode aprovar solicitação")
    void naoDevePermitirAprovacaoPorNaoMestre() {
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        setAuth(outroJogador);
        assertThrows(ForbiddenException.class, () ->
            participanteService.aprovar(jogo.getId(), pendente.getId()));
    }

    @Test
    @DisplayName("não pode aprovar participante que não está PENDENTE")
    void naoDeveAprovarStatusInvalido() {
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        setAuth(mestre);
        participanteService.aprovar(jogo.getId(), pendente.getId());

        // Tentar aprovar novamente
        assertThrows(BusinessException.class, () ->
            participanteService.aprovar(jogo.getId(), pendente.getId()));
    }

    // ===== REJEITAR =====

    @Test
    @DisplayName("Mestre deve rejeitar solicitação pendente")
    void deveMestreRejeitarSolicitacao() {
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        setAuth(mestre);
        JogoParticipante rejeitado = participanteService.rejeitar(jogo.getId(), pendente.getId());

        assertThat(rejeitado.getStatus()).isEqualTo(StatusParticipante.REJEITADO);
    }

    // ===== BANIR =====

    @Test
    @DisplayName("Mestre deve banir participante aprovado")
    void deveMestreBanirParticipante() {
        // Adiciona jogador aprovado
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .status(StatusParticipante.APROVADO)
            .build());

        setAuth(mestre);
        JogoParticipante banido = participanteService.banir(jogo.getId(), aprovado.getId());

        assertThat(banido.getStatus()).isEqualTo(StatusParticipante.BANIDO);
    }

    @Test
    @DisplayName("Mestre não pode se banir do próprio jogo")
    void naoDevePermitirMestreBanirASiMesmo() {
        JogoParticipante mestreParticipante = participanteRepository
            .findByJogoIdAndUsuarioId(jogo.getId(), mestre.getId()).orElseThrow();

        setAuth(mestre);
        assertThrows(BusinessException.class, () ->
            participanteService.banir(jogo.getId(), mestreParticipante.getId()));
    }

    // ===== LISTAR =====

    @Test
    @DisplayName("Mestre deve listar todos os participantes sem filtro")
    void deveListarTodosParticipantesSendoMestre() {
        // Adiciona jogador pendente
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.PENDENTE).build());
        // Adiciona outro aprovado
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(outroJogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        setAuth(mestre);
        List<JogoParticipante> lista = participanteService.listar(jogo.getId(), null);

        // Mestre + pendente + aprovado = 3
        assertThat(lista).hasSize(3);
    }

    @Test
    @DisplayName("Mestre deve listar apenas PENDENTES quando filtro é PENDENTE")
    void deveListarApenasParticipantesPendentesSendoMestreComFiltro() {
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.PENDENTE).build());
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(outroJogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        setAuth(mestre);
        List<JogoParticipante> lista = participanteService.listar(jogo.getId(), StatusParticipante.PENDENTE);

        assertThat(lista).hasSize(1);
        assertThat(lista).allMatch(p -> StatusParticipante.PENDENTE.equals(p.getStatus()));
    }

    @Test
    @DisplayName("Jogador deve listar apenas participantes APROVADOS ignorando filtro")
    void deveListarApenasAprovadosSendoJogador() {
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(outroJogador).role(RoleJogo.JOGADOR).status(StatusParticipante.PENDENTE).build());

        setAuth(jogador);
        List<JogoParticipante> lista = participanteService.listar(jogo.getId(), StatusParticipante.PENDENTE);

        // Jogador sempre vê só APROVADOS, independente do filtro: Mestre + jogador = 2
        assertThat(lista).hasSize(2);
        assertThat(lista).allMatch(p -> StatusParticipante.APROVADO.equals(p.getStatus()));
    }

    // ===== RE-SOLICITACAO (strategy Reactivate) =====

    @Test
    @DisplayName("deve permitir re-solicitação após rejeição, reutilizando mesmo registro")
    void devePermitirReSolicitacaoAposRejeicao() {
        // Arrange: solicitar e rejeitar
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        setAuth(mestre);
        participanteService.rejeitar(jogo.getId(), pendente.getId());

        // Act: re-solicitar
        setAuth(jogador);
        JogoParticipante reativado = participanteService.solicitar(jogo.getId());

        // Assert: mesmo ID (registro reutilizado), status PENDENTE
        assertThat(reativado.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
        assertThat(reativado.getId()).isEqualTo(pendente.getId());
        assertThat(reativado.isActive()).isTrue();
    }

    @Test
    @DisplayName("deve permitir re-solicitação após remoção (soft-deleted), reutilizando mesmo registro")
    void devePermitirReSolicitacaoAposRemocao() {
        // Arrange: criar registro aprovado e depois soft-deletar
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .status(StatusParticipante.APROVADO)
            .build());
        aprovado.delete();
        participanteRepository.saveAndFlush(aprovado);

        // Act: solicitar (deve reativar o registro removido)
        setAuth(jogador);
        JogoParticipante reativado = participanteService.solicitar(jogo.getId());

        // Assert: mesmo ID (registro reutilizado), status PENDENTE, deletedAt nulo
        assertThat(reativado.getId()).isEqualTo(aprovado.getId());
        assertThat(reativado.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
        assertThat(reativado.isActive()).isTrue();
    }

    @Test
    @DisplayName("não deve permitir solicitação se BANIDO")
    void naoDevePermitirSolicitacaoSeBanido() {
        // Arrange: criar aprovado e banir
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .status(StatusParticipante.APROVADO)
            .build());

        setAuth(mestre);
        participanteService.banir(jogo.getId(), aprovado.getId());

        // Act + Assert
        setAuth(jogador);
        assertThrows(ConflictException.class, () -> participanteService.solicitar(jogo.getId()));
    }

    @Test
    @DisplayName("não deve permitir solicitação se já APROVADO")
    void naoDevePermitirSolicitacaoSeAprovado() {
        // Arrange: inserir participante APROVADO diretamente
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .status(StatusParticipante.APROVADO)
            .build());

        // Act + Assert
        setAuth(jogador);
        assertThrows(ConflictException.class, () -> participanteService.solicitar(jogo.getId()));
    }

    @Test
    @DisplayName("não deve permitir solicitação se já PENDENTE")
    void naoDevePermitirSolicitacaoSePendente() {
        // Arrange: solicitar uma primeira vez (cria PENDENTE)
        setAuth(jogador);
        participanteService.solicitar(jogo.getId());

        // Act + Assert: segunda solicitação deve falhar
        assertThrows(ConflictException.class, () -> participanteService.solicitar(jogo.getId()));
    }

    @Test
    @DisplayName("deve criar novo registro na primeira vez que usuário solicita")
    void deveCriarNovoRegistroNaPrimeiraVez() {
        // Arrange: sem nenhum registro existente para o jogador

        // Act
        setAuth(jogador);
        JogoParticipante resultado = participanteService.solicitar(jogo.getId());

        // Assert
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(StatusParticipante.PENDENTE);
        assertThat(resultado.getRole()).isEqualTo(RoleJogo.JOGADOR);
        assertThat(resultado.getUsuario().getId()).isEqualTo(jogador.getId());
        assertThat(resultado.isActive()).isTrue();
    }

    // ===== BANIR (restrição APROVADO) =====

    @Test
    @DisplayName("banir deve rejeitar participante PENDENTE com BusinessException")
    void naoDeveBanirParticipantePendente() {
        // Arrange: jogador com solicitação pendente
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        // Act + Assert
        setAuth(mestre);
        assertThrows(BusinessException.class, () ->
            participanteService.banir(jogo.getId(), pendente.getId()));
    }

    // ===== DESBANIR =====

    @Test
    @DisplayName("deve desbanir participante BANIDO retornando status APROVADO")
    void deveDesbanirParticipanteBanido() {
        // Arrange: jogador aprovado e depois banido
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        setAuth(mestre);
        participanteService.banir(jogo.getId(), aprovado.getId());

        // Act
        JogoParticipante desbanido = participanteService.desbanir(jogo.getId(), aprovado.getId());

        // Assert
        assertThat(desbanido.getStatus()).isEqualTo(StatusParticipante.APROVADO);
    }

    @Test
    @DisplayName("desbanir deve rejeitar participante não-BANIDO com BusinessException")
    void naoDeveDesbanirParticipanteAprovado() {
        // Arrange: jogador aprovado diretamente (sem banimento)
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        // Act + Assert
        setAuth(mestre);
        assertThrows(BusinessException.class, () ->
            participanteService.desbanir(jogo.getId(), aprovado.getId()));
    }

    // ===== REMOVER (soft delete) =====

    @Test
    @DisplayName("deve remover participante APROVADO com soft delete (deletedAt preenchido)")
    void deveRemoverParticipanteAprovadoComSoftDelete() {
        // Arrange
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        // Act
        setAuth(mestre);
        participanteService.remover(jogo.getId(), aprovado.getId());

        // Assert: registro deve estar soft-deleted (não retornado por findByJogoIdAndUsuarioId)
        var resultado = participanteRepository.findByJogoIdAndUsuarioId(jogo.getId(), jogador.getId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("remover deve rejeitar participante não-APROVADO com BusinessException")
    void naoDeveRemoverParticipantePendente() {
        // Arrange: jogador com solicitação pendente
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        // Act + Assert
        setAuth(mestre);
        assertThrows(BusinessException.class, () ->
            participanteService.remover(jogo.getId(), pendente.getId()));
    }

    // ===== MEU STATUS =====

    @Test
    @DisplayName("meuStatus deve retornar participação do usuário autenticado")
    void deveMeuStatusRetornarParticipacaoDoUsuarioAtual() {
        // Arrange: jogador solicita entrada
        setAuth(jogador);
        JogoParticipante pendente = participanteService.solicitar(jogo.getId());

        // Act
        var resultado = participanteService.meuStatus(jogo.getId());

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(pendente.getId());
        assertThat(resultado.get().getStatus()).isEqualTo(StatusParticipante.PENDENTE);
    }

    @Test
    @DisplayName("meuStatus deve retornar Optional.empty se usuário nunca solicitou")
    void deveMeuStatusRetornarVazioSeNuncaSolicitou() {
        // Arrange: nenhum registro para outroJogador
        setAuth(outroJogador);

        // Act
        var resultado = participanteService.meuStatus(jogo.getId());

        // Assert
        assertThat(resultado).isEmpty();
    }

    // ===== CANCELAR SOLICITACAO =====

    @Test
    @DisplayName("deve cancelar solicitação PENDENTE do próprio usuário (soft delete)")
    void deveCancelarSolicitacaoPendente() {
        // Arrange
        setAuth(jogador);
        participanteService.solicitar(jogo.getId());

        // Act
        participanteService.cancelarSolicitacao(jogo.getId());

        // Assert: registro removido (soft-deleted)
        var resultado = participanteRepository.findByJogoIdAndUsuarioId(jogo.getId(), jogador.getId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("cancelarSolicitacao deve rejeitar status não-PENDENTE com BusinessException")
    void naoDeveCancelarSolicitacaoAprovada() {
        // Arrange: jogador já aprovado
        participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        // Act + Assert
        setAuth(jogador);
        assertThrows(BusinessException.class, () ->
            participanteService.cancelarSolicitacao(jogo.getId()));
    }

    @Test
    @DisplayName("cancelarSolicitacao deve lançar ResourceNotFoundException quando usuário não tem participação")
    void naoDeveCancelarSolicitacaoInexistente() {
        // Arrange: outroJogador nunca solicitou entrada neste jogo
        setAuth(outroJogador);

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () ->
            participanteService.cancelarSolicitacao(jogo.getId()));
    }

    @Test
    @DisplayName("banir deve manter registro visível (deletedAt nulo) pois BANIDO é diferente de soft-deleted")
    void banirNaoDeveSetarDeletedAt() {
        // Arrange
        JogoParticipante aprovado = participanteRepository.save(JogoParticipante.builder()
            .jogo(jogo).usuario(jogador).role(RoleJogo.JOGADOR).status(StatusParticipante.APROVADO).build());

        // Act
        setAuth(mestre);
        JogoParticipante banido = participanteService.banir(jogo.getId(), aprovado.getId());

        // Assert: status BANIDO, mas deletedAt permanece nulo (não é soft delete)
        assertThat(banido.getStatus()).isEqualTo(StatusParticipante.BANIDO);
        assertThat(banido.getDeletedAt()).isNull();
    }
}
