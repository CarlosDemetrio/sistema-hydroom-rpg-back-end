package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ReordenarItemRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.*;
import br.com.hydroom.rpg.fichacontrolador.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para reordenação em batch de configurações do jogo.
 * Atualiza o campo ordemExibicao de múltiplos itens de uma vez.
 */
@Service
@RequiredArgsConstructor
public class ReordenacaoService {

    private final ConfiguracaoAtributoRepository atributoRepository;
    private final ConfiguracaoAptidaoRepository aptidaoRepository;
    private final BonusConfigRepository bonusRepository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final DadoProspeccaoConfigRepository dadoProspeccaoRepository;
    private final GeneroConfigRepository generoRepository;
    private final IndoleConfigRepository indoleRepository;
    private final MembroCorpoConfigRepository membroCorpoRepository;
    private final ConfiguracaoNivelRepository nivelRepository;
    private final PresencaConfigRepository presencaRepository;
    private final ConfiguracaoRacaRepository racaRepository;
    private final TipoAptidaoRepository tipoAptidaoRepository;
    private final VantagemConfigRepository vantagemRepository;

    @Transactional
    public void reordenarAtributos(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            AtributoConfig entity = atributoRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Atributo não encontrado: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Atributo");
            entity.setOrdemExibicao(item.ordemExibicao());
            atributoRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarAptidoes(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            AptidaoConfig entity = aptidaoRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Aptidão não encontrada: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Aptidão");
            entity.setOrdemExibicao(item.ordemExibicao());
            aptidaoRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarBonus(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            BonusConfig entity = bonusRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Bônus não encontrado: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Bônus");
            entity.setOrdemExibicao(item.ordemExibicao());
            bonusRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarClasses(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            ClassePersonagem entity = classeRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Classe");
            entity.setOrdemExibicao(item.ordemExibicao());
            classeRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarDadosProspeccao(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            DadoProspeccaoConfig entity = dadoProspeccaoRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Dado de prospecção não encontrado: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Dado de prospecção");
            entity.setOrdemExibicao(item.ordemExibicao());
            dadoProspeccaoRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarGeneros(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            GeneroConfig entity = generoRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Gênero não encontrado: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Gênero");
            entity.setOrdemExibicao(item.ordemExibicao());
            generoRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarIndoles(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            IndoleConfig entity = indoleRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Índole não encontrada: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Índole");
            entity.setOrdemExibicao(item.ordemExibicao());
            indoleRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarMembrosCorpo(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            MembroCorpoConfig entity = membroCorpoRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Membro do corpo não encontrado: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Membro do corpo");
            entity.setOrdemExibicao(item.ordemExibicao());
            membroCorpoRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarPresencas(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            PresencaConfig entity = presencaRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Presença não encontrada: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Presença");
            entity.setOrdemExibicao(item.ordemExibicao());
            presencaRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarRacas(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            Raca entity = racaRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Raça");
            entity.setOrdemExibicao(item.ordemExibicao());
            racaRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarTiposAptidao(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            TipoAptidao entity = tipoAptidaoRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de aptidão não encontrado: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Tipo de aptidão");
            entity.setOrdemExibicao(item.ordemExibicao());
            tipoAptidaoRepository.save(entity);
        }
    }

    @Transactional
    public void reordenarVantagens(Long jogoId, List<ReordenarItemRequest> itens) {
        for (ReordenarItemRequest item : itens) {
            VantagemConfig entity = vantagemRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Vantagem não encontrada: " + item.id()));
            verificarJogo(entity.getJogo().getId(), jogoId, "Vantagem");
            entity.setOrdemExibicao(item.ordemExibicao());
            vantagemRepository.save(entity);
        }
    }

    private void verificarJogo(Long entityJogoId, Long jogoId, String tipo) {
        if (!entityJogoId.equals(jogoId)) {
            throw new ForbiddenException(tipo + " não pertence ao jogo informado.");
        }
    }
}
