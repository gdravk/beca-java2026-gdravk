package br.com.gabrieloliveira.usuarios.application.port.in;

import br.com.gabrieloliveira.usuarios.domain.model.LinhaImportacao;
import br.com.gabrieloliveira.usuarios.domain.model.ResultadoImportacao;

import java.util.List;

// porta de entrada do cadastro em massa (pedido 4 do PO)
// repara que ela recebe as linhas ja lidas, e nao o arquivo csv: ler o csv e um detalhe do adaptador web
// assim, se um dia a importacao aceitar excel (bonus), so o leitor muda e este caso de uso continua igual
public interface ImportarUsuariosUseCase {

    ResultadoImportacao importar(List<LinhaImportacao> linhas);
}
