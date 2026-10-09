package fintrack.model;

/**
 * Contrato para qualquer entidade que possua um identificador.
 * <p>
 * É o "elo" que permite que repositórios e serviços genéricos
 * ({@code Repositorio<T extends Identificavel<ID>, ID>}) operem sobre qualquer
 * tipo de registro sem conhecer sua classe concreta.
 *
 * @param <ID> tipo do identificador (Long, Integer, UUID, String...)
 */
public interface Identificavel<ID> {

    ID getId();

    void setId(ID id);
}
