package br.com.hydroom.rpg.fichacontrolador.config;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Implementação padrão do {@link GameDefaultConfigProvider}.
 *
 * <p>Fornece todos os dados default para inicialização de um jogo novo.</p>
 * <p>Valores são baseados nas seeds SQL originais, mas agora gerenciados via código.</p>
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
                "min(total / 20, 3)",  // exp4j formato (lowercase)
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
            AptidaoConfigDTO.of("Acrobacia", "FISICA", "Habilidade de realizar manobras acrobáticas", 1),
            AptidaoConfigDTO.of("Guarda", "FISICA", "Capacidade de defesa e bloqueio", 2),
            AptidaoConfigDTO.of("Aparar", "FISICA", "Habilidade de desviar ataques", 3),
            AptidaoConfigDTO.of("Atletismo", "FISICA", "Força física e condicionamento", 4),
            AptidaoConfigDTO.of("Resvalar", "FISICA", "Capacidade de esquiva e movimento ágil", 5),
            AptidaoConfigDTO.of("Resistência", "FISICA", "Capacidade de resistir a condições adversas", 6),
            AptidaoConfigDTO.of("Perseguição", "FISICA", "Habilidade de perseguir ou fugir", 7),
            AptidaoConfigDTO.of("Natação", "FISICA", "Capacidade de nadar e manobras aquáticas", 8),
            AptidaoConfigDTO.of("Furtividade", "FISICA", "Capacidade de se mover sem ser detectado", 9),
            AptidaoConfigDTO.of("Prestidigitação", "FISICA", "Destreza manual e truques", 10),
            AptidaoConfigDTO.of("Conduzir", "FISICA", "Habilidade de pilotar veículos e montarias", 11),
            AptidaoConfigDTO.of("Arte da Fuga", "FISICA", "Capacidade de escapar de restrições", 12),

            // === Aptidões Mentais (12) ===
            AptidaoConfigDTO.of("Idiomas", "MENTAL", "Conhecimento de línguas", 13),
            AptidaoConfigDTO.of("Observação", "MENTAL", "Capacidade de notar detalhes", 14),
            AptidaoConfigDTO.of("Falsificar", "MENTAL", "Habilidade de criar falsificações", 15),
            AptidaoConfigDTO.of("Prontidão", "MENTAL", "Capacidade de reagir rapidamente", 16),
            AptidaoConfigDTO.of("Auto Controle", "MENTAL", "Controle emocional e mental", 17),
            AptidaoConfigDTO.of("Sentir Motivação", "MENTAL", "Capacidade de ler intenções", 18),
            AptidaoConfigDTO.of("Sobrevivência", "MENTAL", "Conhecimento de técnicas de sobrevivência", 19),
            AptidaoConfigDTO.of("Investigar", "MENTAL", "Habilidade de coletar e analisar informações", 20),
            AptidaoConfigDTO.of("Blefar", "MENTAL", "Capacidade de enganar e mentir", 21),
            AptidaoConfigDTO.of("Atuação", "MENTAL", "Habilidade de interpretar personagens", 22),
            AptidaoConfigDTO.of("Diplomacia", "MENTAL", "Capacidade de negociação e persuasão", 23),
            AptidaoConfigDTO.of("Operação de Mecanismos", "MENTAL", "Conhecimento de dispositivos mecânicos", 24)
        );
    }

    @Override
    public List<NivelConfigDTO> getDefaultNiveis() {
        // Nível 0 até 35 (36 níveis total)
        return List.of(
            NivelConfigDTO.of(0, 0L, 0, 0, 0),
            NivelConfigDTO.of(1, 1000L, 3, 1, 3),
            NivelConfigDTO.of(2, 3000L, 3, 1, 3),
            NivelConfigDTO.of(3, 6000L, 3, 1, 3),
            NivelConfigDTO.of(4, 10000L, 3, 1, 3),
            NivelConfigDTO.of(5, 15000L, 3, 1, 3),
            NivelConfigDTO.of(6, 21000L, 3, 1, 3),
            NivelConfigDTO.of(7, 28000L, 3, 1, 3),
            NivelConfigDTO.of(8, 36000L, 3, 1, 3),
            NivelConfigDTO.of(9, 45000L, 3, 1, 3),
            NivelConfigDTO.of(10, 55000L, 3, 1, 3),
            NivelConfigDTO.of(11, 66000L, 3, 1, 3),
            NivelConfigDTO.of(12, 78000L, 3, 1, 3),
            NivelConfigDTO.of(13, 91000L, 3, 1, 3),
            NivelConfigDTO.of(14, 105000L, 3, 1, 3),
            NivelConfigDTO.of(15, 120000L, 3, 1, 3),
            NivelConfigDTO.of(16, 136000L, 3, 1, 3),
            NivelConfigDTO.of(17, 153000L, 3, 1, 3),
            NivelConfigDTO.of(18, 171000L, 3, 1, 3),
            NivelConfigDTO.of(19, 190000L, 3, 1, 3),
            NivelConfigDTO.of(20, 210000L, 3, 1, 3),
            NivelConfigDTO.of(21, 231000L, 3, 1, 3),
            NivelConfigDTO.of(22, 253000L, 3, 1, 3),
            NivelConfigDTO.of(23, 276000L, 3, 1, 3),
            NivelConfigDTO.of(24, 300000L, 3, 1, 3),
            NivelConfigDTO.of(25, 325000L, 3, 1, 3),
            NivelConfigDTO.of(26, 351000L, 3, 1, 3),
            NivelConfigDTO.of(27, 378000L, 3, 1, 3),
            NivelConfigDTO.of(28, 406000L, 3, 1, 3),
            NivelConfigDTO.of(29, 435000L, 3, 1, 3),
            NivelConfigDTO.of(30, 465000L, 3, 1, 3),
            NivelConfigDTO.of(31, 496000L, 3, 1, 3),
            NivelConfigDTO.of(32, 528000L, 3, 1, 3),
            NivelConfigDTO.of(33, 561000L, 3, 1, 3),
            NivelConfigDTO.of(34, 595000L, 3, 1, 3),
            NivelConfigDTO.of(35, 595000L, 3, 1, 3)
        );
    }

    @Override
    public List<LimitadorConfigDTO> getDefaultLimitadores() {
        return List.of(
            LimitadorConfigDTO.of(0, 1, 10),
            LimitadorConfigDTO.of(2, 20, 50),
            LimitadorConfigDTO.of(21, 25, 75),
            LimitadorConfigDTO.of(26, 30, 100),
            LimitadorConfigDTO.of(31, 35, 120)
        );
    }

    @Override
    public List<ClasseConfigDTO> getDefaultClasses() {
        return List.of(
            ClasseConfigDTO.of("Guerreiro", "Especialista em combate corpo a corpo", 1),
            ClasseConfigDTO.of("Arqueiro", "Mestre em combate à distância", 2),
            ClasseConfigDTO.of("Monge", "Lutador desarmado com disciplina espiritual", 3),
            ClasseConfigDTO.of("Berserker", "Guerreiro selvagem de fúria incontrolável", 4),
            ClasseConfigDTO.of("Assassino", "Especialista em ataques furtivos e letais", 5),
            ClasseConfigDTO.of("Fauno (Herdeiro)", "Herdeiro com poderes especiais", 6),
            ClasseConfigDTO.of("Mago", "Conjurador de magias arcanas", 7),
            ClasseConfigDTO.of("Feiticeiro", "Usuário de magia inata", 8),
            ClasseConfigDTO.of("Necromance", "Manipulador de forças da morte", 9),
            ClasseConfigDTO.of("Sacerdote", "Servo divino com poderes sagrados", 10),
            ClasseConfigDTO.of("Ladrão", "Especialista em subterfúgio e furto", 11),
            ClasseConfigDTO.of("Negociante", "Mestre em comércio e persuasão", 12)
        );
    }

    @Override
    public List<RacaConfigDTO> getDefaultRacas() {
        return List.of(
            RacaConfigDTO.of("Humano", "Raça versátil e adaptável", 1),
            RacaConfigDTO.of("Elfo", "Seres longevos com afinidade mágica", 2),
            RacaConfigDTO.of("Anão", "Raça resistente e trabalhadora", 3),
            RacaConfigDTO.of("Meio-Elfo", "Híbrido entre humano e elfo", 4)
        );
    }

    @Override
    public Map<String, List<BonusAtributoDTO>> getDefaultBonusRaciais() {
        return Map.of(
            "Humano", List.of(),  // Sem bônus (versatilidade)

            "Elfo", List.of(
                BonusAtributoDTO.of("AGI", 2),
                BonusAtributoDTO.of("VIG", -1)
            ),

            "Anão", List.of(
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
            ProspeccaoConfigDTO.of("d3", 3, 1),
            ProspeccaoConfigDTO.of("d4", 4, 2),
            ProspeccaoConfigDTO.of("d6", 6, 3),
            ProspeccaoConfigDTO.of("d8", 8, 4),
            ProspeccaoConfigDTO.of("d10", 10, 5),
            ProspeccaoConfigDTO.of("d12", 12, 6)
        );
    }

    @Override
    public List<GeneroConfigDTO> getDefaultGeneros() {
        return List.of(
            GeneroConfigDTO.of("Masculino", 1),
            GeneroConfigDTO.of("Feminino", 2),
            GeneroConfigDTO.of("Não-Binário", 3),
            GeneroConfigDTO.of("Prefiro não informar", 4)
        );
    }

    @Override
    public List<IndoleConfigDTO> getDefaultIndoles() {
        return List.of(
            IndoleConfigDTO.of("Ordeiro Bondoso", 1),
            IndoleConfigDTO.of("Neutro Bondoso", 2),
            IndoleConfigDTO.of("Caótico Bondoso", 3),
            IndoleConfigDTO.of("Ordeiro Neutro", 4),
            IndoleConfigDTO.of("Neutro", 5),
            IndoleConfigDTO.of("Caótico Neutro", 6),
            IndoleConfigDTO.of("Ordeiro Maligno", 7),
            IndoleConfigDTO.of("Neutro Maligno", 8),
            IndoleConfigDTO.of("Caótico Maligno", 9)
        );
    }

    @Override
    public List<PresencaConfigDTO> getDefaultPresencas() {
        return List.of(
            PresencaConfigDTO.of("Insignificante", 1),
            PresencaConfigDTO.of("Fraco", 2),
            PresencaConfigDTO.of("Normal", 3),
            PresencaConfigDTO.of("Notável", 4),
            PresencaConfigDTO.of("Impressionante", 5),
            PresencaConfigDTO.of("Dominante", 6)
        );
    }

    @Override
    public List<MembroCorpoConfigDTO> getDefaultMembrosCorpo() {
        return List.of(
            MembroCorpoConfigDTO.of("Cabeça", 1),
            MembroCorpoConfigDTO.of("Tronco", 2),
            MembroCorpoConfigDTO.of("Braço Direito", 3),
            MembroCorpoConfigDTO.of("Braço Esquerdo", 4),
            MembroCorpoConfigDTO.of("Perna Direita", 5),
            MembroCorpoConfigDTO.of("Perna Esquerda", 6)
        );
    }

    @Override
    public List<VantagemConfigDTO> getDefaultVantagens() {
        return List.of(
            // === Vantagens de Atributos ===
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
                .build(),

            // === Vantagens de Combate ===
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
                .build(),

            // === Vantagens de Vida/Essência ===
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
                .build(),

            // === Vantagens Especiais (não evoluem) ===
            VantagemConfigDTO.builder()
                .nome("Visão no Escuro")
                .descricao("Permite enxergar no escuro até 18 metros")
                .tipoBonus("ESPECIAL")
                .valorBonusFormula(null)
                .custoBase(2)
                .formulaCusto("custo_base")
                .nivelMinimoPersonagem(1)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Resistência a Veneno")
                .descricao("Adiciona +4 em testes contra venenos")
                .tipoBonus("ESPECIAL")
                .valorBonusFormula(null)
                .custoBase(3)
                .formulaCusto("custo_base")
                .nivelMinimoPersonagem(3)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .build(),

            VantagemConfigDTO.builder()
                .nome("Ambidestria")
                .descricao("Remove penalidades de usar armas com a mão inábil")
                .tipoBonus("ESPECIAL")
                .valorBonusFormula(null)
                .custoBase(4)
                .formulaCusto("custo_base")
                .nivelMinimoPersonagem(5)
                .podeEvoluir(false)
                .nivelMaximoVantagem(1)
                .build()
        );
    }
}
