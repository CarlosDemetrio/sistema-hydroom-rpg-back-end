package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.model.ClasseVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVantagem;
import br.com.hydroom.rpg.fichacontrolador.model.RacaVantagemPreDefinida;
import br.com.hydroom.rpg.fichacontrolador.model.VantagemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.enums.OrigemVantagem;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseVantagemPreDefinidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVantagemRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaVantagemPreDefinidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por conceder automaticamente vantagens pré-definidas de classe e raça.
 *
 * <p>Triggered em dois momentos:</p>
 * <ul>
 *   <li>Criação da ficha (nível 1)</li>
 *   <li>Level up (para cada nível atingido, inclusive pulos de nível)</li>
 * </ul>
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>RN-015-03: Não duplicar — se a FichaVantagem já existe (qualquer origem), ignorar.</li>
 *   <li>RN-015-04: custoPago = 0 para vantagens auto-concedidas.</li>
 *   <li>RN-015-05: origem = SISTEMA distingue de vantagens compradas ou concedidas pelo Mestre.</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VantagemAutoConcessaoService {

    private final ClasseVantagemPreDefinidaRepository classeVantagemRepo;
    private final RacaVantagemPreDefinidaRepository racaVantagemRepo;
    private final FichaVantagemRepository fichaVantagemRepository;

    /**
     * Concede automaticamente as vantagens pré-definidas para um nível específico.
     * Verifica tanto ClasseVantagemPreDefinida quanto RacaVantagemPreDefinida.
     * Não duplica: se a FichaVantagem já existe, ignora.
     *
     * @param ficha ficha do personagem
     * @param nivel nível a verificar (ex: 1 para criação, novo nível para level up)
     * @return lista de FichaVantagem auto-criadas (pode ser vazia)
     */
    @Transactional
    public List<FichaVantagem> concederVantagensParaNivel(Ficha ficha, int nivel) {
        List<FichaVantagem> concedidas = new ArrayList<>();

        if (ficha.getClasse() != null) {
            List<ClasseVantagemPreDefinida> classeVantagens =
                    classeVantagemRepo.findByClasseIdAndNivelWithVantagem(
                            ficha.getClasse().getId(), nivel);

            for (ClasseVantagemPreDefinida cvp : classeVantagens) {
                FichaVantagem criada = concederSeNaoExiste(ficha, cvp.getVantagemConfig());
                if (criada != null) {
                    concedidas.add(criada);
                    log.info("Auto-concedida vantagem '{}' (classe '{}', nivel {}) para ficha {}",
                            cvp.getVantagemConfig().getNome(),
                            ficha.getClasse().getNome(),
                            nivel, ficha.getId());
                }
            }
        }

        if (ficha.getRaca() != null) {
            List<RacaVantagemPreDefinida> racaVantagens =
                    racaVantagemRepo.findByRacaIdAndNivelWithVantagem(
                            ficha.getRaca().getId(), nivel);

            for (RacaVantagemPreDefinida rvp : racaVantagens) {
                FichaVantagem criada = concederSeNaoExiste(ficha, rvp.getVantagemConfig());
                if (criada != null) {
                    concedidas.add(criada);
                    log.info("Auto-concedida vantagem '{}' (raca '{}', nivel {}) para ficha {}",
                            rvp.getVantagemConfig().getNome(),
                            ficha.getRaca().getNome(),
                            nivel, ficha.getId());
                }
            }
        }

        return concedidas;
    }

    /**
     * Concede a vantagem se a ficha ainda não a possui.
     * Cria FichaVantagem com custoPago=0 e origem=SISTEMA.
     *
     * @return FichaVantagem criada, ou null se já existia
     */
    private FichaVantagem concederSeNaoExiste(Ficha ficha, VantagemConfig vantagemConfig) {
        if (fichaVantagemRepository.existsByFichaIdAndVantagemConfigId(
                ficha.getId(), vantagemConfig.getId())) {
            log.debug("Vantagem '{}' ja existe na ficha {} — ignorando auto-concessao",
                    vantagemConfig.getNome(), ficha.getId());
            return null;
        }

        FichaVantagem fichaVantagem = FichaVantagem.builder()
                .ficha(ficha)
                .vantagemConfig(vantagemConfig)
                .nivelAtual(1)
                .custoPago(0)
                .origem(OrigemVantagem.SISTEMA)
                .build();

        return fichaVantagemRepository.save(fichaVantagem);
    }
}
