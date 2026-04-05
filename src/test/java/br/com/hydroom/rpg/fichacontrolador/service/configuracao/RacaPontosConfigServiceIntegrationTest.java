package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.RacaVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RacaPontosConfig;
import br.com.hydroom.rpg.fichacontrolador.model.RacaVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaPontosConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaVantagemPreDefinidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayName("RacaPontosConfigService e RacaVantagemPreDefinidaService - Testes de Integração")
class RacaPontosConfigServiceIntegrationTest {

    @Autowired
    private RacaPontosConfigService racaPontosConfigService;

    @Autowired
    private RacaVantagemPreDefinidaService racaVantagemPreDefinidaService;

    @Autowired
    private RacaPontosConfigRepository racaPontosConfigRepository;

    @Autowired
    private RacaVantagemPreDefinidaRepository racaVantagemPreDefinidaRepository;

    @Autowired
    private ConfiguracaoRacaRepository racaRepository;

    @Autowired
    private VantagemConfigRepository vantagemConfigRepository;

    @Autowired
    private JogoRepository jogoRepository;

    private Jogo jogo;
    private Jogo outroJogo;
    private Raca elfo;

    @BeforeEach
    void setUp() {
        racaVantagemPreDefinidaRepository.deleteAll();
        racaPontosConfigRepository.deleteAll();
        vantagemConfigRepository.deleteAll();
        racaRepository.deleteAll();
        jogoRepository.deleteAll();

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Teste " + uniqueSuffix())
            .descricao("Jogo para testes de raça")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outro Jogo " + uniqueSuffix())
            .descricao("Jogo alternativo para isolamento")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        elfo = racaRepository.save(Raca.builder()
            .jogo(jogo)
            .nome("Elfo " + uniqueSuffix())
            .descricao("Raça élfica ágil e perceptiva")
            .ordemExibicao(1)
            .build());
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // ===== TESTES: RacaPontosConfig =====

    @Test
    @DisplayName("T2-04: Deve criar RacaPontosConfig com sucesso")
    void deveCriarRacaPontosConfigComSucesso() {
        RacaPontosConfig pontosConfig = RacaPontosConfig.builder()
            .nivel(1)
            .pontosAtributo(1)
            .pontosVantagem(0)
            .build();

        RacaPontosConfig salvo = racaPontosConfigService.criar(elfo.getId(), pontosConfig);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getRaca().getId()).isEqualTo(elfo.getId());
        assertThat(salvo.getNivel()).isEqualTo(1);
        assertThat(salvo.getPontosAtributo()).isEqualTo(1);
        assertThat(salvo.getPontosVantagem()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve listar RacaPontosConfig após criação")
    void deveListarRacaPontosConfigAposCriacao() {
        RacaPontosConfig pontosConfig = RacaPontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();
        racaPontosConfigService.criar(elfo.getId(), pontosConfig);

        List<RacaPontosConfig> lista = racaPontosConfigService.listarPorRaca(elfo.getId());

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve rejeitar duplicata de nível na mesma raça")
    void deveRejeitarDuplicataDeNivelNaRaca() {
        RacaPontosConfig pontos1 = RacaPontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();
        racaPontosConfigService.criar(elfo.getId(), pontos1);

        RacaPontosConfig pontos2 = RacaPontosConfig.builder()
            .nivel(1).pontosAtributo(2).pontosVantagem(0).build();

        assertThrows(ConflictException.class,
            () -> racaPontosConfigService.criar(elfo.getId(), pontos2));
    }

    @Test
    @DisplayName("T2-05: Deve aplicar soft delete em RacaPontosConfig")
    void deveAplicarSoftDeleteEmRacaPontosConfig() {
        RacaPontosConfig pontosConfig = RacaPontosConfig.builder()
            .nivel(3).pontosAtributo(1).pontosVantagem(0).build();
        RacaPontosConfig salvo = racaPontosConfigService.criar(elfo.getId(), pontosConfig);

        racaPontosConfigService.deletar(elfo.getId(), salvo.getId());

        List<RacaPontosConfig> lista = racaPontosConfigService.listarPorRaca(elfo.getId());
        assertThat(lista).isEmpty();

        RacaPontosConfig deletado = racaPontosConfigRepository.findById(salvo.getId()).orElseThrow();
        assertThat(deletado.isActive()).isFalse();
        assertThat(deletado.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("T2-06: PUT deve atualizar RacaPontosConfig corretamente")
    void deveAtualizarRacaPontosConfigCorretamente() {
        RacaPontosConfig pontosConfig = RacaPontosConfig.builder()
            .nivel(2).pontosAtributo(1).pontosVantagem(0).build();
        RacaPontosConfig salvo = racaPontosConfigService.criar(elfo.getId(), pontosConfig);

        RacaPontosConfig atualizado = RacaPontosConfig.builder()
            .nivel(2).pontosAtributo(3).pontosVantagem(1).build();

        RacaPontosConfig resultado = racaPontosConfigService.atualizar(elfo.getId(), salvo.getId(), atualizado);

        assertThat(resultado.getPontosAtributo()).isEqualTo(3);
        assertThat(resultado.getPontosVantagem()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao criar pontos para raça inexistente")
    void deveLancarExcecaoAoCriarParaRacaInexistente() {
        RacaPontosConfig pontosConfig = RacaPontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();

        assertThrows(ResourceNotFoundException.class,
            () -> racaPontosConfigService.criar(999L, pontosConfig));
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao deletar PontosConfig de outra raça")
    void deveLancarExcecaoAoDeletarDeOutraRaca() {
        Raca outraRaca = racaRepository.save(Raca.builder()
            .jogo(jogo)
            .nome("Anão " + uniqueSuffix())
            .ordemExibicao(2)
            .build());

        RacaPontosConfig pontosConfig = RacaPontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();
        RacaPontosConfig salvo = racaPontosConfigService.criar(elfo.getId(), pontosConfig);

        assertThrows(ValidationException.class,
            () -> racaPontosConfigService.deletar(outraRaca.getId(), salvo.getId()));
    }

    // ===== TESTES: RacaVantagemPreDefinida =====

    @Test
    @DisplayName("Deve criar RacaVantagemPreDefinida com sucesso")
    void deveCriarRacaVantagemPreDefinidaComSucesso() {
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo)
            .nome("Visão Noturna " + uniqueSuffix())
            .nivelMaximo(5)
            .formulaCusto("NIVEL * 2")
            .ordemExibicao(1)
            .build());

        RacaVantagemPreDefinidaRequest request = new RacaVantagemPreDefinidaRequest(1, vantagem.getId());

        RacaVantagemPreDefinida salvo = racaVantagemPreDefinidaService.criar(elfo.getId(), request);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNivel()).isEqualTo(1);
        assertThat(salvo.getVantagemConfig().getId()).isEqualTo(vantagem.getId());
        assertThat(salvo.getRaca().getId()).isEqualTo(elfo.getId());
    }

    @Test
    @DisplayName("T2-03: Deve rejeitar VantagemConfig de jogo diferente para Raca")
    void deveRejeitarVantagemDeJogoDiferenteParaRaca() {
        VantagemConfig vantagemOutroJogo = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(outroJogo)
            .nome("Habilidade " + uniqueSuffix())
            .nivelMaximo(5)
            .formulaCusto("NIVEL")
            .ordemExibicao(1)
            .build());

        RacaVantagemPreDefinidaRequest request =
            new RacaVantagemPreDefinidaRequest(1, vantagemOutroJogo.getId());

        assertThrows(ValidationException.class,
            () -> racaVantagemPreDefinidaService.criar(elfo.getId(), request));
    }

    @Test
    @DisplayName("Deve rejeitar RacaVantagemPreDefinida duplicada")
    void deveRejeitarRacaVantagemPreDefinidaDuplicada() {
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Agilidade " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(1).build());

        RacaVantagemPreDefinidaRequest request = new RacaVantagemPreDefinidaRequest(2, vantagem.getId());
        racaVantagemPreDefinidaService.criar(elfo.getId(), request);

        assertThrows(ConflictException.class,
            () -> racaVantagemPreDefinidaService.criar(elfo.getId(), request));
    }

    @Test
    @DisplayName("Deve aplicar soft delete em RacaVantagemPreDefinida")
    void deveAplicarSoftDeleteEmRacaVantagemPreDefinida() {
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Leveza " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(1).build());

        RacaVantagemPreDefinida salvo = racaVantagemPreDefinidaService.criar(elfo.getId(),
            new RacaVantagemPreDefinidaRequest(1, vantagem.getId()));

        racaVantagemPreDefinidaService.deletar(elfo.getId(), salvo.getId());

        List<RacaVantagemPreDefinida> lista = racaVantagemPreDefinidaService.listarPorRaca(elfo.getId());
        assertThat(lista).isEmpty();

        RacaVantagemPreDefinida deletado = racaVantagemPreDefinidaRepository.findById(salvo.getId()).orElseThrow();
        assertThat(deletado.isActive()).isFalse();
    }

    @Test
    @DisplayName("Deve listar vantagens pré-definidas por raça")
    void deveListarVantagensPreDefinidasPorRaca() {
        VantagemConfig v1 = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("V1 " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(1).build());
        VantagemConfig v2 = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("V2 " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(2).build());

        racaVantagemPreDefinidaService.criar(elfo.getId(), new RacaVantagemPreDefinidaRequest(1, v1.getId()));
        racaVantagemPreDefinidaService.criar(elfo.getId(), new RacaVantagemPreDefinidaRequest(5, v2.getId()));

        List<RacaVantagemPreDefinida> lista = racaVantagemPreDefinidaService.listarPorRaca(elfo.getId());
        assertThat(lista).hasSize(2);
    }
}
