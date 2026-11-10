package br.com.gabrieloliveira.usuarios.application.usecase;

import br.com.gabrieloliveira.usuarios.application.port.in.CriarUsuarioUseCase;
import br.com.gabrieloliveira.usuarios.application.port.in.ImportarUsuariosUseCase;
import br.com.gabrieloliveira.usuarios.domain.exception.RegraNegocioException;
import br.com.gabrieloliveira.usuarios.domain.model.LinhaImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.ResultadoImportacao;

import java.util.List;

// cadastro em massa: passa linha por linha e cadastra cada usuario valido
//
// regras do pedido 4 do PO:
// - uma linha invalida nao derruba a importacao: ela vira uma falha no resumo e a proxima linha segue
// - email (ou cpf) repetido e falha da linha, e nao erro do arquivo
//
// cada usuario e cadastrado pelo proprio CriarUsuarioUseCase, o mesmo do POST /users, entao as regras sao
// exatamente as do cadastro normal (email e cpf unicos, senha com bcrypt, data de criacao); ate um email
// repetido dentro do proprio arquivo e pego, porque quando a segunda linha chega a primeira ja foi salva
//
// cada usuario e salvo separado (sem uma transacao em volta de tudo): se a linha 12 falhar, as que ja
// entraram continuam salvas
public class ImportarUsuariosService implements ImportarUsuariosUseCase {

    private static final String MOTIVO_ERRO_INESPERADO =
            "nao foi possivel salvar esse usuario agora (erro inesperado ao gravar no banco)";

    private final CriarUsuarioUseCase criarUsuarioUseCase;

    public ImportarUsuariosService(CriarUsuarioUseCase criarUsuarioUseCase) {
        this.criarUsuarioUseCase = criarUsuarioUseCase;
    }

    @Override
    public ResultadoImportacao importar(List<LinhaImportacao> linhas) {
        ResultadoImportacao resultado = new ResultadoImportacao();

        for (LinhaImportacao linha : linhas) {
            // a linha ja veio com problema da leitura (campo vazio, email errado, coluna faltando...)
            if (!linha.isValida()) {
                resultado.registrarFalha(linha.getNumeroLinha(), linha.getErro());
                continue;
            }

            try {
                criarUsuarioUseCase.criar(linha.getUsuario());
                resultado.registrarImportado();
            } catch (RegraNegocioException e) {
                // email ou cpf ja cadastrado: falha so desta linha, com a mesma mensagem do cadastro normal
                resultado.registrarFalha(linha.getNumeroLinha(), e.getMessage());
            } catch (RuntimeException e) {
                // rede de seguranca: qualquer outro erro ao salvar (o unique do banco barrando um email cadastrado no
                // mesmo instante por outra requisicao, por exemplo) tambem vira falha da linha, para nao interromper
                // a importacao das linhas seguintes
                resultado.registrarFalha(linha.getNumeroLinha(), MOTIVO_ERRO_INESPERADO);
            }
        }

        return resultado;
    }
}
