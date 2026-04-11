package br.com.hydroom.rpg.fichacontrolador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * Configures HTTP clients for OAuth2 token exchange and userinfo calls with explicit
 * timeouts. Without timeouts, Cloud Run's 60s request timeout triggers if Google's
 * endpoints are unreachable (e.g., VPC egress misconfiguration), resulting in a 504.
 * With 15s timeouts, failures surface as fast OAuth2 errors that redirect to /login?error.
 *
 * The RestClient for token exchange uses OAuth2AccessTokenResponseHttpMessageConverter
 * (not Jackson) because OAuth2AccessTokenResponse has no Jackson-compatible constructor.
 * This fix works for both JVM and GraalVM native image deployments.
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
     * Token response client for the authorization code → access token exchange.
     * Explicitly configures OAuth2AccessTokenResponseHttpMessageConverter so that
     * the token response is parsed without Jackson (avoids "no suitable constructor"
     * errors on both JVM and GraalVM native image).
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(
            SimpleClientHttpRequestFactory oAuth2RequestFactory) {
        RestClient restClient = RestClient.builder()
                .requestFactory(oAuth2RequestFactory)
                .messageConverters(List.of(
                        new FormHttpMessageConverter(),
                        new OAuth2AccessTokenResponseHttpMessageConverter()
                ))
                .defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
                .build();

        RestClientAuthorizationCodeTokenResponseClient client =
                new RestClientAuthorizationCodeTokenResponseClient();
        client.setRestClient(restClient);
        return client;
    }
}
