package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseEquipamentoInicialRequest;
import br.com.hydroom.rpg.fichacontrolador.dto.request.configuracao.ClasseEquipamentoInicialUpdateRequest;
import br.com.hydroom.rpg.fichacontrolador.exception.BusinessException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseEquipamentoInicial;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.ItemConfig;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseEquipamentoInicialRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ItemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de equipamentos iniciais de classes de personagem.
 *
 * <p>Valida que o item pertence ao mesmo jogo da classe (RN-T3-01).</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClasseEquipamentoInicialService {

    private final ClasseEquipamentoInicialRepository equipamentoRepository;
    private final ConfiguracaoClasseRepository classeRepository;
    private final ItemConfigRepository itemConfigRepository;

    /**
     * Lista todos os equipamentos iniciais de uma classe com itens carregados.
     */
    public List<ClasseEquipamentoInicial> listar(Long classeId) {
        log.debug("Listando equipamentos iniciais da classe ID: {}", classeId);
        if (!classeRepository.existsById(classeId)) {
            throw new ResourceNotFoundException("ClassePersonagem", classeId);
        }
        return equipamentoRepository.findByClasseIdWithItems(classeId);
    }

    /**
     * Cria um novo equipamento inicial para uma classe.
     *
     * <p>RN-T3-01: o item deve pertencer ao mesmo jogo que a classe.</p>
     */
    @Transactional
    public ClasseEquipamentoInicial criar(Long classeId, ClasseEquipamentoInicialRequest request) {
        log.debug("Criando equipamento inicial para classe ID: {}, item ID: {}", classeId, request.itemConfigId());

        ClassePersonagem classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassePersonagem", classeId));

        ItemConfig itemConfig = itemConfigRepository.findById(request.itemConfigId())
                .orElseThrow(() -> new ResourceNotFoundException("ItemConfig", request.itemConfigId()));

        if (!itemConfig.getJogo().getId().equals(classe.getJogo().getId())) {
            throw new BusinessException("Item deve pertencer ao mesmo jogo que a classe");
        }

        int quantidade = request.quantidade() > 0 ? request.quantidade() : 1;

        ClasseEquipamentoInicial equipamento = ClasseEquipamentoInicial.builder()
                .classe(classe)
                .itemConfig(itemConfig)
                .obrigatorio(request.obrigatorio())
                .grupoEscolha(request.grupoEscolha())
                .quantidade(quantidade)
                .build();

        ClasseEquipamentoInicial salvo = equipamentoRepository.save(equipamento);
        log.info("Equipamento inicial criado. ID: {}, Classe: {}, Item: {}", salvo.getId(), classeId, request.itemConfigId());
        return salvo;
    }

    /**
     * Atualiza campos de um equipamento inicial existente.
     */
    @Transactional
    public ClasseEquipamentoInicial atualizar(Long classeId, Long id, ClasseEquipamentoInicialUpdateRequest request) {
        log.debug("Atualizando equipamento inicial ID: {} da classe ID: {}", id, classeId);

        ClasseEquipamentoInicial equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseEquipamentoInicial", id));

        if (!equipamento.getClasse().getId().equals(classeId)) {
            throw new ValidationException("O equipamento não pertence à classe informada");
        }

        if (request.obrigatorio() != null) {
            equipamento.setObrigatorio(request.obrigatorio());
        }
        if (request.grupoEscolha() != null) {
            equipamento.setGrupoEscolha(request.grupoEscolha());
        }
        if (request.quantidade() != null) {
            equipamento.setQuantidade(request.quantidade());
        }

        ClasseEquipamentoInicial atualizado = equipamentoRepository.save(equipamento);
        log.info("Equipamento inicial ID: {} atualizado com sucesso", id);
        return atualizado;
    }

    /**
     * Realiza soft delete de um equipamento inicial.
     */
    @Transactional
    public void deletar(Long classeId, Long id) {
        log.debug("Deletando equipamento inicial ID: {} da classe ID: {}", id, classeId);

        ClasseEquipamentoInicial equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClasseEquipamentoInicial", id));

        if (!equipamento.getClasse().getId().equals(classeId)) {
            throw new ValidationException("O equipamento não pertence à classe informada");
        }

        equipamento.delete();
        equipamentoRepository.save(equipamento);
        log.info("Equipamento inicial ID: {} deletado (soft delete) da classe ID: {}", id, classeId);
    }
}
