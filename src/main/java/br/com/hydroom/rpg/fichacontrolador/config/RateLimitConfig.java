package br.com.hydroom.rpg.fichacontrolador.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuração de Rate Limiting usando Bucket4j.
 * Protege contra ataques de força bruta e DoS/DDoS.
 *
 * As configurações podem ser customizadas via application.properties:
 * - app.rate-limit.general.capacity
 * - app.rate-limit.general.refill-duration
 * - app.rate-limit.auth.capacity
 * - app.rate-limit.auth.refill-duration
 * - app.rate-limit.public-endpoint.capacity
 * - app.rate-limit.public-endpoint.refill-duration
 */
@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;

    /**
     * Cache de buckets por IP.
     * Em produção, considere usar Redis ou outra solução distribuída.
     */
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Cria bucket de rate limiting para endpoints gerais.
     * Limite: 100 requisições por minuto por IP.
     */
    public Bucket resolveBucket(String key) {
        return bucketCache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Cria bucket de rate limiting para endpoints de login/autenticação.
     * Limite mais restritivo: 10 tentativas por minuto por IP.
     */
    public Bucket resolveAuthBucket(String key) {
        return bucketCache.computeIfAbsent(key + ":auth", k -> createAuthBucket());
    }

    /**
     * Cria bucket para API pública.
     * Limite: 200 requisições por minuto por IP.
     */
    public Bucket resolvePublicBucket(String key) {
        return bucketCache.computeIfAbsent(key + ":public", k -> createPublicBucket());
    }

    private Bucket createNewBucket() {
        RateLimitProperties.EndpointConfig config = rateLimitProperties.getGeneral();
        Bandwidth limit = Bandwidth.builder()
                .capacity(config.getCapacity())
                .refillGreedy(config.getCapacity(), config.getRefillDuration())
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createAuthBucket() {
        RateLimitProperties.EndpointConfig config = rateLimitProperties.getAuth();
        Bandwidth limit = Bandwidth.builder()
                .capacity(config.getCapacity())
                .refillGreedy(config.getCapacity(), config.getRefillDuration())
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createPublicBucket() {
        RateLimitProperties.EndpointConfig config = rateLimitProperties.getPublicEndpoint();
        Bandwidth limit = Bandwidth.builder()
                .capacity(config.getCapacity())
                .refillGreedy(config.getCapacity(), config.getRefillDuration())
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Limpa o cache periodicamente (pode ser chamado por scheduled task).
     * Evita crescimento infinito do cache.
     */
    public void clearCache() {
        bucketCache.clear();
    }

    /**
     * Remove bucket específico do cache.
     */
    public void removeBucket(String key) {
        bucketCache.remove(key);
        bucketCache.remove(key + ":auth");
        bucketCache.remove(key + ":public");
    }
}
