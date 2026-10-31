package br.com.gabrieloliveira.transacoes.config;

import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.application.usecase.BuscarTransacaoService;
import br.com.gabrieloliveira.transacoes.application.usecase.CriarTransacaoService;
import br.com.gabrieloliveira.transacoes.application.usecase.EstornarTransacaoService;
import br.com.gabrieloliveira.transacoes.application.usecase.ListarTransacoesService;
import br.com.gabrieloliveira.transacoes.domain.service.ValidadorDeTransacao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// monta o nucleo da aplicacao: o dominio e os casos de uso sao java puro, sem @Service,
// entao sao criados aqui e recebem as implementacoes das portas de saida (adaptadores @Component)
// trocar um adaptador (ex: saldo falso -> api mock real) nao muda nada nesta classe
@Configuration
public class BeanConfig {

    @Bean
    public ValidadorDeTransacao validadorDeTransacao() {
        return new ValidadorDeTransacao();
    }

    @Bean
    public CriarTransacaoUseCase criarTransacaoUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                       SaldoContaPort saldoContaPort,
                                                       CambioPort cambioPort,
                                                       ValidadorDeTransacao validadorDeTransacao) {
        return new CriarTransacaoService(transacaoRepositoryPort, saldoContaPort, cambioPort, validadorDeTransacao);
    }

    @Bean
    public BuscarTransacaoUseCase buscarTransacaoUseCase(TransacaoRepositoryPort transacaoRepositoryPort) {
        return new BuscarTransacaoService(transacaoRepositoryPort);
    }

    @Bean
    public ListarTransacoesUseCase listarTransacoesUseCase(TransacaoRepositoryPort transacaoRepositoryPort) {
        return new ListarTransacoesService(transacaoRepositoryPort);
    }

    @Bean
    public EstornarTransacaoUseCase estornarTransacaoUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                             CriarTransacaoUseCase criarTransacaoUseCase) {
        return new EstornarTransacaoService(transacaoRepositoryPort, criarTransacaoUseCase);
    }
}
