package br.com.hydroom.rpg.fichacontrolador.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Exceção lançada quando há erros de validação customizados.
 */
@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Map.of();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

}
