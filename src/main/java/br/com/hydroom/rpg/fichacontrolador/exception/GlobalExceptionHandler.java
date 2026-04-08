package br.com.hydroom.rpg.fichacontrolador.exception;

import br.com.hydroom.rpg.fichacontrolador.constants.ValidationMessages;
import br.com.hydroom.rpg.fichacontrolador.exception.ExternalServiceException;
import br.com.hydroom.rpg.fichacontrolador.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler para tratamento centralizado de exceções.
 * Implementa OWASP Security - não expõe detalhes internos do sistema.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação de campos (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Erro de validação na requisição {} - Campos inválidos: {}",
                request.getRequestURI(), errors.keySet());

        ValidationErrorResponse response = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ValidationMessages.Erro.DADOS_INVALIDOS,
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata erros de acesso negado (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Tentativa de acesso negado ao recurso: {} - IP: {}",
                request.getRequestURI(),
                request.getRemoteAddr());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                ValidationMessages.Seguranca.ACESSO_NEGADO,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Trata erros de autenticação (401 Unauthorized).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Falha de autenticação ao acessar: {} - IP: {}",
                request.getRequestURI(),
                request.getRemoteAddr());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                ValidationMessages.Seguranca.NAO_AUTENTICADO,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Trata recurso não encontrado (404 Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Recurso não encontrado: {} - {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage() != null ? ex.getMessage() : ValidationMessages.Erro.NAO_ENCONTRADO,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Trata violações de regras de negócio.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        log.warn("Erro de negócio: {} - {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage() != null ? ex.getMessage() : ValidationMessages.Erro.OPERACAO_INVALIDA,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Trata conflitos (409 Conflict) - recursos duplicados, etc.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex,
            HttpServletRequest request) {

        log.warn("Conflito detectado: {} - {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage() != null ? ex.getMessage() : ValidationMessages.Erro.JA_EXISTE,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Trata erros de validação customizados.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleCustomValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        log.warn("Erro de validação customizada: {} - {}", request.getRequestURI(), ex.getMessage());

        ValidationErrorResponse response = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : ValidationMessages.Erro.DADOS_INVALIDOS,
                request.getRequestURI(),
                ex.getErrors()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata violações de integridade do banco de dados (constraints, unique, etc).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Violação de integridade de dados: {} - {}",
                request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());

        String message = ValidationMessages.Erro.INTEGRIDADE_DADOS;

        // Detecta tipo de violação para mensagem mais específica
        String errorMsg = ex.getMostSpecificCause().getMessage().toLowerCase();
        if (errorMsg.contains("unique") || errorMsg.contains("duplicate")) {
            message = ValidationMessages.Erro.JA_EXISTE;
        } else if (errorMsg.contains("foreign key") || errorMsg.contains("fk_")) {
            message = "Operação inválida: registro está sendo referenciado por outros dados";
        }

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Trata exceções genéricas não previstas.
     * IMPORTANTE: Não expõe detalhes da exceção ao cliente (OWASP Security).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log completo apenas no servidor
        log.error("Erro não tratado ao processar requisição: {} - Erro: {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        // Resposta genérica ao cliente (sem detalhes internos)
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ValidationMessages.Erro.INTERNO,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Trata argumentos ilegais.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Argumento ilegal na requisição: {} - {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage() != null ? ex.getMessage() : ValidationMessages.Erro.DADOS_INVALIDOS,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata acesso proibido lançado programaticamente (403 Forbidden).
     * Diferente de {@link AccessDeniedException} do Spring Security,
     * este handler trata a exceção de domínio usada nos services.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request) {

        log.warn("Acesso proibido: {} - {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage() != null ? ex.getMessage() : ValidationMessages.Seguranca.ACESSO_NEGADO,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Trata falhas de comunicação com serviços externos (502 Bad Gateway).
     * Lançada pelo CloudinaryUploadService quando o upload falha.
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException ex,
            HttpServletRequest request) {

        log.error("Falha de comunicação com serviço externo: {} - Erro: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "Falha ao comunicar com serviço externo. Tente novamente em instantes.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
}
