package br.com.hydroom.rpg.fichacontrolador.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Propriedades de configuração para Rate Limiting.
 * Permite configurar limites via application.properties/yml.
 */
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
@Data
public class RateLimitProperties {

    /**
     * Configurações para endpoints gerais.
     */
    private EndpointConfig general = new EndpointConfig(100, Duration.ofMinutes(1));

    /**
     * Configurações para endpoints de autenticação (mais restritivo).
     */
    private EndpointConfig auth = new EndpointConfig(10, Duration.ofMinutes(1));

    /**
     * Configurações para endpoints públicos (mais permissivo).
     */
    private EndpointConfig publicEndpoint = new EndpointConfig(200, Duration.ofMinutes(1));

    @Data
    public static class EndpointConfig {
        /**
         * Capacidade máxima do bucket (número de tokens).
         */
        private long capacity;

        /**
         * Período de reabastecimento dos tokens.
         */
        private Duration refillDuration;

        public EndpointConfig() {
        }

        public EndpointConfig(long capacity, Duration refillDuration) {
            this.capacity = capacity;
            this.refillDuration = refillDuration;
        }
    }
}
