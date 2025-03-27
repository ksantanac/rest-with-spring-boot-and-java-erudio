package br.com.erudio.services;

import br.com.erudio.data.dto.security.AccountCredentialsDTO;
import br.com.erudio.data.dto.security.TokenDTO;
import br.com.erudio.repository.UserRepository;
import br.com.erudio.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

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
        // Se falhar, lança AuthenticationException (BadCredentialsException)

        // 2. Busca o usuário no banco de dados
        var user = repository.findByUsername(credentials.getUsername());

        // Verifica se usuário foi encontrado (isso normalmente não deveria acontecer se a autenticação passou)
        if (user == null) {
            throw new UsernameNotFoundException(
                    "Username " + credentials.getUsername() + " not found!"
            );
        }

        // 3. Gera os tokens JWT para o usuário autenticado
        var token = tokenProvider.createAcessToken(
                credentials.getUsername(), // Nome de usuário
                user.getRoles()           // Roles/permissões do usuário
        );

        // 4. Retorna os tokens com status HTTP 200 (OK)
        return ResponseEntity.ok(token);

    }

}
