package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseVantagemPreDefinidaRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePontosConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.Jogo;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ClassePontosConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseVantagemPreDefinidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoRepository;
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
@DisplayName("ClassePontosConfigService e ClasseVantagemPreDefinidaService - Testes de Integração")
class ClassePontosConfigServiceIntegrationTest {

    @Autowired
    private ClassePontosConfigService classePontosConfigService;

    @Autowired
    private ClasseVantagemPreDefinidaService classeVantagemPreDefinidaService;

    @Autowired
    private ClassePontosConfigRepository classePontosConfigRepository;

    @Autowired
    private ClasseVantagemPreDefinidaRepository classeVantagemPreDefinidaRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private VantagemConfigRepository vantagemConfigRepository;

    @Autowired
    private JogoRepository jogoRepository;

    private Jogo jogo;
    private Jogo outroJogo;
    private ClassePersonagem guerreiro;

    @BeforeEach
    void setUp() {
        classeVantagemPreDefinidaRepository.deleteAll();
        classePontosConfigRepository.deleteAll();
        vantagemConfigRepository.deleteAll();
        classeRepository.deleteAll();
        jogoRepository.deleteAll();

        jogo = jogoRepository.save(Jogo.builder()
            .nome("Campanha Teste " + uniqueSuffix())
            .descricao("Jogo para testes")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        outroJogo = jogoRepository.save(Jogo.builder()
            .nome("Outro Jogo " + uniqueSuffix())
            .descricao("Jogo alternativo para testes de isolamento")
            .dataInicio(LocalDate.now())
            .jogoAtivo(true)
            .build());

        guerreiro = classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Guerreiro " + uniqueSuffix())
            .descricao("Classe de combate")
            .ordemExibicao(1)
            .build());
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // ===== TESTES: ClassePontosConfig =====

    @Test
    @DisplayName("T2-01: Deve criar ClassePontosConfig com sucesso")
    void deveCriarClassePontosConfigComSucesso() {
        ClassePontosConfig pontosConfig = ClassePontosConfig.builder()
            .nivel(1)
            .pontosAtributo(2)
            .pontosVantagem(1)
            .build();

        ClassePontosConfig salvo = classePontosConfigService.criar(guerreiro.getId(), pontosConfig);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getClassePersonagem().getId()).isEqualTo(guerreiro.getId());
        assertThat(salvo.getNivel()).isEqualTo(1);
        assertThat(salvo.getPontosAtributo()).isEqualTo(2);
        assertThat(salvo.getPontosVantagem()).isEqualTo(1);
    }

    @Test
    @DisplayName("T2-01: GET deve retornar ClassePontosConfig após criação")
    void deveListarClassePontosConfigAposCriacao() {
        ClassePontosConfig pontosConfig = ClassePontosConfig.builder()
            .nivel(1)
            .pontosAtributo(2)
            .pontosVantagem(1)
            .build();
        classePontosConfigService.criar(guerreiro.getId(), pontosConfig);

        List<ClassePontosConfig> lista = classePontosConfigService.listarPorClasse(guerreiro.getId());

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("T2-02: Deve rejeitar duplicata de nível na mesma classe")
    void deveRejeitarDuplicataDeNivel() {
        ClassePontosConfig pontos1 = ClassePontosConfig.builder()
            .nivel(1)
            .pontosAtributo(2)
            .pontosVantagem(0)
            .build();
        classePontosConfigService.criar(guerreiro.getId(), pontos1);

        ClassePontosConfig pontos2 = ClassePontosConfig.builder()
            .nivel(1)
            .pontosAtributo(3)
            .pontosVantagem(1)
            .build();

        assertThrows(ConflictException.class,
            () -> classePontosConfigService.criar(guerreiro.getId(), pontos2));
    }

    @Test
    @DisplayName("T2-05: Deve aplicar soft delete em ClassePontosConfig")
    void deveAplicarSoftDeleteEmClassePontosConfig() {
        ClassePontosConfig pontosConfig = ClassePontosConfig.builder()
            .nivel(5)
            .pontosAtributo(1)
            .pontosVantagem(2)
            .build();
        ClassePontosConfig salvo = classePontosConfigService.criar(guerreiro.getId(), pontosConfig);

        classePontosConfigService.deletar(guerreiro.getId(), salvo.getId());

        List<ClassePontosConfig> lista = classePontosConfigService.listarPorClasse(guerreiro.getId());
        assertThat(lista).isEmpty();

        ClassePontosConfig deletado = classePontosConfigRepository.findById(salvo.getId()).orElseThrow();
        assertThat(deletado.isActive()).isFalse();
        assertThat(deletado.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("T2-06: PUT deve atualizar ClassePontosConfig corretamente")
    void deveAtualizarClassePontosConfigCorretamente() {
        ClassePontosConfig pontosConfig = ClassePontosConfig.builder()
            .nivel(1)
            .pontosAtributo(2)
            .pontosVantagem(0)
            .build();
        ClassePontosConfig salvo = classePontosConfigService.criar(guerreiro.getId(), pontosConfig);

        ClassePontosConfig atualizado = ClassePontosConfig.builder()
            .nivel(1)
            .pontosAtributo(5)
            .pontosVantagem(0)
            .build();

        ClassePontosConfig resultado = classePontosConfigService.atualizar(guerreiro.getId(), salvo.getId(), atualizado);

        assertThat(resultado.getPontosAtributo()).isEqualTo(5);
        assertThat(resultado.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve rejeitar atualização com nível duplicado")
    void deveRejeitarAtualizacaoComNivelDuplicado() {
        ClassePontosConfig pontos1 = ClassePontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();
        ClassePontosConfig pontos2 = ClassePontosConfig.builder()
            .nivel(2).pontosAtributo(1).pontosVantagem(0).build();

        classePontosConfigService.criar(guerreiro.getId(), pontos1);
        ClassePontosConfig salvo2 = classePontosConfigService.criar(guerreiro.getId(), pontos2);

        ClassePontosConfig tentativaConflito = ClassePontosConfig.builder()
            .nivel(1).pontosAtributo(3).pontosVantagem(0).build();

        assertThrows(ConflictException.class,
            () -> classePontosConfigService.atualizar(guerreiro.getId(), salvo2.getId(), tentativaConflito));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao criar pontos para classe inexistente")
    void deveLancarExcecaoAoCriarParaClasseInexistente() {
        ClassePontosConfig pontosConfig = ClassePontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();

        assertThrows(ResourceNotFoundException.class,
            () -> classePontosConfigService.criar(999L, pontosConfig));
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao deletar PontosConfig de outra classe")
    void deveLancarExcecaoAoDeletarDeOutraClasse() {
        ClassePersonagem outraClasse = classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Mago " + uniqueSuffix())
            .ordemExibicao(2)
            .build());

        ClassePontosConfig pontosConfig = ClassePontosConfig.builder()
            .nivel(1).pontosAtributo(1).pontosVantagem(0).build();
        ClassePontosConfig salvo = classePontosConfigService.criar(guerreiro.getId(), pontosConfig);

        assertThrows(ValidationException.class,
            () -> classePontosConfigService.deletar(outraClasse.getId(), salvo.getId()));
    }

    // ===== TESTES: ClasseVantagemPreDefinida =====

    @Test
    @DisplayName("Deve criar ClasseVantagemPreDefinida com sucesso")
    void deveCriarClasseVantagemPreDefinidaComSucesso() {
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo)
            .nome("Fortitude " + uniqueSuffix())
            .nivelMaximo(5)
            .formulaCusto("NIVEL * 2")
            .ordemExibicao(1)
            .build());

        ClasseVantagemPreDefinidaRequest request = new ClasseVantagemPreDefinidaRequest(5, vantagem.getId());

        ClasseVantagemPreDefinida salvo = classeVantagemPreDefinidaService.criar(guerreiro.getId(), request);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNivel()).isEqualTo(5);
        assertThat(salvo.getVantagemConfig().getId()).isEqualTo(vantagem.getId());
        assertThat(salvo.getClassePersonagem().getId()).isEqualTo(guerreiro.getId());
    }

    @Test
    @DisplayName("T2-03: Deve rejeitar VantagemConfig de jogo diferente")
    void deveRejeitarVantagemDeJogoDiferente() {
        VantagemConfig vantagemOutroJogo = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(outroJogo)
            .nome("Fortitude " + uniqueSuffix())
            .nivelMaximo(5)
            .formulaCusto("NIVEL * 2")
            .ordemExibicao(1)
            .build());

        ClasseVantagemPreDefinidaRequest request =
            new ClasseVantagemPreDefinidaRequest(5, vantagemOutroJogo.getId());

        assertThrows(ValidationException.class,
            () -> classeVantagemPreDefinidaService.criar(guerreiro.getId(), request));
    }

    @Test
    @DisplayName("Deve rejeitar vantagem pré-definida duplicada (mesmo nível e vantagem)")
    void deveRejeitarVantagemPreDefinidaDuplicada() {
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo)
            .nome("Reflexos " + uniqueSuffix())
            .nivelMaximo(3)
            .formulaCusto("NIVEL")
            .ordemExibicao(1)
            .build());

        ClasseVantagemPreDefinidaRequest request = new ClasseVantagemPreDefinidaRequest(3, vantagem.getId());
        classeVantagemPreDefinidaService.criar(guerreiro.getId(), request);

        assertThrows(ConflictException.class,
            () -> classeVantagemPreDefinidaService.criar(guerreiro.getId(), request));
    }

    @Test
    @DisplayName("Deve aplicar soft delete em ClasseVantagemPreDefinida")
    void deveAplicarSoftDeleteEmClasseVantagemPreDefinida() {
        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo)
            .nome("Escudo Pesado " + uniqueSuffix())
            .nivelMaximo(5)
            .formulaCusto("NIVEL")
            .ordemExibicao(1)
            .build());

        ClasseVantagemPreDefinidaRequest request = new ClasseVantagemPreDefinidaRequest(3, vantagem.getId());
        ClasseVantagemPreDefinida salvo = classeVantagemPreDefinidaService.criar(guerreiro.getId(), request);

        classeVantagemPreDefinidaService.deletar(guerreiro.getId(), salvo.getId());

        List<ClasseVantagemPreDefinida> lista = classeVantagemPreDefinidaService.listarPorClasse(guerreiro.getId());
        assertThat(lista).isEmpty();

        ClasseVantagemPreDefinida deletado = classeVantagemPreDefinidaRepository.findById(salvo.getId()).orElseThrow();
        assertThat(deletado.isActive()).isFalse();
    }

    @Test
    @DisplayName("Deve listar vantagens pré-definidas por classe")
    void deveListarVantagensPreDefinidasPorClasse() {
        VantagemConfig vantagem1 = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Vantagem1 " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(1).build());
        VantagemConfig vantagem2 = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Vantagem2 " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(2).build());

        classeVantagemPreDefinidaService.criar(guerreiro.getId(),
            new ClasseVantagemPreDefinidaRequest(1, vantagem1.getId()));
        classeVantagemPreDefinidaService.criar(guerreiro.getId(),
            new ClasseVantagemPreDefinidaRequest(3, vantagem2.getId()));

        List<ClasseVantagemPreDefinida> lista = classeVantagemPreDefinidaService.listarPorClasse(guerreiro.getId());
        assertThat(lista).hasSize(2);
    }

    @Test
    @DisplayName("Deve lançar ValidationException ao deletar VantagemPreDefinida de outra classe")
    void deveLancarExcecaoAoDeletarVantagemDeOutraClasse() {
        ClassePersonagem outraClasse = classeRepository.save(ClassePersonagem.builder()
            .jogo(jogo)
            .nome("Paladino " + uniqueSuffix())
            .ordemExibicao(3)
            .build());

        VantagemConfig vantagem = vantagemConfigRepository.save(VantagemConfig.builder()
            .jogo(jogo).nome("Aura " + uniqueSuffix())
            .nivelMaximo(5).formulaCusto("NIVEL").ordemExibicao(1).build());

        ClasseVantagemPreDefinida salvo = classeVantagemPreDefinidaService.criar(guerreiro.getId(),
            new ClasseVantagemPreDefinidaRequest(5, vantagem.getId()));

        assertThrows(ValidationException.class,
            () -> classeVantagemPreDefinidaService.deletar(outraClasse.getId(), salvo.getId()));
    }
}
