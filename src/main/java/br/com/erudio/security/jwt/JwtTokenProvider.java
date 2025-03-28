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

    // Cria um TokenDTO contendo access token e refresh token para um usuário
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

    // Método que recebe o refreshToken e retorna um novo token de acesso (access token)
    public TokenDTO refreshToken(String refreshToken) {

        var token = "";

        // Verifica se o refreshToken começa com "Bearer " e, se sim, remove essa parte
        if (refreshTokenContainsBearer(refreshToken))
            // Removendo o prefixo "Bearer " do refreshToken, caso ele esteja presente.
            token = refreshToken.substring("Bearer ".length());

        // Criando um verificador JWT com o algoritmo previamente configurado
        JWTVerifier verifier = JWT.require(algorithm).build();

        // Verificando e decodificando o refreshToken usando o verificador
        DecodedJWT decodedJWT = verifier.verify(token);

        // Extraindo o nome de usuário (subject) do token decodificado
        String username = decodedJWT.getSubject();

        // Extraindo as roles (permissões) do token decodificado
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

        // Criando um novo access token com o nome de usuário e as permissões recuperadas
        return createAcessToken(username, roles);
    }

    // Gera um refresh token JWT
    private String getRefreshToken(String username, List<String> roles, Date now) {
        // Define validade do refresh token (normalmente mais longo que o access token)
        Date refreshTokenValidty = new Date(now.getTime() + (validityInMilliseconds * 3));

        // Constrói e retorna o token JWT
        return JWT.create()
                .withClaim("roles", roles)          // Adiciona roles como claim
                .withIssuedAt(now)                  // Data de emissão
                .withExpiresAt(refreshTokenValidty) // Data de expiração
                .withSubject(username)              // Identificador do usuário
                .sign(algorithm);
    }

    // Gera um access token JWT
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
                .sign(algorithm);
    }

    // Obtém a autenticação do usuário a partir do token JWT
    public Authentication getAuthentication(String token) {
        // Decodifica o token JWT
        DecodedJWT decodedJWT = decodedToken(token);

        // Carrega os detalhes do usuário do banco de dados usando o subject (username) do token
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(decodedJWT.getSubject());

        // Cria e retorna um objeto de autenticação do Spring Security
        return new UsernamePasswordAuthenticationToken(
                userDetails,           // Principal (usuário autenticado)
                "",                     // Credenciais (não necessárias após autenticação)
                userDetails.getAuthorities()  // Lista de autoridades/roles do usuário
        );
    }

    // Decodifica e valida um token JWT usando a chave secreta
    private DecodedJWT decodedToken(String token) {
        // Cria o algoritmo de verificação com a chave secreta
        Algorithm alg = Algorithm.HMAC256(secretKey.getBytes());

        // Cria o verificador JWT
        JWTVerifier verifier = JWT.require(alg).build();

        // Verifica e decodifica o token
        DecodedJWT decodedJWT = verifier.verify(token);

        return decodedJWT;
    }

    // Método responsável por extrair o token JWT do cabeçalho da requisição HTTP
    public String resolveToken(HttpServletRequest request) {
        // Obtém o valor do cabeçalho "Authorization"
        String bearerToken = request.getHeader("Authorization");

        // Verifica se o token possui o prefixo "Bearer " e retorna apenas o token, removendo o prefixo
        if (refreshTokenContainsBearer(bearerToken)) return bearerToken.substring("Bearer ".length());

        // Retorna null caso o token não esteja presente ou não tenha o formato esperado
        return null;
    }

    // Método auxiliar que verifica se a string contém um token válido com prefixo "Bearer "
    private static boolean refreshTokenContainsBearer(String refreshToken) {
        return StringUtils.isNotBlank(refreshToken) && refreshToken.startsWith("Bearer ");
    }

    // Método responsável por validar o token JWT
    public boolean validateToken(String token) {
        // Decodifica o token JWT
        DecodedJWT decodedJWT = decodedToken(token);

        try {
            // Verifica se o token está expirado
            if (decodedJWT.getExpiresAt().before(new Date())) {
                return false; // Retorna falso se o token estiver expirado
            }
            return true; // Retorna verdadeiro se o token for válido
        } catch (Exception e) {
            // Lança uma exceção personalizada caso o token seja inválido ou expirado
            throw new InvalidJwtAuthenticationException("Expired or Invalid JWT Token!");
        }
    }

}
