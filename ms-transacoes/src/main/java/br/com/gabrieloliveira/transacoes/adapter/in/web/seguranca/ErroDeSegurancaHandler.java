package br.com.gabrieloliveira.transacoes.adapter.in.web.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

// quando o spring security barra uma requisicao (sem login, login errado, ms1 fora), o erro acontece
// num filtro, antes do controller; essa classe entrega o erro pro GlobalExceptionHandler,
// pra resposta sair no mesmo json de erro do resto da api (igual no ms1)
@Component
public class ErroDeSegurancaHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public ErroDeSegurancaHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException erro) {
        handlerExceptionResolver.resolveException(request, response, null, erro);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException erro) {
        handlerExceptionResolver.resolveException(request, response, null, erro);
    }
}
