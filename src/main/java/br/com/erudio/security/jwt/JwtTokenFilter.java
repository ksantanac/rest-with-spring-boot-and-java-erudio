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

        // 1. Extrai o token JWT do cabeçalho da requisição
        var token = tokenProvider.resolveToken((HttpServletRequest) request);

        // 2. Se existir token e for válido
        if (StringUtils.isNotBlank(token) && tokenProvider.validateToken(token)) {

            // 3. Obtém a autenticação do usuário a partir do token
            Authentication authentication = tokenProvider.getAuthentication(token);

            // 4. Se a autenticação for válida, define no contexto de segurança
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 5. Continua a cadeia de filtros
        filter.doFilter(request, response);
    }

}
