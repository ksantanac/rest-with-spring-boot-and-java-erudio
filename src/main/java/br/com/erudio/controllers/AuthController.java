package br.com.erudio.controllers;

import br.com.erudio.controllers.docs.AuthControllerDocs;
import br.com.erudio.data.dto.security.AccountCredentialsDTO;
import br.com.erudio.data.dto.v1.PersonDTO;
import br.com.erudio.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication Endpoint")
@RestController
@RequestMapping("/auth")
public class AuthController implements AuthControllerDocs {

    @Autowired
    AuthService service;

    // SINGNIN
    @PostMapping("/signin")
    @Override
    public ResponseEntity<?> signin(@RequestBody AccountCredentialsDTO credentials) {

        // 1. Validação das credenciais de entrada
        if (credentialsIsInvalid(credentials)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid client request!");
        }

        // 2. Tentativa de autenticação
        var token = service.signIn(credentials);

        // 3. Tratamento do resultado
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid client request!");
        }

        // 4. Retorno do token em caso de sucesso
        return token;
    }

    // REFRESH
    // Define o endpoint PUT para atualizar o token de um usuário autenticado
    @PutMapping("/refresh/{username}")
    @Override
    public ResponseEntity<?> refreshToken(
            @PathVariable("username") String username, // Recebe o nome de usuário da URL
            @RequestHeader("Authorization") String refreshToken // Recebe o refresh token do cabeçalho da requisição
    ) {

        // Valida se os parâmetros fornecidos são inválidos
        if (parametersAreInvalid(username, refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid client request!"); // Retorna status 403 (Forbidden) se os parâmetros forem inválidos
        }

        // Chama o serviço para obter um novo token com base no refresh token
        var token = service.refreshToken(username, refreshToken);

        // Se não houver um token válido, retorna erro
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid client request!"); // Retorna status 403 se não houver um token válido
        }

        // Retorna o novo token com status 200 OK
        return token;
    }

    // CREATE
    @PostMapping(
        value = "/createUser",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_YAML_VALUE
        },
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE,
            MediaType.APPLICATION_YAML_VALUE
        }
    )
    @Override
    public AccountCredentialsDTO create(@RequestBody AccountCredentialsDTO credentials) {
        return service.create(credentials);
    }

    private boolean parametersAreInvalid(String username, String refreshToken) {
        return StringUtils.isBlank(username) || StringUtils.isBlank(refreshToken);
    }

    // Método auxiliar para validar credenciais
    private static boolean credentialsIsInvalid(AccountCredentialsDTO credentials) {
        return credentials == null ||
                StringUtils.isBlank(credentials.getPassword()) ||
                StringUtils.isBlank(credentials.getUsername());
    }

}
