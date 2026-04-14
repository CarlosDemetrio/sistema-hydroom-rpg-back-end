package br.com.hydroom.rpg.fichacontrolador.service.configuracao;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.exception.ConflictException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.exception.ValidationException;
import br.com.hydroom.rpg.fichacontrolador.model.AtributoConfig;
import br.com.hydroom.rpg.fichacontrolador.model.ClassePersonagem;
import br.com.hydroom.rpg.fichacontrolador.model.Raca;
import br.com.hydroom.rpg.fichacontrolador.model.RacaBonusAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.RacaClassePermitida;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoClasseRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.ConfiguracaoRacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaBonusAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.RacaClassePermitidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.VantagemPreRequisitoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de configurações de Raças.
 *
 * <p>Raças definem a espécie/etnia do personagem (Humano, Elfo, Anão, etc.)</p>
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class RacaConfiguracaoService extends AbstractConfiguracaoService<Raca, ConfiguracaoRacaRepository> {

    @Autowired
    private RacaBonusAtributoRepository bonusAtributoRepository;

    @Autowired
    private RacaClassePermitidaRepository classePermitidaRepository;

    @Autowired
    private ConfiguracaoAtributoRepository atributoRepository;

    @Autowired
    private ConfiguracaoClasseRepository classeRepository;

    @Autowired
    private VantagemPreRequisitoRepository vantagemPreRequisitoRepository;

    public RacaConfiguracaoService(ConfiguracaoRacaRepository repository) {
        super(repository, "Raça");
    }

    @Override
    public Raca buscarPorId(Long id) {
        return repository.findByIdWithRelationships(id)
            .orElseThrow(() -> new ResourceNotFoundException("Raça", id));
    }

    @Override
    public List<Raca> listar(Long jogoId) {
        log.debug("Listando raças para jogo ID: {}", jogoId);
        return repository.findByJogoIdOrderByOrdemExibicao(jogoId);
    }

    public List<Raca> listar(Long jogoId, String nome) {
        if (nome != null && !nome.isBlank()) {
            return repository.findByJogoIdAndNomeContainingIgnoreCaseOrderByOrdemExibicao(jogoId, nome);
        }
        return listar(jogoId);
    }

    @Override
    protected void validarAntesCriar(Raca configuracao) {
        validateUniqueNome(configuracao.getNome(), configuracao.getJogo().getId());
    }

    @Override
    protected void validarAntesAtualizar(Raca configuracaoExistente, Raca configuracaoAtualizada) {
        if (!configuracaoExistente.getNome().equals(configuracaoAtualizada.getNome())) {
            validateUniqueNome(configuracaoAtualizada.getNome(), configuracaoExistente.getJogo().getId());
        }
    }

    @Override
    protected void atualizarCampos(Raca existente, Raca atualizado) {
        existente.setNome(atualizado.getNome());
        existente.setDescricao(atualizado.getDescricao());
        existente.setOrdemExibicao(atualizado.getOrdemExibicao());
    }

    // ===== BONUS ATRIBUTO =====

    @Transactional
    public RacaBonusAtributo adicionarBonusAtributo(Long racaId, Long atributoId, Integer bonus) {
        Raca raca = buscarPorId(racaId);
        AtributoConfig atributo = atributoRepository.findById(atributoId)
            .orElseThrow(() -> new ResourceNotFoundException("AtributoConfig", atributoId));

        if (!atributo.getJogo().getId().equals(raca.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.RacaBonusAtributo.JOGO_DIFERENTE);
        }
        if (bonusAtributoRepository.existsByRacaIdAndAtributoId(racaId, atributoId)) {
            throw new ConflictException(ValidationMessages.RacaBonusAtributo.JA_EXISTE);
        }

        return bonusAtributoRepository.save(
            RacaBonusAtributo.builder().raca(raca).atributo(atributo).bonus(bonus).build()
        );
    }

    public List<RacaBonusAtributo> listarBonusAtributo(Long racaId) {
        return bonusAtributoRepository.findByRacaId(racaId);
    }

    @Transactional
    public void removerBonusAtributo(Long racaId, Long bonusAtributoId) {
        RacaBonusAtributo rba = bonusAtributoRepository.findById(bonusAtributoId)
            .orElseThrow(() -> new ResourceNotFoundException("RacaBonusAtributo", bonusAtributoId));
        if (!rba.getRaca().getId().equals(racaId)) {
            throw new ValidationException("Bônus de atributo não pertence à raça informada.");
        }
        bonusAtributoRepository.delete(rba);
    }

    // ===== CLASSES PERMITIDAS =====

    @Transactional
    public RacaClassePermitida permitirClasse(Long racaId, Long classeId) {
        Raca raca = buscarPorId(racaId);
        ClassePersonagem classe = classeRepository.findById(classeId)
            .orElseThrow(() -> new ResourceNotFoundException("ClassePersonagem", classeId));

        if (!classe.getJogo().getId().equals(raca.getJogo().getId())) {
            throw new ValidationException(ValidationMessages.RacaClassePermitida.JOGO_DIFERENTE);
        }
        if (classePermitidaRepository.existsByRacaIdAndClasseId(racaId, classeId)) {
            throw new ConflictException(ValidationMessages.RacaClassePermitida.JA_EXISTE);
        }

        return classePermitidaRepository.save(
            RacaClassePermitida.builder().raca(raca).classe(classe).build()
        );
    }

    public List<RacaClassePermitida> listarClassesPermitidas(Long racaId) {
        return classePermitidaRepository.findByRacaId(racaId);
    }

    @Transactional
    public void removerClassePermitida(Long racaId, Long classePermitidaId) {
        RacaClassePermitida rcp = classePermitidaRepository.findById(classePermitidaId)
            .orElseThrow(() -> new ResourceNotFoundException("RacaClassePermitida", classePermitidaId));
        if (!rcp.getRaca().getId().equals(racaId)) {
            throw new ValidationException("Classe permitida não pertence à raça informada.");
        }
        classePermitidaRepository.delete(rcp);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        long count = vantagemPreRequisitoRepository.countByRacaId(id);
        if (count > 0) {
            throw new ConflictException(
                "Não é possível excluir: Raça usada como pré-requisito em " + count + " vantagem(ns)."
            );
        }
        super.deletar(id);
    }

    private void validateUniqueNome(String nome, Long jogoId) {
        if (repository.existsByJogoIdAndNomeIgnoreCase(jogoId, nome)) {
            throw new ConflictException("Já existe uma raça com o nome '" + nome + "' neste jogo");
        }
    }
}
