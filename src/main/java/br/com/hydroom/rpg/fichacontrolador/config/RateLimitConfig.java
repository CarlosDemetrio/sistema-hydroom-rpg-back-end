package br.com.hydroom.rpg.fichacontrolador.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuração de Rate Limiting usando Bucket4j.
 * Protege contra ataques de força bruta e DoS/DDoS.
 */
@Configuration
public class RateLimitConfig {

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
        Bandwidth limit = Bandwidth.classic(
                100, // capacidade máxima
                Refill.intervally(100, Duration.ofMinutes(1)) // reabastece 100 tokens a cada 1 minuto
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(
                10, // capacidade máxima (proteção brute force)
                Refill.intervally(10, Duration.ofMinutes(1)) // reabastece 10 tokens a cada 1 minuto
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createPublicBucket() {
        Bandwidth limit = Bandwidth.classic(
                200, // capacidade maior para APIs públicas
                Refill.intervally(200, Duration.ofMinutes(1))
        );
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
