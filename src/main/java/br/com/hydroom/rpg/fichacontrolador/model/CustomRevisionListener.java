package br.com.hydroom.rpg.fichacontrolador.model;

import br.com.hydroom.rpg.fichacontrolador.model.Usuario;
import br.com.hydroom.rpg.fichacontrolador.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Listener customizado para capturar informações adicionais em cada revisão do Hibernate Envers.
 */
@Slf4j
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;

        // Captura o usuário logado
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof OAuth2User oauth2User) {
                    String providerId = oauth2User.getAttribute("sub");
                    // Por enquanto não temos como buscar o repository aqui
                    // Será preenchido via AuditorAware
                    log.debug("Revisão criada por usuário providerId: {}", providerId);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao capturar usuário na revisão: {}", e.getMessage());
        }

        // Captura o IP de origem
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ipAddress = getClientIpAddress(request);
                customRevisionEntity.setIpOrigem(ipAddress);
                log.debug("Revisão criada de IP: {}", ipAddress);
            }
        } catch (Exception e) {
            log.warn("Erro ao capturar IP na revisão: {}", e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
