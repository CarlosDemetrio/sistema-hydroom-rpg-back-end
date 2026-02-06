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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para JogoRepository.
 * Usa H2 em memória para testes rápidos e isolados.
 *
 * @author Carlos Demétrio
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("JogoRepository - Testes de Integração")
class JogoRepositoryTest {

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private JogoParticipanteRepository participanteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario mestre;
    private Usuario jogador;
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

        jogador = Usuario.builder()
            .nome("Jogador RPG")
            .email("jogador@test.com")
            .provider("google")
            .providerId("google456")
            .build();
        jogador = usuarioRepository.save(jogador);

        // Criar jogo de teste
        jogo = Jogo.builder()
            .nome("Campanha de Tormenta")
            .descricao("Uma aventura épica no mundo de Arton")
            .dataInicio(LocalDate.now())
            .build();
        jogo = jogoRepository.save(jogo);

        // Adicionar participantes
        JogoParticipante mestreParticipante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(mestre)
            .role(RoleJogo.MESTRE)
            .build();
        participanteRepository.save(mestreParticipante);

        JogoParticipante jogadorParticipante = JogoParticipante.builder()
            .jogo(jogo)
            .usuario(jogador)
            .role(RoleJogo.JOGADOR)
            .build();
        participanteRepository.save(jogadorParticipante);

        // Atualizar a lista de participantes do jogo
        jogo.getParticipantes().add(mestreParticipante);
        jogo.getParticipantes().add(jogadorParticipante);
    }

    @Test
    @DisplayName("Deve salvar um jogo com sucesso")
    void testSalvarJogo() {
        // Arrange
        Jogo novoJogo = Jogo.builder()
            .nome("D&D 5e - Lost Mine of Phandelver")
            .descricao("Aventura iniciante de D&D")
            .dataInicio(LocalDate.now())
            .build();

        // Act
        Jogo saved = jogoRepository.save(novoJogo);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNome()).isEqualTo("D&D 5e - Lost Mine of Phandelver");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar jogos ativos")
    void testBuscarJogosAtivos() {
        // Arrange
        Jogo jogoInativo = Jogo.builder()
            .nome("Jogo Inativo")
            .build();
        jogoInativo = jogoRepository.save(jogoInativo);
        jogoInativo.delete();
        jogoRepository.save(jogoInativo);

        // Act
        List<Jogo> jogosAtivos = jogoRepository.findByAtivoTrue();

        // Assert
        assertThat(jogosAtivos).hasSize(1);
        assertThat(jogosAtivos.get(0).getNome()).isEqualTo("Campanha de Tormenta");
    }

    @Test
    @DisplayName("Deve buscar jogo ativo por ID")
    void testBuscarJogoAtivoPorId() {
        // Act
        Optional<Jogo> found = jogoRepository.findByIdAndAtivoTrue(jogo.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Campanha de Tormenta");
    }

    @Test
    @DisplayName("Não deve buscar jogo inativo por ID")
    void testNaoBuscarJogoInativoPorId() {
        // Arrange
        jogo.delete();
        jogoRepository.save(jogo);

        // Act
        Optional<Jogo> found = jogoRepository.findByIdAndAtivoTrue(jogo.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar jogos por usuário ID")
    void testBuscarJogosPorUsuarioId() {
        // Act
        List<Jogo> jogosDoMestre = jogoRepository.findJogosByUsuarioId(mestre.getId());
        List<Jogo> jogosDoJogador = jogoRepository.findJogosByUsuarioId(jogador.getId());

        // Assert
        assertThat(jogosDoMestre).hasSize(1);
        assertThat(jogosDoJogador).hasSize(1);
        assertThat(jogosDoMestre.get(0).getId()).isEqualTo(jogo.getId());
        assertThat(jogosDoJogador.get(0).getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Deve buscar jogos por mestre")
    void testBuscarJogosPorMestre() {
        // Act
        List<Jogo> jogosDoMestre = jogoRepository.findJogosByMestre(mestre.getId());
        List<Jogo> jogosDoJogador = jogoRepository.findJogosByMestre(jogador.getId());

        // Assert
        assertThat(jogosDoMestre).hasSize(1);
        assertThat(jogosDoJogador).isEmpty();
        assertThat(jogosDoMestre.get(0).getId()).isEqualTo(jogo.getId());
    }

    @Test
    @DisplayName("Não deve buscar jogos de participantes inativos")
    void testNaoBuscarJogosDeParticipantesInativos() {
        // Arrange - Desativar participação do jogador (soft delete)
        JogoParticipante participante = participanteRepository
            .findByJogoIdAndUsuarioIdAndAtivoTrue(jogo.getId(), jogador.getId())
            .orElseThrow();
        participante.delete();
        participanteRepository.save(participante);

        // Act
        List<Jogo> jogosDoJogador = jogoRepository.findJogosByUsuarioId(jogador.getId());

        // Assert
        assertThat(jogosDoJogador).isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar jogo")
    void testAtualizarJogo() {
        // Arrange
        jogo.setNome("Campanha de Tormenta - ATUALIZADO");
        jogo.setDescricao("Descrição atualizada");

        // Act
        Jogo updated = jogoRepository.save(jogo);

        // Assert
        assertThat(updated.getNome()).isEqualTo("Campanha de Tormenta - ATUALIZADO");
        assertThat(updated.getDescricao()).isEqualTo("Descrição atualizada");
    }

    @Test
    @DisplayName("Deve desativar jogo (soft delete)")
    void testDesativarJogo() {
        // Act
        jogo.delete();
        jogoRepository.save(jogo);
        Optional<Jogo> found = jogoRepository.findByIdAndAtivoTrue(jogo.getId());

        // Assert
        assertThat(found).isEmpty();
        assertThat(jogoRepository.findById(jogo.getId())).isPresent();
    }

    @Test
    @DisplayName("Deve deletar jogo")
    void testDeletarJogo() {
        // Arrange
        Long jogoId = jogo.getId();

        // Act
        jogoRepository.deleteById(jogoId);

        // Assert
        assertThat(jogoRepository.findById(jogoId)).isEmpty();
    }
}
