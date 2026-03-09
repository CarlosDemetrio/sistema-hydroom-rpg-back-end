package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AptidaoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.BonusConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseAptidaoBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClasseBonus;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseAptidaoBonusRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ClasseBonusRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAptidaoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.BonusConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service para gerenciamento de configurações de Classes de Personagem.
 *
 * <p>Classes definem o arquétipo do personagem (Guerreiro, Mago, Ladino, etc.)</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ClasseConfiguracaoService extends AbstractConfiguracaoService<ClassePersonagem, ConfiguracaoClasseRepository> {

    @Autowired
    private ClasseBonusRepository classeBonusRepository;

    @Autowired
    private ClasseAptidaoBonusRepository classeAptidaoBonusRepository;

    @Autowired
    private BonusConfigRepository bonusConfigRepository;

    @Autowired
    private ConfiguracaoAptidaoRepository aptidaoRepository;

    public ClasseConfiguracaoService(ConfiguracaoClasseRepository repository) {
        super(repository, "Classe");
    }

    @Override
    public ClassePersonagem buscarPorId(Long id) {
        return repository.findByIdWithBonuses(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe", id));
    }

    @Override
    public List<ClassePersonagem> listar(Long jogoId) {
        log.debug("Listando classes para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    @Override
    protected void validarAntesCriar(ClassePersonagem configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(ClassePersonagem configuracaoExistente, ClassePersonagem configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(ClassePersonagem existente, ClassePersonagem atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }

    // ===== CLASSE BONUS =====

    @Transactional
    public ClasseBonus adicionarBonus(Long classeId, Long bonusId, BigDecimal valorPorNivel) {
        ClassePersonagem classe = buscarPorId(classeId);
        BonusConfig bonus = bonusConfigRepository.findById(bonusId)
            .orElseThrow(() -> new ResourceNotFoundException("BonusConfig", bonusId));

        if (!bonus.getJogo().getId().equals(classe.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.ClasseBonus.JOGO_DIFERENTE);
        }
        if (classeBonusRepository.existsByClasseIdAndBonusId(classeId, bonusId)) {
            throw new ConflictException(ValidationMessages.ClasseBonus.JA_EXISTE);
        }

        return classeBonusRepository.save(
            ClasseBonus.builder().classe(classe).bonus(bonus).valorPorNivel(valorPorNivel).build()
        );
    }

    public List<ClasseBonus> listarBonus(Long classeId) {
        return classeBonusRepository.findByClasseId(classeId);
    }

    @Transactional
    public void removerBonus(Long classeId, Long classeBonusId) {
        ClasseBonus cb = classeBonusRepository.findById(classeBonusId)
            .orElseThrow(() -> new ResourceNotFoundException("ClasseBonus", classeBonusId));
        if (!cb.getClasse().getId().equals(classeId)) {
            throw new ValidationException("Bônus não pertence à classe informada.");
        }
        classeBonusRepository.delete(cb);
    }

    // ===== CLASSE APTIDAO BONUS =====

    @Transactional
    public ClasseAptidaoBonus adicionarAptidaoBonus(Long classeId, Long aptidaoId, Integer bonus) {
        ClassePersonagem classe = buscarPorId(classeId);
        AptidaoConfig aptidao = aptidaoRepository.findById(aptidaoId)
            .orElseThrow(() -> new ResourceNotFoundException("AptidaoConfig", aptidaoId));

        if (!aptidao.getJogo().getId().equals(classe.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.ClasseAptidaoBonus.JOGO_DIFERENTE);
        }
        if (classeAptidaoBonusRepository.existsByClasseIdAndAptidaoId(classeId, aptidaoId)) {
            throw new ConflictException(ValidationMessages.ClasseAptidaoBonus.JA_EXISTE);
        }

        return classeAptidaoBonusRepository.save(
            ClasseAptidaoBonus.builder().classe(classe).aptidao(aptidao).bonus(bonus).build()
        );
    }

    public List<ClasseAptidaoBonus> listarAptidaoBonus(Long classeId) {
        return classeAptidaoBonusRepository.findByClasseId(classeId);
    }

    @Transactional
    public void removerAptidaoBonus(Long classeId, Long classeAptidaoBonusId) {
        ClasseAptidaoBonus cab = classeAptidaoBonusRepository.findById(classeAptidaoBonusId)
            .orElseThrow(() -> new ResourceNotFoundException("ClasseAptidaoBonus", classeAptidaoBonusId));
        if (!cab.getClasse().getId().equals(classeId)) {
            throw new ValidationException("Bônus de aptidão não pertence à classe informada.");
        }
        classeAptidaoBonusRepository.delete(cab);
    }

    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe uma classe com o nome '" + nome + "' neste jogo");
        }
    }
}
