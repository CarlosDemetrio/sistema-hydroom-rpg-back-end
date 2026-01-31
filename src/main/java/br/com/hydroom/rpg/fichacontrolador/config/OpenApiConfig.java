package br.com.hydroom.rpg.fichacontrolador.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação da API.
 * Acesse em: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${app.backend.url.prod:https://api.seu-dominio.com}")
    private String backendUrlProd;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ficha Controlador RPG API")
                        .version("1.0.0")
                        .description("""
                                API REST para gerenciamento de fichas de personagens de RPG.
                                
                                ### Funcionalidades:
                                - Autenticação via Google OAuth2
                                - Gerenciamento de usuários (Mestres e Jogadores)
                                - CRUD de fichas de personagens
                                - Gerenciamento de jogos
                                - Sistema de permissões baseado em roles
                                
                                ### Segurança:
                                - CSRF Protection
                                - Rate Limiting (100 req/min geral, 10 req/min para auth)
                                - Validação completa de inputs
                                - Headers de segurança HTTP
                                - Logs de auditoria
                                """)
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@hydroom.com.br")
                                .url(frontendUrl))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )
                .servers(List.of(
                        new Server()
                                .url(backendUrl)
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url(backendUrlProd)
                                .description("Servidor de Produção")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("OAuth2"))
                .components(new Components()
                        .addSecuritySchemes("OAuth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Autenticação via Google OAuth2")
                                .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                                        .authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
                                                .authorizationUrl("/oauth2/authorization/google")
                                                .tokenUrl("/login/oauth2/code/google")
                                        )
                                )
                        )
                        .addSecuritySchemes("Session", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("SESSIONID")
                                .description("Sessão baseada em cookie após OAuth2")
                        )
                );
    }
}
