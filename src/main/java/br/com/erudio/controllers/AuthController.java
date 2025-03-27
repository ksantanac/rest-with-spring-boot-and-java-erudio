package br.com.erudio.controllers;

import br.com.erudio.data.dto.security.AccountCredentialsDTO;
import br.com.erudio.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication Endpoint")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService service;

    @Operation(summary = "Autenticates an user and returns a token.")
    @PostMapping("/signin")
    public ResponseEntity<?> signin(AccountCredentialsDTO credentials) {

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
        return ResponseEntity.ok().body(token);
    }

    // Método auxiliar para validar credenciais
    private static boolean credentialsIsInvalid(AccountCredentialsDTO credentials) {
        return credentials == null ||
                StringUtils.isBlank(credentials.getPassword()) ||
                StringUtils.isBlank(credentials.getUsername());
    }

}
