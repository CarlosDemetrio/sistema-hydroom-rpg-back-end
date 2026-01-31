package br.com.hydroom.rpg.fichacontrolador.filter;

import br.com.hydroom.rpg.fichacontrolador.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de Rate Limiting aplicado a todas as requisições.
 * Protege contra ataques de força bruta e DoS/DDoS.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIP = getClientIP(request);
        String requestURI = request.getRequestURI();

        // Seleciona bucket apropriado baseado no endpoint
        Bucket bucket = selectBucket(clientIP, requestURI);

        // Tenta consumir 1 token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Token consumido com sucesso - requisição permitida

            // Adiciona headers informativos (opcional)
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

            filterChain.doFilter(request, response);
        } else {
            // Rate limit excedido - bloqueia requisição

            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000; // converter para segundos

            log.warn("Rate limit excedido para IP: {} no endpoint: {} - Aguardar {} segundos",
                    clientIP, requestURI, waitForRefill);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit excedido. Tente novamente em %d segundos.\",\"retryAfter\":%d}",
                    waitForRefill, waitForRefill
            ));
        }
    }

    /**
     * Seleciona o bucket apropriado baseado no endpoint.
     */
    private Bucket selectBucket(String clientIP, String requestURI) {
        if (requestURI.startsWith("/api/public")) {
            return rateLimitConfig.resolvePublicBucket(clientIP);
        } else if (requestURI.contains("/login") || requestURI.contains("/oauth2")) {
            return rateLimitConfig.resolveAuthBucket(clientIP);
        } else {
            return rateLimitConfig.resolveBucket(clientIP);
        }
    }

    /**
     * Obtém o IP real do cliente, considerando proxies.
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For pode conter múltiplos IPs, pegar o primeiro
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}
