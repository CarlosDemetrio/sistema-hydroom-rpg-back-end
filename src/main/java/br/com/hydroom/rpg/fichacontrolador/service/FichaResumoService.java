package br.com.hydroom.rpg.fichacontrolador.service;

import br.com.hydroom.rpg.fichacontrolador.dto.response.FichaResumoResponse;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import br.com.hydroom.rpg.fichacontrolador.exception.ResourceNotFoundException;
import br.com.hydroom.rpg.fichacontrolador.model.Ficha;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAmeaca;
import br.com.hydroom.rpg.fichacontrolador.model.FichaAtributo;
import br.com.hydroom.rpg.fichacontrolador.model.FichaBonus;
import br.com.hydroom.rpg.fichacontrolador.model.FichaEssencia;
import br.com.hydroom.rpg.fichacontrolador.model.FichaVida;
import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.model.enums.RoleJogo;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAmeacaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaAtributoRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaBonusRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaEssenciaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.FichaVidaRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.JogoParticipanteRepository;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsável por gerar o resumo completo de uma ficha.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FichaResumoService {

    private final FichaRepository fichaRepository;
    private final FichaAtributoRepository fichaAtributoRepository;
    private final FichaBonusRepository fichaBonusRepository;
    private final FichaVidaRepository fichaVidaRepository;
    private final FichaEssenciaRepository fichaEssenciaRepository;
    private final FichaAmeacaRepository fichaAmeacaRepository;
    private final JogoParticipanteRepository jogoParticipanteRepository;
    private final UsuarioRepository usuarioRepository;

    public FichaResumoResponse getResumo(Long fichaId) {
        Ficha ficha = fichaRepository.findByIdWithRelationships(fichaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha não encontrada: " + fichaId));

        verificarAcessoLeitura(ficha);

        // Atributos: abreviação → total (JOIN FETCH evita N+1 ao acessar atributoConfig.abreviacao)
        List<FichaAtributo> fichaAtributos = fichaAtributoRepository.findByFichaIdWithConfig(fichaId);
        Map<String, Integer> atributosTotais = new LinkedHashMap<>();
        for (FichaAtributo fa : fichaAtributos) {
            String abreviacao = fa.getAtributoConfig() != null ? fa.getAtributoConfig().getAbreviacao() : null;
            if (abreviacao != null) {
                atributosTotais.put(abreviacao, fa.getTotal() != null ? fa.getTotal() : 0);
            }
        }

        // Bônus: nome → total (JOIN FETCH evita N+1 ao acessar bonusConfig.nome)
        List<FichaBonus> fichaBonus = fichaBonusRepository.findByFichaIdWithConfig(fichaId);
        Map<String, Integer> bonusTotais = new LinkedHashMap<>();
        for (FichaBonus fb : fichaBonus) {
            String nome = fb.getBonusConfig() != null ? fb.getBonusConfig().getNome() : null;
            if (nome != null) {
                bonusTotais.put(nome, fb.getTotal() != null ? fb.getTotal() : 0);
            }
        }

        // Vida
        FichaVida vida = fichaVidaRepository.findByFichaId(fichaId).orElse(null);
        int vidaTotal = vida != null && vida.getVidaTotal() != null ? vida.getVidaTotal() : 0;

        // Essência
        FichaEssencia essencia = fichaEssenciaRepository.findByFichaId(fichaId).orElse(null);
        int essenciaTotal = essencia != null && essencia.getTotal() != null ? essencia.getTotal() : 0;

        // Ameaça
        FichaAmeaca ameaca = fichaAmeacaRepository.findByFichaId(fichaId).orElse(null);
        int ameacaTotal = ameaca != null && ameaca.getTotal() != null ? ameaca.getTotal() : 0;

        String racaNome = ficha.getRaca() != null ? ficha.getRaca().getNome() : null;
        String classeNome = ficha.getClasse() != null ? ficha.getClasse().getNome() : null;

        return new FichaResumoResponse(
                ficha.getId(),
                ficha.getNome(),
                ficha.getNivel() != null ? ficha.getNivel() : 1,
                ficha.getXp() != null ? ficha.getXp() : 0L,
                racaNome,
                classeNome,
                atributosTotais,
                bonusTotais,
                vidaTotal,
                essenciaTotal,
                ameacaTotal
        );
    }

    private void verificarAcessoLeitura(Ficha ficha) {
        Usuario usuarioAtual = getUsuarioAtual();
        Long jogoId = ficha.getJogo().getId();

        boolean isMestre = jogoParticipanteRepository.existsByJogoIdAndUsuarioIdAndRole(
                jogoId, usuarioAtual.getId(), RoleJogo.MESTRE);

        if (isMestre) {
            return;
        }

        if (!usuarioAtual.getId().equals(ficha.getJogadorId())) {
            throw new ForbiddenException("Acesso negado: você não tem permissão para acessar esta ficha.");
        }
    }

    private Usuario getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ForbiddenException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado: " + email));
    }
}
