package br.com.hydroom.rpg.fichacontrolador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configures HTTP clients for OAuth2 token exchange and userinfo calls with explicit
 * timeouts. Without timeouts, Cloud Run's 60s request timeout triggers if Google's
 * endpoints are unreachable (e.g., VPC egress misconfiguration), resulting in a 504.
 * With 15s timeouts, failures surface as fast OAuth2 errors that redirect to /login?error.
 */
@Configuration
public class OAuth2ClientConfig {

    private static final Duration OAUTH2_TIMEOUT = Duration.ofSeconds(15);

    @Bean
    public SimpleClientHttpRequestFactory oAuth2RequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(OAUTH2_TIMEOUT);
        factory.setReadTimeout(OAUTH2_TIMEOUT);
        return factory;
    }

    /**
     * RestTemplate used by {@link org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService}
     * to call Google's userinfo endpoint.
     */
    @Bean
    public RestTemplate oAuth2RestTemplate(SimpleClientHttpRequestFactory oAuth2RequestFactory) {
        return new RestTemplate(oAuth2RequestFactory);
    }

    /**
     * Token response client used by Spring Security to exchange the authorization code
     * for an access token (POST to oauth2.googleapis.com/token).
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(
            SimpleClientHttpRequestFactory oAuth2RequestFactory) {
        RestClient restClient = RestClient.builder()
                .requestFactory(oAuth2RequestFactory)
                .build();
        RestClientAuthorizationCodeTokenResponseClient client =
                new RestClientAuthorizationCodeTokenResponseClient();
        client.setRestClient(restClient);
        return client;
    }
}
