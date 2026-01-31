package br.com.hydroom.rpg.fichacontrolador.exception;

/**
 * Exceção para conflitos (recursos duplicados, estado inválido, etc).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
