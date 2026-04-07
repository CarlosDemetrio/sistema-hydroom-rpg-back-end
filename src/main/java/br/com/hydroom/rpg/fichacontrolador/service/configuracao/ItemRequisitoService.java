package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ItemRequisitoRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ItemRequisito;
import br.com.hydroom.rpg.fichacontrolador.model.enums.TipoRequisito;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemRequisitoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de requisitos de itens.
 *
 * <p>Sem soft delete — remoção é física via orphanRemoval.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequisitoService {

    private final ItemRequisitoRepository requisitoRepository;
    private final ItemConfigRepository itemConfigRepository;

    /**
     * Lista todos os requisitos de um item.
     */
    public List<ItemRequisito> listarRequisitos(Long itemConfigId) {
        if (!itemConfigRepository.existsById(itemConfigId)) {
            throw new ResourceNotFoundException("ItemConfig", itemConfigId);
        }
        return requisitoRepository.findByItemConfigId(itemConfigId);
    }

    /**
     * Adiciona um novo requisito a um item.
     */
    @Transactional
    public ItemRequisito adicionarRequisito(Long itemConfigId, ItemRequisitoRequest request) {
        ItemConfig item = itemConfigRepository.findById(itemConfigId)
            .orElseThrow(() -> new ResourceNotFoundException("ItemConfig", itemConfigId));

        validarCamposRequisito(request);

        ItemRequisito requisito = ItemRequisito.builder()
            .itemConfig(item)
            .tipo(request.tipo())
            .alvo(request.alvo())
            .valorMinimo(request.valorMinimo())
            .build();

        log.debug("Adicionando requisito {} ao item ID: {}", request.tipo(), itemConfigId);
        return requisitoRepository.save(requisito);
    }

    /**
     * Remove fisicamente um requisito de um item (sem soft delete).
     */
    @Transactional
    public void removerRequisito(Long itemConfigId, Long requisitoId) {
        ItemRequisito requisito = requisitoRepository.findById(requisitoId)
            .orElseThrow(() -> new ResourceNotFoundException("ItemRequisito", requisitoId));

        if (!requisito.getItemConfig().getId().equals(itemConfigId)) {
            throw new ValidationException("O requisito não pertence ao item informado");
        }

        requisitoRepository.delete(requisito);
        log.debug("Requisito ID: {} removido do item ID: {}", requisitoId, itemConfigId);
    }

    // ===== HELPERS PRIVADOS =====

    private void validarCamposRequisito(ItemRequisitoRequest request) {
        if (request.tipo() != TipoRequisito.NIVEL
                && (request.alvo() == null || request.alvo().isBlank())) {
            throw new ValidationException(
                "alvo é obrigatório para requisito do tipo " + request.tipo()
            );
        }
    }
}
