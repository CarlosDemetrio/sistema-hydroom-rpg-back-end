package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Implementação padrão do {@link GameDefaultConfigProvider}.
 *
 * <p>Fornece todos os dados default para inicialização de um jogo novo.</p>
 * <p>Valores são baseados nas regras canônicas do sistema Klayrah RPG.</p>
 *
 * <p><strong>Para customizar</strong>: Crie sua própria implementação de GameDefaultConfigProvider
 * e registre como @Primary no Spring Context.</p>
 *
 * @author Carlos Demétrio
 * @since 2026-02-05
 */
@Component
public class DefaultGameConfigProviderImpl implements GameDefaultConfigProvider {

    @Override
    public List<AtributoConfigDTO> getDefaultAtributos() {
        return List.of(
            AtributoConfigDTO.of(
                "Força",
                "FOR",
                "Capacidade física bruta, determina capacidade de carga",
                "total * 3",
                "kg",
                1
            ),
            AtributoConfigDTO.of(
                "Agilidade",
                "AGI",
                "Velocidade e reflexos, determina deslocamento",
                "total / 3",
                "metros",
                2
            ),
            AtributoConfigDTO.of(
                "Vigor",
                "VIG",
                "Resistência física, redução de dano físico",
                "total / 10",
                "RD",
                3
            ),
            AtributoConfigDTO.of(
                "Sabedoria",
                "SAB",
                "Resistência mágica, redução de dano mágico",
                "total / 10",
                "RDM",
                4
            ),
            AtributoConfigDTO.of(
                "Intuição",
                "INTU",
                "Sorte e percepção instintiva, pontos de sorte",
                "min(total / 20, 3)",
                "pontos",
                5
            ),
            AtributoConfigDTO.of(
                "Inteligência",
                "INT",
                "Capacidade de comando e raciocínio",
                "total / 20",
                "comando",
                6
            ),
            AtributoConfigDTO.of(
                "Astúcia",
                "AST",
                "Pensamento estratégico e tático",
                "total / 10",
                "estratégia",
                7
            )
        );
    }

    @Override
    public List<AptidaoConfigDTO> getDefaultAptidoes() {
        return List.of(
            // === Aptidões Físicas (12) ===
            AptidaoConfigDTO.of("Acrobacia",             "FISICA", "Habilidade de realizar manobras acrobáticas", 1),
            AptidaoConfigDTO.of("Guarda",                "FISICA", "Capacidade de defesa e bloqueio", 2),
            AptidaoConfigDTO.of("Aparar",                "FISICA", "Habilidade de desviar ataques", 3),
            AptidaoConfigDTO.of("Atletismo",             "FISICA", "Força física e condicionamento", 4),
            AptidaoConfigDTO.of("Resvalar",              "FISICA", "Capacidade de esquiva e movimento ágil", 5),
            AptidaoConfigDTO.of("Resistência",           "FISICA", "Capacidade de resistir a condições adversas", 6),
            AptidaoConfigDTO.of("Perseguição",           "FISICA", "Habilidade de perseguir ou fugir", 7),
            AptidaoConfigDTO.of("Natação",               "FISICA", "Capacidade de nadar e manobras aquáticas", 8),
            AptidaoConfigDTO.of("Furtividade",           "FISICA", "Capacidade de se mover sem ser detectado", 9),
            AptidaoConfigDTO.of("Prestidigitação",       "FISICA", "Destreza manual e truques", 10),
            AptidaoConfigDTO.of("Conduzir",              "FISICA", "Habilidade de pilotar veículos e montarias", 11),
            AptidaoConfigDTO.of("Arte da Fuga",          "FISICA", "Capacidade de escapar de restrições", 12),

            // === Aptidões Mentais (12) ===
            AptidaoConfigDTO.of("Idiomas",               "MENTAL", "Conhecimento de línguas", 13),
            AptidaoConfigDTO.of("Observação",            "MENTAL", "Capacidade de notar detalhes", 14),
            AptidaoConfigDTO.of("Falsificar",            "MENTAL", "Habilidade de criar falsificações", 15),
            AptidaoConfigDTO.of("Prontidão",             "MENTAL", "Capacidade de reagir rapidamente", 16),
            AptidaoConfigDTO.of("Auto Controle",         "MENTAL", "Controle emocional e mental", 17),
            AptidaoConfigDTO.of("Sentir Motivação",      "MENTAL", "Capacidade de ler intenções", 18),
            AptidaoConfigDTO.of("Sobrevivência",         "MENTAL", "Conhecimento de técnicas de sobrevivência", 19),
            AptidaoConfigDTO.of("Investigar",            "MENTAL", "Habilidade de coletar e analisar informações", 20),
            AptidaoConfigDTO.of("Blefar",                "MENTAL", "Capacidade de enganar e mentir", 21),
            AptidaoConfigDTO.of("Atuação",               "MENTAL", "Habilidade de interpretar personagens", 22),
            AptidaoConfigDTO.of("Diplomacia",            "MENTAL", "Capacidade de negociação e persuasão", 23),
            AptidaoConfigDTO.of("Operação de Mecanismos","MENTAL", "Conhecimento de dispositivos mecânicos", 24)
        );
    }

    @Override
    public List<NivelConfigDTO> getDefaultNiveis() {
        // Nível 0 até 35 (36 níveis total)
        // limitadorAtributo por faixa: 0-1=10, 2-20=50, 21-25=75, 26-30=100, 31-35=120
        return List.of(
            NivelConfigDTO.of(0,  0L,      0, 0, 0, 10),
            NivelConfigDTO.of(1,  1000L,   3, 1, 3, 10),
            NivelConfigDTO.of(2,  3000L,   3, 1, 3, 50),
            NivelConfigDTO.of(3,  6000L,   3, 1, 3, 50),
            NivelConfigDTO.of(4,  10000L,  3, 1, 3, 50),
            NivelConfigDTO.of(5,  15000L,  3, 1, 3, 50),
            NivelConfigDTO.of(6,  21000L,  3, 1, 3, 50),
            NivelConfigDTO.of(7,  28000L,  3, 1, 3, 50),
            NivelConfigDTO.of(8,  36000L,  3, 1, 3, 50),
            NivelConfigDTO.of(9,  45000L,  3, 1, 3, 50),
            NivelConfigDTO.of(10, 55000L,  3, 1, 3, 50),
            NivelConfigDTO.of(11, 66000L,  3, 1, 3, 50),
            NivelConfigDTO.of(12, 78000L,  3, 1, 3, 50),
            NivelConfigDTO.of(13, 91000L,  3, 1, 3, 50),
            NivelConfigDTO.of(14, 105000L, 3, 1, 3, 50),
            NivelConfigDTO.of(15, 120000L, 3, 1, 3, 50),
            NivelConfigDTO.of(16, 136000L, 3, 1, 3, 50),
            NivelConfigDTO.of(17, 153000L, 3, 1, 3, 50),
            NivelConfigDTO.of(18, 171000L, 3, 1, 3, 50),
            NivelConfigDTO.of(19, 190000L, 3, 1, 3, 50),
            NivelConfigDTO.of(20, 210000L, 3, 1, 3, 50),
            NivelConfigDTO.of(21, 231000L, 3, 1, 3, 75),
            NivelConfigDTO.of(22, 253000L, 3, 1, 3, 75),
            NivelConfigDTO.of(23, 276000L, 3, 1, 3, 75),
            NivelConfigDTO.of(24, 300000L, 3, 1, 3, 75),
            NivelConfigDTO.of(25, 325000L, 3, 1, 3, 75),
            NivelConfigDTO.of(26, 351000L, 3, 1, 3, 100),
            NivelConfigDTO.of(27, 378000L, 3, 1, 3, 100),
            NivelConfigDTO.of(28, 406000L, 3, 1, 3, 100),
            NivelConfigDTO.of(29, 435000L, 3, 1, 3, 100),
            NivelConfigDTO.of(30, 465000L, 3, 1, 3, 100),
            NivelConfigDTO.of(31, 496000L, 3, 1, 3, 120),
            NivelConfigDTO.of(32, 528000L, 3, 1, 3, 120),
            NivelConfigDTO.of(33, 561000L, 3, 1, 3, 120),
            NivelConfigDTO.of(34, 595000L, 3, 1, 3, 120),
            NivelConfigDTO.of(35, 595000L, 3, 1, 3, 120)
        );
    }

    @Override
    public List<LimitadorConfigDTO> getDefaultLimitadores() {
        return List.of(
            LimitadorConfigDTO.of(0,  1,  10),
            LimitadorConfigDTO.of(2,  20, 50),
            LimitadorConfigDTO.of(21, 25, 75),
            LimitadorConfigDTO.of(26, 30, 100),
            LimitadorConfigDTO.of(31, 35, 120)
        );
    }

    @Override
    public List<ClasseConfigDTO> getDefaultClasses() {
        return List.of(
            ClasseConfigDTO.of("Guerreiro",       "Especialista em combate corpo a corpo",   1),
            ClasseConfigDTO.of("Arqueiro",         "Mestre em combate à distância",           2),
            ClasseConfigDTO.of("Monge",            "Lutador desarmado com disciplina espiritual", 3),
            ClasseConfigDTO.of("Berserker",        "Guerreiro selvagem de fúria incontrolável", 4),
            ClasseConfigDTO.of("Assassino",        "Especialista em ataques furtivos e letais", 5),
            ClasseConfigDTO.of("Fauno (Herdeiro)", "Herdeiro com poderes especiais",          6),
            ClasseConfigDTO.of("Mago",             "Conjurador de magias arcanas",            7),
            ClasseConfigDTO.of("Feiticeiro",       "Usuário de magia inata",                  8),
            ClasseConfigDTO.of("Necromante",       "Manipulador de forças da morte",          9),
            ClasseConfigDTO.of("Sacerdote",        "Servo divino com poderes sagrados",       10),
            ClasseConfigDTO.of("Ladrão",           "Especialista em subterfúgio e furto",     11),
            ClasseConfigDTO.of("Negociante",       "Mestre em comércio e persuasão",          12)
        );
    }

    @Override
    public List<RacaConfigDTO> getDefaultRacas() {
        return List.of(
            RacaConfigDTO.of("Humano",    "Raça versátil e adaptável",           1),
            RacaConfigDTO.of("Elfo",      "Seres longevos com afinidade mágica", 2),
            RacaConfigDTO.of("Anão",      "Raça resistente e trabalhadora",      3),
            RacaConfigDTO.of("Meio-Elfo", "Híbrido entre humano e elfo",         4)
        );
    }

    @Override
    public Map<String, List<BonusAtributoDTO>> getDefaultBonusRaciais() {
        return Map.of(
            "Humano",    List.of(),

            "Elfo",      List.of(
                BonusAtributoDTO.of("AGI", 2),
                BonusAtributoDTO.of("VIG", -1)
            ),

            "Anão",      List.of(
                BonusAtributoDTO.of("VIG", 2),
                BonusAtributoDTO.of("AGI", -1)
            ),

            "Meio-Elfo", List.of(
                BonusAtributoDTO.of("AGI", 1),
                BonusAtributoDTO.of("INT", 1)
            )
        );
    }

    @Override
    public List<ProspeccaoConfigDTO> getDefaultProspeccoes() {
        return List.of(
            ProspeccaoConfigDTO.of("d3",  3,  1),
            ProspeccaoConfigDTO.of("d4",  4,  2),
            ProspeccaoConfigDTO.of("d6",  6,  3),
            ProspeccaoConfigDTO.of("d8",  8,  4),
            ProspeccaoConfigDTO.of("d10", 10, 5),
            ProspeccaoConfigDTO.of("d12", 12, 6)
        );
    }

    @Override
    public List<GeneroConfigDTO> getDefaultGeneros() {
        return List.of(
            GeneroConfigDTO.of("Masculino", 1),
            GeneroConfigDTO.of("Feminino",  2),
            GeneroConfigDTO.of("Outro",     3)
        );
    }

    @Override
    public List<IndoleConfigDTO> getDefaultIndoles() {
        return List.of(
            IndoleConfigDTO.of("Bom",    1),
            IndoleConfigDTO.of("Mau",    2),
            IndoleConfigDTO.of("Neutro", 3)
        );
    }

    @Override
    public List<PresencaConfigDTO> getDefaultPresencas() {
        return List.of(
            PresencaConfigDTO.of("Bom",     1),
            PresencaConfigDTO.of("Leal",    2),
            PresencaConfigDTO.of("Caótico", 3),
            PresencaConfigDTO.of("Neutro",  4)
        );
    }

    @Override
    public List<MembroCorpoConfigDTO> getDefaultMembrosCorpo() {
        return List.of(
            MembroCorpoConfigDTO.of("Cabeça",         new BigDecimal("0.75"), 1),
            MembroCorpoConfigDTO.of("Tronco",         new BigDecimal("0.35"), 2),
            MembroCorpoConfigDTO.of("Braço Direito",  new BigDecimal("0.10"), 3),
            MembroCorpoConfigDTO.of("Braço Esquerdo", new BigDecimal("0.10"), 4),
            MembroCorpoConfigDTO.of("Perna Direita",  new BigDecimal("0.10"), 5),
            MembroCorpoConfigDTO.of("Perna Esquerda", new BigDecimal("0.10"), 6),
            MembroCorpoConfigDTO.of("Sangue",         new BigDecimal("1.00"), 9)
        );
    }

    @Override
    public List<BonusConfigDTO> getDefaultBonus() {
        return List.of(
            BonusConfigDTO.of("B.B.A",      "BBA", "(FOR + AGI) / 3", 1),
            BonusConfigDTO.of("B.B.M",      "BBM", "(SAB + INT) / 3", 2),
            BonusConfigDTO.of("Defesa",     "DEF", "VIG / 5",         3),
            BonusConfigDTO.of("Esquiva",    "ESQ", "AGI / 5",         4),
            BonusConfigDTO.of("Iniciativa", "INI", "INTU / 5",        5),
            BonusConfigDTO.of("Percepção",  "PER", "INTU / 3",        6),
            BonusConfigDTO.of("Raciocínio", "RAC", "INT / 3",         7),
            BonusConfigDTO.of("Bloqueio",   "BLO", "VIG / 3",         8),
            BonusConfigDTO.of("Reflexo",    "REF", "AGI / 3",         9)
        );
    }

    @Override
    public List<PontosVantagemConfigDTO> getDefaultPontosVantagem() {
        return List.of(
            PontosVantagemConfigDTO.of(1,  6),
            PontosVantagemConfigDTO.of(5,  3),
            PontosVantagemConfigDTO.of(10, 10),
            PontosVantagemConfigDTO.of(15, 3),
            PontosVantagemConfigDTO.of(20, 10),
            PontosVantagemConfigDTO.of(25, 3),
            PontosVantagemConfigDTO.of(30, 15),
            PontosVantagemConfigDTO.of(35, 3)
        );
    }

    @Override
    public List<CategoriaVantagemDTO> getDefaultCategoriasVantagem() {
        return List.of(
            CategoriaVantagemDTO.of("Treinamento Físico",      "#e74c3c", 1),
            CategoriaVantagemDTO.of("Treinamento Mental",      "#8e44ad", 2),
            CategoriaVantagemDTO.of("Ação",                    "#e67e22", 3),
            CategoriaVantagemDTO.of("Reação",                  "#27ae60", 4),
            CategoriaVantagemDTO.of("Vantagem de Atributo",    "#2980b9", 5),
            CategoriaVantagemDTO.of("Vantagem Geral",          "#95a5a6", 6),
            CategoriaVantagemDTO.of("Vantagem Histórica",      "#f39c12", 7),
            CategoriaVantagemDTO.of("Vantagem de Renascimento","#1abc9c", 8)
        );
    }

    @Override
    public List<VantagemConfigDTO> getDefaultVantagens() {
        return List.of(
            // === Treinamento Físico ===
            VantagemConfigDTO.builder()
                .nome("Treinamento de Combate Ofensivo")
                .descricao("Treinamento especializado em técnicas ofensivas de combate")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(1)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Treinamento de Combate Defensivo")
                .descricao("Treinamento especializado em técnicas defensivas de combate")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(2)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Treinamento de Combate com Escudo")
                .descricao("Treinamento especializado no uso de escudo em combate")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(3)
                .build(),

            // === Treinamento Mental ===
            VantagemConfigDTO.builder()
                .nome("Treinamento Mágico")
                .descricao("Treinamento especializado em técnicas mágicas")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(4)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Treinamento de Poder Mental")
                .descricao("Treinamento especializado em poderes mentais")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(5)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Treinamento de Liderança")
                .descricao("Treinamento especializado em técnicas de liderança e comando")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(6)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Treinamento de Meditação")
                .descricao("Treinamento especializado em técnicas de meditação e foco mental")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(7)
                .build(),

            // === Ação ===
            VantagemConfigDTO.builder()
                .nome("Ataque Adicional")
                .descricao("Permite realizar um ataque adicional por turno")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(4)
                .nivelMinimoPersonagem(5)
                .podeEvoluir(true)
                .nivelMaximoVantagem(3)
                .ordemExibicao(8)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Ataque Sentai")
                .descricao("Permite atacar múltiplos alvos em área")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(5)
                .nivelMinimoPersonagem(10)
                .podeEvoluir(true)
                .nivelMaximoVantagem(5)
                .ordemExibicao(9)
                .build(),

            // === Reação ===
            VantagemConfigDTO.builder()
                .nome("Contra-Ataque")
                .descricao("Permite realizar um contra-ataque ao ser atacado")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(4)
                .nivelMinimoPersonagem(5)
                .podeEvoluir(true)
                .nivelMaximoVantagem(5)
                .ordemExibicao(10)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Interceptação")
                .descricao("Permite interceptar ataques direcionados a aliados")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(4)
                .nivelMinimoPersonagem(5)
                .podeEvoluir(true)
                .nivelMaximoVantagem(5)
                .ordemExibicao(11)
                .build(),

            // === Vantagem de Atributo ===
            VantagemConfigDTO.builder()
                .nome("Corpo Fechado")
                .descricao("Proteção mágica que reduz danos recebidos")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(3)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(12)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Destreza Mental")
                .descricao("Aumenta a capacidade de processamento mental")
                .tipoBonus("ATRIBUTO_INT")
                .valorBonusFormula("nivel_vantagem * 2")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(13)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Destreza Física")
                .descricao("Aumenta a agilidade e reflexos físicos")
                .tipoBonus("ATRIBUTO_AGI")
                .valorBonusFormula("nivel_vantagem * 2")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(14)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Determinação Vital")
                .descricao("Aumenta a resistência física e pontos de vida")
                .tipoBonus("ATRIBUTO_VIG")
                .valorBonusFormula("nivel_vantagem * 2")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(15)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Sexto Sentido")
                .descricao("Percepção aguçada que detecta perigos e anomalias")
                .tipoBonus("ATRIBUTO_INTU")
                .valorBonusFormula("nivel_vantagem * 2")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(16)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Inspiração Natural")
                .descricao("Dom natural que melhora a eficiência em ações")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(17)
                .build(),

            // === Vantagem Geral ===
            VantagemConfigDTO.builder()
                .nome("Saúde de Ferro")
                .descricao("Constituição excepcional que aumenta pontos de vida")
                .tipoBonus("VIDA")
                .valorBonusFormula("nivel_vantagem * 5")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(2)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(18)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Ambidestria")
                .descricao("Remove penalidades de usar armas com a mão inábil")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base")
                .custoBase(4)
                .nivelMinimoPersonagem(5)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .ordemExibicao(19)
                .build(),

            // === Vantagem Histórica ===
            VantagemConfigDTO.builder()
                .nome("Riqueza")
                .descricao("Personagem possui recursos financeiros consideráveis")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(5)
                .ordemExibicao(20)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Capangas")
                .descricao("Personagem possui aliados leais que podem ser convocados")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base * nivel_vantagem")
                .custoBase(3)
                .nivelMinimoPersonagem(5)
                .podeEvoluir(true)
                .nivelMaximoVantagem(5)
                .ordemExibicao(21)
                .build(),

            // === Vantagem de Renascimento ===
            VantagemConfigDTO.builder()
                .nome("Último Sigilo")
                .descricao("Proteção divina que pode prevenir a morte uma vez por sessão")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base")
                .custoBase(10)
                .nivelMinimoPersonagem(31)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .ordemExibicao(22)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Pensamento Bifurcado")
                .descricao("Capacidade de processar duas linhas de pensamento simultaneamente")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base")
                .custoBase(8)
                .nivelMinimoPersonagem(31)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .ordemExibicao(23)
                .build(),

            // === Vantagens legadas ===
            VantagemConfigDTO.builder()
                .nome("Fortitude")
                .descricao("Aumenta o atributo Vigor permanentemente")
                .tipoBonus("ATRIBUTO_VIGOR")
                .valorBonusFormula("nivel_vantagem * 2")
                .custoBase(3)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(24)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Força Aprimorada")
                .descricao("Aumenta o atributo Força permanentemente")
                .tipoBonus("ATRIBUTO_FORCA")
                .valorBonusFormula("nivel_vantagem * 2")
                .custoBase(3)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(25)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Agilidade Aprimorada")
                .descricao("Aumenta o atributo Agilidade permanentemente")
                .tipoBonus("ATRIBUTO_AGILIDADE")
                .valorBonusFormula("nivel_vantagem * 2")
                .custoBase(3)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(10)
                .ordemExibicao(26)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Ataque Aprimorado")
                .descricao("Aumenta BBA (Bônus Base de Ataque) em +1 por nível da vantagem")
                .tipoBonus("BBA")
                .valorBonusFormula("nivel_vantagem * 1")
                .custoBase(2)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(null)
                .ordemExibicao(27)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Defesa Mágica")
                .descricao("Aumenta BBM (Bônus Base Mágico) em +1 por nível da vantagem")
                .tipoBonus("BBM")
                .valorBonusFormula("nivel_vantagem * 1")
                .custoBase(2)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(3)
                .podeEvoluir(true)
                .nivelMaximoVantagem(null)
                .ordemExibicao(28)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Golpe Crítico")
                .descricao("Aumenta chance de crítico em combate")
                .tipoBonus("CRITICO")
                .valorBonusFormula("nivel_vantagem * 5")
                .custoBase(4)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(5)
                .podeEvoluir(true)
                .nivelMaximoVantagem(5)
                .ordemExibicao(29)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Vida Extra")
                .descricao("Aumenta pontos de vida em +5 por nível da vantagem")
                .tipoBonus("VIDA")
                .valorBonusFormula("nivel_vantagem * 5")
                .custoBase(1)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(null)
                .ordemExibicao(30)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Essência Ampliada")
                .descricao("Aumenta pontos de essência/mana em +10 por nível da vantagem")
                .tipoBonus("ESSENCIA")
                .valorBonusFormula("nivel_vantagem * 10")
                .custoBase(2)
                .formulaCusto("custo_base * nivel_vantagem")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(true)
                .nivelMaximoVantagem(null)
                .ordemExibicao(31)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Visão no Escuro")
                .descricao("Permite enxergar no escuro até 18 metros")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base")
                .custoBase(2)
                .nivelMinimoPersonagem(1)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .ordemExibicao(32)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Resistência a Veneno")
                .descricao("Adiciona +4 em testes contra venenos")
                .tipoBonus("ESPECIAL")
                .formulaCusto("custo_base")
                .custoBase(3)
                .nivelMinimoPersonagem(3)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .ordemExibicao(33)
                .build()
        );
    }

    @Override
    public Map<String, List<?>> getDefaultClassePontos() {
        // TODO PA-015-01: Definir com PO os valores canônicos de pontos por classe.
        // Mestre pode configurar manualmente após criar o jogo via ClassePontosConfig endpoints.
        return Map.of();
    }

    @Override
    public Map<String, List<?>> getDefaultRacaPontos() {
        // TODO PA-015-02: Definir com PO os valores canônicos de pontos por raça.
        // Mestre pode configurar manualmente após criar o jogo via RacaPontosConfig endpoints.
        return Map.of();
    }
}
