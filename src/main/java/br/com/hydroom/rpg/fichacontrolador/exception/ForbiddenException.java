package br.com.hydroom.rpg.fichacontrolador.exception;

/**
 * Exceção lançada quando um usuário tenta acessar um recurso sem permissão.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
