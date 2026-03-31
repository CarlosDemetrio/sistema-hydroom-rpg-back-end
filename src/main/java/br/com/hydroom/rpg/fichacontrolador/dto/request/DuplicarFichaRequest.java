package br.com.hydroom.rpg.fichacontrolador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DuplicarFichaRequest(
    @NotBlank(message = "Novo nome é obrigatório")
    @Size(max = 100)
    String novoNome,
    boolean manterJogador
) {}
