package br.com.gabrieloliveira.usuarios.adapter.in.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

// o que acontece quando o spring security barra uma requisicao (401 sem login, 403 sem permissao)
//
// o problema: a seguranca roda num "filtro", ANTES de a requisicao chegar no controller, entao o
// GlobalExceptionHandler nao enxerga esses erros sozinho e a resposta sairia num formato diferente
// a solucao: esta classe entrega o erro para o HandlerExceptionResolver do spring mvc, que e quem
// chama o GlobalExceptionHandler; assim o 401 e o 403 saem no mesmo json de todos os outros erros
@Component
public class ErroDeSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    // o spring tem mais de um HandlerExceptionResolver; o @Qualifier escolhe o principal, que e o que
    // conhece os @ExceptionHandler do GlobalExceptionHandler
    public ErroDeSegurancaHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    // chamado quando nao veio login ou o login esta errado (401)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException erro) {
        handlerExceptionResolver.resolveException(request, response, null, erro);
    }

    // chamado quando a pessoa esta logada, mas nao tem permissao (403)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException erro) {
        handlerExceptionResolver.resolveException(request, response, null, erro);
    }
}
