package br.com.gabrieloliveira.transacoes.adapter.out.ms1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// o json que o ms1 devolve no POST /auth/verificar quando o login esta certo
// so precisamos do usuarioId; o @JsonIgnoreProperties faz o jackson ignorar os outros campos (nome, email)
// em vez de dar erro por nao conhecer eles
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioVerificadoMs1DTO {

    private Long usuarioId;

    public UsuarioVerificadoMs1DTO() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
