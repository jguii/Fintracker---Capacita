package fintrack.exceptions;

/**
 * Falha de infraestrutura ao acessar o banco de dados.
 * <p>
 * Envolve {@link java.sql.SQLException} em uma exceção não verificada para que
 * detalhes de JDBC não "vazem" pelas assinaturas das camadas de serviço e interface.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
