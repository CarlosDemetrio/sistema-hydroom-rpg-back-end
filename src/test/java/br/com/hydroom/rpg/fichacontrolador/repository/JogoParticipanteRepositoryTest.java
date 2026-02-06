package br.com.hydroom.rpg.fichacontrolador.repository;

import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.JogoParticipante;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração para JogoParticipanteRepository.
 * Usa H2 em memória para testes rápidos e isolados.
 *
 * @author Carlos Demétrio
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("JogoParticipanteRepository - Testes de Integração")
class JogoParticipanteRepositoryTest {

    @Autowired
    private JogoParticipanteRepository participanteRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario mestre;
    private Usuario jogador1;
    private Usuario jogador2;
    private Jogo jogo;

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        participanteRepository.deleteAll();
        jogoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Criar usuários de teste
        mestre = Usuario.builder()
            .nome("Mestre RPG")
            .email("mestre@test.com")
            .provider("google")
            .providerId("google123")
            .build();
        mestre = usuarioRepository.save(mestre);

        jogador1 = Usuario.builder()
            .nome("Jogador 1")
            .email("jogador1@test.com")
            .provider("google")
            .providerId("google456")
            .build();
        jogador1 = usuarioRepository.save(jogador1);

        jogador2 = Usuario.builder()
            .nome("Jogador 2")
            .email("jogador2@test.com")
            .provider("google")
            .providerId("google789")
            .build();
        jogador2 = usuarioRepository.save(jogador2);

        // Criar jogo de teste
        jogo = Jogo.builder()
            .nome("Campanha de Tormenta")
            .descricao("Uma aventura épica")
            .dataInicio(LocalDate.now())
            .build();
        jogo = jogoRepository.save(jogo);
    }

    @Test
    @DisplayName("Deve salvar participante com sucesso")
    void testSalvarParticipante() {
        // Arrange
        JogoParticipante participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();

        // Act
        JogoParticipante saved = participanteRepository.save(participante);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getJogo().getId()).isEqualTo(jogo.getId());
        assertThat(saved.getUsuario().getId()).isEqualTo(mestre.getId());
        assertThat(saved.getRole()).isEqualTo(RoleJogo.MESTRE);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar participantes por jogo ID")
    void testBuscarParticipantesPorJogoId() {
        // Arrange
        criarParticipantes();

        // Act
        List<JogoParticipante> participantes = participanteRepository.findByJogoId(jogo.getId());

        // Assert
        assertThat(participantes).hasSize(3);
        assertThat(participantes)
            .extracting(JogoParticipante::getRole)
            .containsExactlyInAnyOrder(RoleJogo.MESTRE, RoleJogo.JOGADOR, RoleJogo.JOGADOR);
    }

    @Test
    @DisplayName("Deve buscar participantes ativos por jogo ID")
    void testBuscarParticipantesAtivosPorJogoId() {
        // Arrange
        criarParticipantes();

        // Desativar um participante
        JogoParticipante participante = participanteRepository
            .findByJogoIdAndUsuarioId(jogo.getId(), jogador1.getId())
            .orElseThrow();
        participante.delete();
        participanteRepository.save(participante);

        // Act
        List<JogoParticipante> participantes = participanteRepository.findByJogoId(jogo.getId());

        // Assert
        assertThat(participantes).hasSize(2); // Apenas mestre e jogador2
    }

    @Test
    @DisplayName("Deve buscar participantes por usuário ID")
    void testBuscarParticipantesPorUsuarioId() {
        // Arrange
        criarParticipantes();

        // Act
        List<JogoParticipante> participantes = participanteRepository.findByUsuarioId(mestre.getId());

        // Assert
        assertThat(participantes).hasSize(1);
        assertThat(participantes.getFirst().getRole()).isEqualTo(RoleJogo.MESTRE);
    }

    @Test
    @DisplayName("Deve buscar participante por jogo e usuário")
    void testBuscarParticipantePorJogoEUsuario() {
        // Arrange
        criarParticipantes();

        // Act
        Optional<JogoParticipante> found = participanteRepository
            .findByJogoIdAndUsuarioId(jogo.getId(), mestre.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(RoleJogo.MESTRE);
    }

    @Test
    @DisplayName("Não deve buscar participante inativo")
    void testNaoBuscarParticipanteInativo() {
        // Arrange
        JogoParticipante participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();
        participante = participanteRepository.save(participante);
        participante.delete();
        participanteRepository.save(participante);

        // Act
        Optional<JogoParticipante> found = participanteRepository
            .findByJogoIdAndUsuarioId(jogo.getId(), mestre.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se usuário é mestre do jogo")
    void testVerificarSeMestre() {
        // Arrange
        criarParticipantes();

        // Act
        boolean mestreEhMestre = participanteRepository.existsByJogoIdAndUsuarioIdAndRole(
            jogo.getId(), mestre.getId(), RoleJogo.MESTRE);
        boolean jogadorEhMestre = participanteRepository.existsByJogoIdAndUsuarioIdAndRole(
            jogo.getId(), jogador1.getId(), RoleJogo.MESTRE);

        // Assert
        assertThat(mestreEhMestre).isTrue();
        assertThat(jogadorEhMestre).isFalse();
    }

    @Test
    @DisplayName("Deve listar jogadores de um jogo")
    void testListarJogadores() {
        // Arrange
        criarParticipantes();

        // Act
        List<JogoParticipante> jogadores = participanteRepository
            .findByJogoIdAndRole(jogo.getId(), RoleJogo.JOGADOR);

        // Assert
        assertThat(jogadores).hasSize(2);
        assertThat(jogadores)
            .extracting(p -> p.getUsuario().getNome())
            .containsExactlyInAnyOrder("Jogador 1", "Jogador 2");
    }

    @Test
    @DisplayName("Deve impedir participante duplicado (unique constraint)")
    void testParticipanteDuplicado() {
        // Arrange
        JogoParticipante participante1 = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();
        participanteRepository.save(participante1);

        JogoParticipante participante2 = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.JOGADOR)
            .build();

        // Act & Assert
        assertThatThrownBy(() -> {
            participanteRepository.saveAndFlush(participante2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve atualizar role do participante")
    void testAtualizarRole() {
        // Arrange
        JogoParticipante participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador1)
            .role(RoleJogo.JOGADOR)
            .build();
        participante = participanteRepository.save(participante);

        // Act
        participante.setRole(RoleJogo.MESTRE);
        JogoParticipante updated = participanteRepository.save(participante);

        // Assert
        assertThat(updated.getRole()).isEqualTo(RoleJogo.MESTRE);
    }

    @Test
    @DisplayName("Deve remover participante (soft delete)")
    void testRemoverParticipante() {
        // Arrange
        JogoParticipante participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();
        participante = participanteRepository.save(participante);
        Long participanteId = participante.getId();

        // Act
        participante.delete();
        participanteRepository.save(participante);

        // Assert
        Optional<JogoParticipante> found = participanteRepository
            .findByJogoIdAndUsuarioId(jogo.getId(), mestre.getId());
        assertThat(found).isEmpty();
        assertThat(participanteRepository.findById(participanteId)).isPresent();
    }

    @Test
    @DisplayName("Deve deletar participante")
    void testDeletarParticipante() {
        // Arrange
        JogoParticipante participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();
        participante = participanteRepository.save(participante);
        Long participanteId = participante.getId();

        // Act
        participanteRepository.delete(participante);

        // Assert
        assertThat(participanteRepository.findById(participanteId)).isEmpty();
    }

    /**
     * Helper method para criar participantes padrão.
     */
    private void criarParticipantes() {
        JogoParticipante mestreParticipante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();
        participanteRepository.save(mestreParticipante);

        JogoParticipante jogador1Participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador1)
            .role(RoleJogo.JOGADOR)
            .build();
        participanteRepository.save(jogador1Participante);

        JogoParticipante jogador2Participante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador2)
            .role(RoleJogo.JOGADOR)
            .build();
        participanteRepository.save(jogador2Participante);
    }
}
