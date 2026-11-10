package br.com.gabrieloliveira.transacoes.domain.model;

import br.com.gabrieloliveira.transacoes.domain.exception.ParametroInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// testes de como o periodo da analise e do relatorio e decidido
// o "hoje" e sempre fixo (5 de novembro de 2026), para o resultado nao mudar conforme o dia em que o teste roda
class PeriodoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 5);

    @Test
    void semDatasUsaOMesAtualInteiro() {
        // criterio do PO: "sem informar periodo, recebo o mes atual"
        Periodo periodo = Periodo.resolver(null, null, HOJE);

        assertEquals(LocalDate.of(2026, 11, 1), periodo.getInicio());
        assertEquals(LocalDate.of(2026, 11, 30), periodo.getFim());
    }

    @Test
    void mesAtualEmFevereiroDeAnoBissextoTerminaNoDia29() {
        Periodo periodo = Periodo.resolver(null, null, LocalDate.of(2028, 2, 10));

        assertEquals(LocalDate.of(2028, 2, 29), periodo.getFim());
    }

    @Test
    void comAsDuasDatasUsaExatamenteOQueVeio() {
        Periodo periodo = Periodo.resolver(LocalDate.of(2026, 9, 15), LocalDate.of(2026, 10, 20), HOJE);

        assertEquals(LocalDate.of(2026, 9, 15), periodo.getInicio());
        assertEquals(LocalDate.of(2026, 10, 20), periodo.getFim());
    }

    @Test
    void soComInicioVaiAteOFimDoMesAtual() {
        Periodo periodo = Periodo.resolver(LocalDate.of(2026, 10, 10), null, HOJE);

        assertEquals(LocalDate.of(2026, 10, 10), periodo.getInicio());
        assertEquals(LocalDate.of(2026, 11, 30), periodo.getFim());
    }

    @Test
    void soComFimComecaNoDia1DoMesDoFim() {
        Periodo periodo = Periodo.resolver(null, LocalDate.of(2026, 9, 20), HOJE);

        assertEquals(LocalDate.of(2026, 9, 1), periodo.getInicio());
        assertEquals(LocalDate.of(2026, 9, 20), periodo.getFim());
    }

    @Test
    void inicioDepoisDoFimEParametroInvalido() {
        ParametroInvalidoException erro = assertThrows(ParametroInvalidoException.class,
                () -> Periodo.resolver(LocalDate.of(2026, 11, 30), LocalDate.of(2026, 11, 1), HOJE));

        assertEquals("a data inicial (2026-11-30) nao pode ser depois da data final (2026-11-01)", erro.getMessage());
    }

    @Test
    void inicioEFimNoMesmoDiaEPermitido() {
        Periodo periodo = Periodo.resolver(HOJE, HOJE, HOJE);

        assertEquals(HOJE, periodo.getInicio());
        assertEquals(HOJE, periodo.getFim());
    }

    @Test
    void limitesParaOBancoCobremOUltimoDiaInteiro() {
        Periodo periodo = Periodo.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), HOJE);

        // de 00:00 do dia 1 ate (sem incluir) 00:00 do dia 1 de dezembro: o dia 30 entra inteiro
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), periodo.inicioDoPrimeiroDia());
        assertEquals(LocalDateTime.of(2026, 12, 1, 0, 0), periodo.inicioDoDiaSeguinteAoFim());
    }
}
