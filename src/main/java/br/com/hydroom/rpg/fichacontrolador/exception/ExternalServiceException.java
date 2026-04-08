package br.com.hydroom.rpg.fichacontrolador.exception;

/**
 * Exceção lançada quando ocorre falha de comunicação com serviço externo (ex: Cloudinary).
 * Mapeada para HTTP 502 Bad Gateway no GlobalExceptionHandler.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
