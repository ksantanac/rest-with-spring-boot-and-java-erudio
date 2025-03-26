package br.com.erudio.security.jwt;

import br.com.erudio.data.dto.security.TokenDTO;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

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

    public TokenDTO createAcessToken(String username, List<String> roles) {

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        String acessToken = getAccessToken(username, roles, now, validity);
        String refreshToken = getRefreshToken(username, roles, now);

        return new TokenDTO(username, true, now, validity, acessToken, refreshToken);
    }

    private String getRefreshToken(String username, List<String> roles, Date now) {
        return "";
    }

    private String getAccessToken(String username, List<String> roles, Date now, Date validity) {

        return "";

    }

}
