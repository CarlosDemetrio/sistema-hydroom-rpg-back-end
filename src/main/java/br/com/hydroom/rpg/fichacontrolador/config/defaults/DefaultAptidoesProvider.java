package br.com.hydroom.rpg.fichacontrolador.config.defaults;

import br.com.hydroom.rpg.fichacontrolador.dto.defaults.AptidaoConfigDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAptidoesProvider {

    public List<AptidaoConfigDTO> get() {
        return List.of(
            // === Aptidoes Fisicas (12) ===
            AptidaoConfigDTO.of("Acrobacia",              "FISICA", "Execu\u00e7\u00e3o de manobras \u00e1geis como saltos, rolamentos e equil\u00edbrio em terreno dif\u00edcil", 1),
            AptidaoConfigDTO.of("Guarda",                 "FISICA", "T\u00e9cnica defensiva de posicionar corpo e armas para absorver ou redirecionar impactos", 2),
            AptidaoConfigDTO.of("Aparar",                 "FISICA", "Desviar ou neutralizar ataques com arma ou escudo com precis\u00e3o de timing", 3),
            AptidaoConfigDTO.of("Atletismo",              "FISICA", "For\u00e7a f\u00edsica bruta aplicada a escalada, arremesso, luta corporal e resist\u00eancia de esfor\u00e7o", 4),
            AptidaoConfigDTO.of("Resvalar",               "FISICA", "T\u00e9cnica de esquiva din\u00e2mica que usa o movimento do corpo para evitar golpes e proj\u00e9teis", 5),
            AptidaoConfigDTO.of("Resist\u00eancia",            "FISICA", "Capacidade de suportar condi\u00e7\u00f5es extremas: fome, veneno, dor, temperatura e exaust\u00e3o", 6),
            AptidaoConfigDTO.of("Persegui\u00e7\u00e3o",            "FISICA", "Habilidade de rastrear e perseguir alvos em movimento, ou de fugir eficientemente", 7),
            AptidaoConfigDTO.of("Nata\u00e7\u00e3o",                "FISICA", "Habilidade de nadar e se mover em ambientes aqu\u00e1ticos, incluindo mergulho e combate na \u00e1gua", 8),
            AptidaoConfigDTO.of("Furtividade",            "FISICA", "Habilidade de mover-se silenciosamente, esconder-se e realizar a\u00e7\u00f5es sem ser detectado", 9),
            AptidaoConfigDTO.of("Prestidigita\u00e7\u00e3o",        "FISICA", "Destreza manual para realizar truques, pickpocket, esconder objetos e manipula\u00e7\u00e3o fina", 10),
            AptidaoConfigDTO.of("Conduzir",               "FISICA", "Habilidade de montar animais ou pilotar ve\u00edculos, incluindo manobras em alta velocidade", 11),
            AptidaoConfigDTO.of("Arte da Fuga",           "FISICA", "Habilidade de escapar de amarras, algemas, pris\u00f5es e outras situa\u00e7\u00f5es de captura", 12),

            // === Aptid\u00f5es Mentais (12) ===
            AptidaoConfigDTO.of("Idiomas",                "MENTAL", "Conhecimento de idiomas estrangeiros, dialetos e sistemas de escrita do mundo de Klayrah", 13),
            AptidaoConfigDTO.of("Observa\u00e7\u00e3o",             "MENTAL", "Percep\u00e7\u00e3o agu\u00e7ada para notar detalhes, pistas ocultas e anomalias no ambiente", 14),
            AptidaoConfigDTO.of("Falsificar",             "MENTAL", "Habilidade de criar documentos falsos, imitar assinaturas e forjar selos oficiais", 15),
            AptidaoConfigDTO.of("Prontid\u00e3o",              "MENTAL", "Estado de alerta elevado; evita ser surpreendido e age rapidamente em situa\u00e7\u00f5es de crise", 16),
            AptidaoConfigDTO.of("Auto Controle",          "MENTAL", "Dom\u00ednio das emo\u00e7\u00f5es e resist\u00eancia \u00e0 manipula\u00e7\u00e3o, medo, sedu\u00e7\u00e3o e press\u00e3o psicol\u00f3gica", 17),
            AptidaoConfigDTO.of("Sentir Motiva\u00e7\u00e3o",       "MENTAL", "Habilidade de perceber as verdadeiras inten\u00e7\u00f5es e emo\u00e7\u00f5es ocultas de outras pessoas", 18),
            AptidaoConfigDTO.of("Sobreviv\u00eancia",          "MENTAL", "Conhecimento de orienta\u00e7\u00e3o, ca\u00e7a, armadilhas e sobreviv\u00eancia em ambientes hostis", 19),
            AptidaoConfigDTO.of("Investigar",             "MENTAL", "Habilidade de reunir pistas, interrogar testemunhas e deduzir conclus\u00f5es a partir de evid\u00eancias", 20),
            AptidaoConfigDTO.of("Blefar",                 "MENTAL", "Capacidade de mentir convincentemente, criar desvios e manipular a percep\u00e7\u00e3o alheia", 21),
            AptidaoConfigDTO.of("Atua\u00e7\u00e3o",                "MENTAL", "Habilidade de interpretar personagens, disfar\u00e7ar-se e convencer por meio de performance", 22),
            AptidaoConfigDTO.of("Diplomacia",             "MENTAL", "Arte da negocia\u00e7\u00e3o, persuas\u00e3o e media\u00e7\u00e3o de conflitos com argumentos e charme", 23),
            AptidaoConfigDTO.of("Opera\u00e7\u00e3o de Mecanismos", "MENTAL", "Habilidade de operar, reparar e arrombar mecanismos, fechaduras e engenhocas", 24)
        );
    }
}
