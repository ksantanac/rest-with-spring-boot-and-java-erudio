package br.com.erudio.security.jwt;

import br.com.erudio.data.dto.security.TokenDTO;
import br.com.erudio.exception.InvalidJwtAuthenticationException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey;

    @Value("${security.jwt.token.expire-lenght:3600000}")
    private long validityInMilliseconds;

    @Autowired
    private UserDetailsService userDetailsService;

    Algorithm algorithm = null;

    @PostConstruct
    protected void init() {
        // Converte a chave secreta (String) para Base64 (formato de codificação)
        // Isso é útil para garantir que a chave tenha apenas caracteres válidos
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());

        // Cria um algoritmo de assinatura HMAC usando SHA-256 com a chave secreta
        // HMAC-SHA256 é um algoritmo comum para assinar tokens JWT
        algorithm = Algorithm.HMAC256(secretKey.getBytes());
    }

    /**
     * Cria um TokenDTO contendo access token e refresh token para um usuário
     *
     * @param username Nome do usuário autenticado
     * @param roles Lista de permissões/roles do usuário
     * @return TokenDTO contendo ambos tokens e informações relevantes
     */
    public TokenDTO createAcessToken(String username, List<String> roles) {
        // Data/hora atual
        Date now = new Date();

        // Data de expiração (now + tempo configurado em validityInMilliseconds)
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        // Gera o access token (token de acesso principal)
        String acessToken = getAccessToken(username, roles, now, validity);

        // Gera o refresh token (token para renovar o acesso)
        String refreshToken = getRefreshToken(username, roles, now);

        // Retorna DTO com todas as informações
        return new TokenDTO(username, true, now, validity, acessToken, refreshToken);
    }

    /**
     * Gera um refresh token JWT
     *
     * @param username Nome do usuário
     * @param roles Lista de permissões
     * @param now Data/hora atual
     * @return String contendo o refresh token assinado
     */
    private String getRefreshToken(String username, List<String> roles, Date now) {
        // Define validade do refresh token (normalmente mais longo que o access token)
        Date refreshTokenValidty = new Date(now.getTime() + validityInMilliseconds);

        // Constrói e retorna o token JWT
        return JWT.create()
                .withClaim("roles", roles)          // Adiciona roles como claim
                .withIssuedAt(now)                  // Data de emissão
                .withExpiresAt(refreshTokenValidty) // Data de expiração
                .withSubject(username)              // Identificador do usuário
                .sign(algorithm)                    // Assina com o algoritmo HMAC256
                .toString();                        // Converte para String
    }

    /**
     * Gera um access token JWT
     *
     * @param username Nome do usuário
     * @param roles Lista de permissões
     * @param now Data/hora atual
     * @param validity Data de expiração
     * @return String contendo o access token assinado
     */
    private String getAccessToken(String username, List<String> roles, Date now, Date validity) {
        // Obtém a URL base da aplicação para usar como issuer
        String issuerUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .build()
                .toUriString();

        // Constrói e retorna o token JWT
        return JWT.create()
                .withClaim("roles", roles)    // Adiciona roles como claim
                .withIssuedAt(now)            // Data de emissão
                .withExpiresAt(validity)      // Data de expiração
                .withSubject(username)        // Identificador do usuário
                .withIssuer(issuerUrl)        // Quem emitiu o token (sua aplicação)
                .sign(algorithm)              // Assina com o algoritmo HMAC256
                .toString();                  // Converte para String
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = decodedToken(token);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(decodedJWT.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private DecodedJWT decodedToken(String token) {
        Algorithm alg = Algorithm.HMAC256(secretKey.getBytes());
        JWTVerifier verifier = JWT.require(alg).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        return decodedJWT;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorizations");

        // Bearer
        if (StringUtils.isEmpty(bearerToken) && bearerToken.startsWith("Beaer ")) {
            return bearerToken.substring("Beaer ".length());
        } else {
            throw new InvalidJwtAuthenticationException("Invalid JWT Token.");
        }
    }

    public boolean validateToken(String token) {
        DecodedJWT decodedJWT = decodedToken(token);
        try {
            if (decodedJWT.getExpiresAt().before(new Date())) {
                return false;
            }

            return true;
        } catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Expire or Invalid Token.");
        }
    }

}
