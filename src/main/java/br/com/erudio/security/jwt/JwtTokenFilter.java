package br.com.erudio.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class JwtTokenFilter extends GenericFilterBean {

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtTokenFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter)
            throws IOException, ServletException {

        // Extrai o token do cabeçalho da requisição HTTP
        var token = tokenProvider.resolveToken((HttpServletRequest) request);

        // Verifica se o token não está em branco e se é válido
        if (StringUtils.isNotBlank(token) && tokenProvider.validateToken(token)) {

            // Obtém a autenticação do token
            Authentication authentication = tokenProvider.getAuthentication(token);

            // Se a autenticação for válida, define-a no contexto de segurança do Spring
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continua a cadeia de filtros
        filter.doFilter(request, response);
    }

}
