package br.com.hydroom.rpg.fichacontrolador.exception;

/**
 * Exceção lançada quando um recurso não é encontrado.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s com ID %d não encontrado", resource, id));
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s com identificador '%s' não encontrado", resource, identifier));
    }
}
