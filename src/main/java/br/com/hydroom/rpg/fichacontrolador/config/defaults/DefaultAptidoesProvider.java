package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.AptidaoConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAptidoesProvider {

    public List<AptidaoConfigDTO> get() {
        return List.of(
            // === Aptidoes Fisicas (12) ===
            AptidaoConfigDTO.of("Acrobacia",              "FISICA", "Execucao de manobras ageis como saltos, rolamentos e equilibrio em terreno dificil", 1),
            AptidaoConfigDTO.of("Guarda",                 "FISICA", "Tecnica defensiva de posicionar corpo e armas para absorver ou redirecionar impactos", 2),
            AptidaoConfigDTO.of("Aparar",                 "FISICA", "Desviar ou neutralizar ataques com arma ou escudo com precisao de timing", 3),
            AptidaoConfigDTO.of("Atletismo",              "FISICA", "Forca fisica bruta aplicada a escalada, arremesso, luta corporal e resistencia de esforco", 4),
            AptidaoConfigDTO.of("Resvalar",               "FISICA", "Tecnica de esquiva dinamica que usa o movimento do corpo para evitar golpes e projeteis", 5),
            AptidaoConfigDTO.of("Resistencia",            "FISICA", "Capacidade de suportar condicoes extremas: fome, veneno, dor, temperatura e exaustao", 6),
            AptidaoConfigDTO.of("Perseguicao",            "FISICA", "Habilidade de rastrear e perseguir alvos em movimento, ou de fugir eficientemente", 7),
            AptidaoConfigDTO.of("Natacao",                "FISICA", "Habilidade de nadar e se mover em ambientes aquaticos, incluindo mergulho e combate na agua", 8),
            AptidaoConfigDTO.of("Furtividade",            "FISICA", "Habilidade de mover-se silenciosamente, esconder-se e realizar acoes sem ser detectado", 9),
            AptidaoConfigDTO.of("Prestidigitacao",        "FISICA", "Destreza manual para realizar truques, pickpocket, esconder objetos e manipulacao fina", 10),
            AptidaoConfigDTO.of("Conduzir",               "FISICA", "Habilidade de montar animais ou pilotar veiculos, incluindo manobras em alta velocidade", 11),
            AptidaoConfigDTO.of("Arte da Fuga",           "FISICA", "Habilidade de escapar de amarras, algemas, prisoes e outras situacoes de captura", 12),

            // === Aptidoes Mentais (12) ===
            AptidaoConfigDTO.of("Idiomas",                "MENTAL", "Conhecimento de idiomas estrangeiros, dialetos e sistemas de escrita do mundo de Klayrah", 13),
            AptidaoConfigDTO.of("Observacao",             "MENTAL", "Percepcao agucada para notar detalhes, pistas ocultas e anomalias no ambiente", 14),
            AptidaoConfigDTO.of("Falsificar",             "MENTAL", "Habilidade de criar documentos falsos, imitar assinaturas e forjar selos oficiais", 15),
            AptidaoConfigDTO.of("Prontidao",              "MENTAL", "Estado de alerta elevado; evita ser surpreendido e age rapidamente em situacoes de crise", 16),
            AptidaoConfigDTO.of("Auto Controle",          "MENTAL", "Dominio das emocoes e resistencia a manipulacao, medo, seducao e pressao psicologica", 17),
            AptidaoConfigDTO.of("Sentir Motivacao",       "MENTAL", "Habilidade de perceber as verdadeiras intencoes e emocoes ocultas de outras pessoas", 18),
            AptidaoConfigDTO.of("Sobrevivencia",          "MENTAL", "Conhecimento de orientacao, caca, armadilhas e sobrevivencia em ambientes hostis", 19),
            AptidaoConfigDTO.of("Investigar",             "MENTAL", "Habilidade de reunir pistas, interrogar testemunhas e deduzir conclusoes a partir de evidencias", 20),
            AptidaoConfigDTO.of("Blefar",                 "MENTAL", "Capacidade de mentir convincentemente, criar desvios e manipular a percepcao alheia", 21),
            AptidaoConfigDTO.of("Atuacao",                "MENTAL", "Habilidade de interpretar personagens, disfarcar-se e convencer por meio de performance", 22),
            AptidaoConfigDTO.of("Diplomacia",             "MENTAL", "Arte da negociacao, persuasao e mediacao de conflitos com argumentos e charme", 23),
            AptidaoConfigDTO.of("Operacao de Mecanismos", "MENTAL", "Habilidade de operar, reparar e arrombar mecanismos, fechaduras e engenhocas", 24)
        );
    }
}
