package br.com.erudio.services;

import br.com.erudio.data.dto.security.AccountCredentialsDTO;
import br.com.erudio.data.dto.security.TokenDTO;
import br.com.erudio.data.dto.v1.PersonDTO;
import br.com.erudio.exception.RequiredObjectIsNullException;
import br.com.erudio.model.Person;
import br.com.erudio.model.User;
import br.com.erudio.repository.UserRepository;
import br.com.erudio.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static br.com.erudio.mapper.ObjectMapper.parseObject;

@Service
public class AuthService {

    Logger logger = Logger.getLogger(BookService.class.getName());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository repository;

    // Autentica um usuário e retorna tokens JWT
    public ResponseEntity<TokenDTO> signIn(AccountCredentialsDTO credentials) {

        // 1. Autentica as credenciais do usuário
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                credentials.getUsername(), // Nome de usuário fornecido
                credentials.getPassword()  // Senha fornecida
            )
        );

        // 2. Busca o usuário no banco de dados
        var user = repository.findByUsername(credentials.getUsername());

        // Verifica se usuário foi encontrado (isso normalmente não deveria acontecer se a autenticação passou)
        if (user == null) {
            throw new UsernameNotFoundException("Username " + credentials.getUsername() + " not found!");
        }

        // 3. Gera os tokens JWT para o usuário autenticado
        var token = tokenProvider.createAcessToken(
                credentials.getUsername(), // Nome de usuário
                user.getRoles()           // Roles/permissões do usuário
        );

        // 4. Retorna os tokens com status HTTP 200 (OK)
        return ResponseEntity.ok(token);

    }

    // Método que recebe o nome de usuário e o refresh token, e retorna um novo token de acesso (TokenDTO)
    public ResponseEntity<TokenDTO> refreshToken(String username, String refreshToken) {

        var user = repository.findByUsername(username);
        TokenDTO token;

        if (user != null) {
            // Se o usuário não for encontrado, chama o método refreshToken no TokenProvider para gerar um novo token
            token = tokenProvider.refreshToken(refreshToken);
        } else {
            throw new UsernameNotFoundException("Username " + username + " not found!");
        }

        return ResponseEntity.ok(token);
    }

    private String generateHashPassword(String password) {

        PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder(
                "",
                8,
                185000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256
        );

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("pbkdf2", pbkdf2Encoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);

        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);

        return passwordEncoder.encode(password);
    }

    // CREATE
    public AccountCredentialsDTO create(AccountCredentialsDTO user) {

        if (user == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one new User!");

        var entity = new User();

        entity.setFullName(user.getFullname());
        entity.setUserName(user.getUsername());
        entity.setPassword(generateHashPassword(user.getPassword()));
        entity.setAccountNonExpired(true);
        entity.setAccountNonLocked(true);
        entity.setCredentialsNonExpired(true);
        entity.setEnabled(true);

        var dto = repository.save(entity);
        return new AccountCredentialsDTO(dto.getUsername(), dto.getPassword(), dto.getFullName());
    }


}
