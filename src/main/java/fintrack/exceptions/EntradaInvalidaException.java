package fintrack.exceptions;

/**
 * Erro de validação de regra de negócio (valor inválido, descrição vazia,
 * registro inexistente...). Exceção verificada: obriga a camada de interface
 * a tratar e exibir uma mensagem amigável ao usuário.
 */
public class EntradaInvalidaException extends Exception {

    public EntradaInvalidaException(String mensagem) {
        super(mensagem);
    }

    public EntradaInvalidaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
