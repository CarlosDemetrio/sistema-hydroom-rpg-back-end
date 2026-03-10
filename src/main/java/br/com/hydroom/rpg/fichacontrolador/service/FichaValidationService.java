package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaClassePermitidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVantagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsável pelas validações de regras de negócio da Ficha.
 *
 * <p>Validações incluem: pontos de atributo/aptidão, limitador de atributo,
 * classe permitida por raça e pré-requisitos de vantagens.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaValidationService {

    private final VantagemPreRequisitoRepository vantagemPreRequisitoRepository;
    private final FichaVantagemRepository fichaVantagemRepository;
    private final RacaClassePermitidaRepository racaClassePermitidaRepository;

    /**
     * Valida se os pontos de atributo distribuídos não excedem o limite do nível.
     */
    public void validarPontosAtributo(Ficha ficha, List<FichaAtributo> atributos, NivelConfig nivel) {
        if (nivel == null) return;

        int distribuidos = atributos.stream()
                .mapToInt(a -> (a.getBase() != null ? a.getBase() : 0)
                        + (a.getNivel() != null ? a.getNivel() : 0))
                .sum();

        Integer disponíveis = nivel.getPontosAtributo();
        if (disponíveis != null && distribuidos > disponíveis) {
            throw new BusinessException(
                    "Pontos de atributo excedidos: distribuídos " + distribuidos
                            + ", disponíveis " + disponíveis);
        }
    }

    /**
     * Valida se nenhum atributo excede o limitador do nível.
     */
    public void validarLimitadorAtributo(List<FichaAtributo> atributos, NivelConfig nivel) {
        if (nivel == null || nivel.getLimitadorAtributo() == null) return;

        int limitador = nivel.getLimitadorAtributo();

        List<String> excedidos = atributos.stream()
                .filter(a -> {
                    int valor = (a.getBase() != null ? a.getBase() : 0)
                            + (a.getNivel() != null ? a.getNivel() : 0);
                    return valor > limitador;
                })
                .map(a -> a.getAtributoConfig() != null ? a.getAtributoConfig().getNome() : "Desconhecido")
                .toList();

        if (!excedidos.isEmpty()) {
            throw new BusinessException(
                    "Atributos acima do limitador (" + limitador + "): "
                            + String.join(", ", excedidos));
        }
    }

    /**
     * Valida se os pontos de aptidão distribuídos não excedem o limite do nível.
     */
    public void validarPontosAptidao(Ficha ficha, List<FichaAptidao> aptidoes, NivelConfig nivel) {
        if (nivel == null) return;

        int distribuidos = aptidoes.stream()
                .mapToInt(a -> a.getBase() != null ? a.getBase() : 0)
                .sum();

        Integer disponíveis = nivel.getPontosAptidao();
        if (disponíveis != null && distribuidos > disponíveis) {
            throw new BusinessException(
                    "Pontos de aptidão excedidos: distribuídos " + distribuidos
                            + ", disponíveis " + disponíveis);
        }
    }

    /**
     * Valida se a classe escolhida é permitida para a raça do personagem.
     * Se raça ou classe for null, não valida. Se não houver restrições configuradas para
     * a raça (lista vazia), todas as classes são permitidas.
     */
    public void validarClassePermitidaPorRaca(Ficha ficha) {
        if (ficha.getRaca() == null || ficha.getClasse() == null) return;

        Long racaId = ficha.getRaca().getId();
        Long classeId = ficha.getClasse().getId();

        List<?> restricoes = racaClassePermitidaRepository.findByRacaId(racaId);

        // Se não há restrições configuradas, todas as classes são permitidas
        if (restricoes.isEmpty()) return;

        boolean permitida = racaClassePermitidaRepository.existsByRacaIdAndClasseId(racaId, classeId);
        if (!permitida) {
            throw new BusinessException(
                    "Classe '" + ficha.getClasse().getNome()
                            + "' não é permitida para a raça '" + ficha.getRaca().getNome() + "'");
        }
    }

    /**
     * Valida se os pré-requisitos de todas as vantagens da ficha estão satisfeitos.
     */
    public void validarPreRequisitosVantagens(Ficha ficha, List<FichaVantagem> vantagens) {
        if (vantagens.isEmpty()) return;

        // Mapear vantagens da ficha: vantagemConfigId -> nivelAtual
        Map<Long, Integer> vantagensDaFicha = vantagens.stream()
                .filter(v -> v.getVantagemConfig() != null)
                .collect(Collectors.toMap(
                        v -> v.getVantagemConfig().getId(),
                        v -> v.getNivelAtual() != null ? v.getNivelAtual() : 1
                ));

        for (FichaVantagem fichaVantagem : vantagens) {
            if (fichaVantagem.getVantagemConfig() == null) continue;

            Long vantagemId = fichaVantagem.getVantagemConfig().getId();
            List<VantagemPreRequisito> preRequisitos = vantagemPreRequisitoRepository.findByVantagemId(vantagemId);

            for (VantagemPreRequisito preRequisito : preRequisitos) {
                if (preRequisito.getRequisito() == null) continue;

                Long requisitoId = preRequisito.getRequisito().getId();
                int nivelMinimo = preRequisito.getNivelMinimo() != null ? preRequisito.getNivelMinimo() : 1;

                Integer nivelFicha = vantagensDaFicha.get(requisitoId);
                if (nivelFicha == null || nivelFicha < nivelMinimo) {
                    String nomeVantagem = fichaVantagem.getVantagemConfig().getNome();
                    String nomeRequisito = preRequisito.getRequisito().getNome();
                    throw new BusinessException(
                            "Vantagem '" + nomeVantagem + "' requer '"
                                    + nomeRequisito + "' no nível mínimo " + nivelMinimo);
                }
            }
        }
    }

    /**
     * Executa todas as validações de negócio da ficha.
     * Tolerante a dados faltando (null-safe).
     */
    public void validarTudo(
            Ficha ficha,
            List<FichaAtributo> atributos,
            List<FichaAptidao> aptidoes,
            List<FichaVantagem> vantagens,
            NivelConfig nivel) {

        validarPontosAtributo(ficha, atributos, nivel);
        validarLimitadorAtributo(atributos, nivel);
        validarPontosAptidao(ficha, aptidoes, nivel);
        validarClassePermitidaPorRaca(ficha);
        validarPreRequisitosVantagens(ficha, vantagens);
    }
}
