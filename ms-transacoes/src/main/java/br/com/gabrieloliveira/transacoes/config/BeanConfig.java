package br.com.gabrieloliveira.transacoes.config;

import br.com.gabrieloliveira.transacoes.application.port.in.AnalisarDespesasUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.BuscarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ConsultarSaldoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.CriarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.EstornarTransacaoUseCase;
import br.com.gabrieloliveira.transacoes.application.port.in.ListarTransacoesUseCase;
import br.com.gabrieloliveira.transacoes.application.port.out.CambioPort;
import br.com.gabrieloliveira.transacoes.application.port.out.SaldoContaPort;
import br.com.gabrieloliveira.transacoes.application.port.out.TransacaoRepositoryPort;
import br.com.gabrieloliveira.transacoes.application.usecase.AnalisarDespesasService;
import br.com.gabrieloliveira.transacoes.application.usecase.BuscarTransacaoService;
import br.com.gabrieloliveira.transacoes.application.usecase.ConsultarSaldoService;
import br.com.gabrieloliveira.transacoes.application.usecase.CriarTransacaoService;
import br.com.gabrieloliveira.transacoes.application.usecase.EstornarTransacaoService;
import br.com.gabrieloliveira.transacoes.application.usecase.ListarTransacoesService;
import br.com.gabrieloliveira.transacoes.application.usecase.PreenchedorDeCotacoes;
import br.com.gabrieloliveira.transacoes.domain.service.AnalisadorDeDespesas;
import br.com.gabrieloliveira.transacoes.domain.service.ValidadorDeTransacao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

// monta o nucleo da aplicacao: o dominio e os casos de uso sao java puro, sem @Service,
// entao sao criados aqui e recebem as implementacoes das portas de saida (adaptadores @Component)
// trocar um adaptador (ex: saldo falso -> api mock real) nao muda nada nesta classe
@Configuration
public class BeanConfig {

    // o relogio da aplicacao, sempre no fuso de brasilia
    // sem fixar o fuso, num container em utc o "hoje" e o "agora" seriam os de londres
    // (uma compra as 22h no brasil ja cairia no dia seguinte)
    // fica num bean pra que os testes possam usar um relogio parado no lugar dele
    @Bean
    public Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    public ValidadorDeTransacao validadorDeTransacao() {
        return new ValidadorDeTransacao();
    }

    // um preenchedor so, compartilhado pelos tres casos de uso que mostram cambio (criar, buscar e listar)
    // ele recebe a CambioPort, que hoje e implementada pelo BrasilApiCambioAdapter
    @Bean
    public PreenchedorDeCotacoes preenchedorDeCotacoes(CambioPort cambioPort) {
        return new PreenchedorDeCotacoes(cambioPort);
    }

    @Bean
    public CriarTransacaoUseCase criarTransacaoUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                       SaldoContaPort saldoContaPort,
                                                       PreenchedorDeCotacoes preenchedorDeCotacoes,
                                                       ValidadorDeTransacao validadorDeTransacao) {
        return new CriarTransacaoService(transacaoRepositoryPort, saldoContaPort, preenchedorDeCotacoes,
                validadorDeTransacao);
    }

    @Bean
    public BuscarTransacaoUseCase buscarTransacaoUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                         PreenchedorDeCotacoes preenchedorDeCotacoes) {
        return new BuscarTransacaoService(transacaoRepositoryPort, preenchedorDeCotacoes);
    }

    @Bean
    public ListarTransacoesUseCase listarTransacoesUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                           PreenchedorDeCotacoes preenchedorDeCotacoes) {
        return new ListarTransacoesService(transacaoRepositoryPort, preenchedorDeCotacoes);
    }

    @Bean
    public ConsultarSaldoUseCase consultarSaldoUseCase(SaldoContaPort saldoContaPort) {
        return new ConsultarSaldoService(saldoContaPort);
    }

    @Bean
    public AnalisadorDeDespesas analisadorDeDespesas() {
        return new AnalisadorDeDespesas();
    }

    // a analise usa o relogio (bean "relogio", la em cima) pra saber qual e o "mes atual"
    @Bean
    public AnalisarDespesasUseCase analisarDespesasUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                           AnalisadorDeDespesas analisadorDeDespesas,
                                                           Clock relogio) {
        return new AnalisarDespesasService(transacaoRepositoryPort, analisadorDeDespesas, relogio);
    }

    @Bean
    public EstornarTransacaoUseCase estornarTransacaoUseCase(TransacaoRepositoryPort transacaoRepositoryPort,
                                                             CriarTransacaoUseCase criarTransacaoUseCase) {
        return new EstornarTransacaoService(transacaoRepositoryPort, criarTransacaoUseCase);
    }
}
