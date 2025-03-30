package br.com.erudio.config;

import br.com.erudio.security.jwt.JwtTokenFilter;
import br.com.erudio.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtTokenProvider tokenProvider;

    public SecurityConfig(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // 1. Cria o encoder específico PBKDF2
        PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder(
            "",                  // Salt secreto (vazio para salt aleatório)
            8,                   // Tamanho do salt em bytes
            185000,              // Número de iterações
            Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
        );

        // 2. Cria um mapa de encoders (útil para migração ou múltiplos algoritmos)
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2Encoder);

        // 3. Cria um encoder delegado que pode suportar múltiplos algoritmos
        DelegatingPasswordEncoder passwordEncoder =
                new DelegatingPasswordEncoder("pbkdf2", encoders);

        // 4. Configura o encoder padrão para verificação
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);

        return passwordEncoder;
    }

    @Bean
    // Expõe o AuthenticationManager como um bean Spring
    // Necessário para processar autenticações programáticas
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    // Configura a cadeia de filtros de segurança principal
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cria nosso filtro JWT personalizado
        JwtTokenFilter filter = new JwtTokenFilter(tokenProvider);

        // Configuração da segurança HTTP (desabilitamos formatação automática para melhor legibilidade)
        // @formatter:off
        return http
            // Desabilita autenticação básica (HTTP Basic Auth)
            .httpBasic(AbstractHttpConfigurer::disable)

            // Desabilita proteção CSRF (não necessário para APIs stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // Adiciona nosso filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)

            // Configura política de sessão como STATELESS (sem sessões HTTP)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configura as regras de autorização
            .authorizeHttpRequests(authorize -> authorize
                    // Rotas públicas - acesso permitido sem autenticação
                    .requestMatchers(
                "/auth/signin" +
                        "",          // Endpoint de login
                        "/auth/refresh/**",      // Endpoint para refresh token
                        "/auth/createUser",      // Endpoint de criação de usuário
                        "/swagger-ui/**",        // Documentação Swagger UI
                        "/v3/api-docs/**"        // Especificação OpenAPI
                    ).permitAll()

                    // Rotas da API - requerem autenticação
                    .requestMatchers("/api/**").authenticated()

                    // Rotas explícitamente bloqueadas
                    .requestMatchers("/users").denyAll()
            )

            // Habilita CORS (com configuração padrão)
            .cors(cors -> {})

            // Constrói a configuração
            .build();
    // @formatter:on
    }

}








