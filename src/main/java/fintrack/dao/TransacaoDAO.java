package fintrack.dao;

import fintrack.exceptions.PersistenciaException;
import fintrack.model.TipoTransacao;
import fintrack.model.Transacao;
import fintrack.model.TransacaoMensal;
import fintrack.repository.Repositorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO (Data Access Object) da tabela {@code transacoes}.
 *
 * Implementa o contrato genérico {@link Repositorio}.
 * - Todas as consultas usam {@link PreparedStatement} — protege contra SQL Injection;
 * - Recursos JDBC são fechados com try-with-resources;
 * - {@link SQLException} é registrada no log e convertida em {@link PersistenciaException};
 * - {@link #salvarTodos(Collection)} usa transação com {@code commit()}/{@code rollback()}.
 */
public class TransacaoDAO implements Repositorio<Transacao, Long> {

    private static final Logger LOG = Logger.getLogger(TransacaoDAO.class.getName());

    private static final String COLUNAS =
            "id, descricao, valor, tipo, data, observacao, dia_recorrencia";

    private static final String SQL_INSERIR =
            "INSERT INTO transacoes (descricao, valor, tipo, data, observacao, dia_recorrencia) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_ATUALIZAR =
            "UPDATE transacoes SET descricao = ?, valor = ?, tipo = ?, data = ?, observacao = ?, "
                    + "dia_recorrencia = ? WHERE id = ?";
    private static final String SQL_REMOVER = "DELETE FROM transacoes WHERE id = ?";
    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUNAS + " FROM transacoes WHERE id = ?";
    private static final String SQL_LISTAR =
            "SELECT " + COLUNAS + " FROM transacoes ORDER BY data DESC, id DESC";
    private static final String SQL_LISTAR_PERIODO =
            "SELECT " + COLUNAS + " FROM transacoes WHERE data BETWEEN ? AND ? ORDER BY data DESC, id DESC";
    private static final String SQL_CONTAR = "SELECT COUNT(*) FROM transacoes";

    private final Conexao conexao;

    public TransacaoDAO(Conexao conexao) {
        this.conexao = Objects.requireNonNull(conexao, "conexao");
    }

    // ------------------------------------------------------------------ CREATE

    @Override
    public Transacao salvar(Transacao transacao) {
        Objects.requireNonNull(transacao, "A transação não pode ser nula!");
        try (Connection con = conexao.obterConexao()) {
            inserir(con, transacao);
            LOG.fine(() -> "Transação inserida: id=" + transacao.getId());
            return transacao;
        } catch (SQLException e) {
            throw erro("Erro ao inserir transação", e);
        }
    }

    /**
     * Insere todas as transações numa única transação de banco: ou todas são gravadas
     * ({@code commit}) ou nenhuma ({@code rollback}).
     */
    @Override
    public void salvarTodos(Collection<? extends Transacao> transacoes) {
        List<Transacao> inseridas = new ArrayList<>();
        try (Connection con = conexao.obterConexao()) {
            boolean autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                for (Transacao t : transacoes) {
                    Objects.requireNonNull(t, "A lista contém uma transação nula");
                    inserir(con, t);
                    inseridas.add(t);
                }
                con.commit();
                LOG.info(() -> inseridas.size() + " transações gravadas (commit)");
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                inseridas.forEach(t -> t.setId(null)); // ids gerados foram desfeitos
                LOG.log(Level.WARNING, "Rollback executado ao salvar lote de transações", e);
                throw e;
            } finally {
                con.setAutoCommit(autoCommitOriginal);
            }
        } catch (SQLException e) {
            throw erro("Erro ao salvar lote de transações", e);
        } catch (NullPointerException e) {
            throw new PersistenciaException("Lote inválido: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------- READ

    @Override
    public Optional<Transacao> buscarPorId(Long id) {
        try (Connection con = conexao.obterConexao();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw erro("Erro ao buscar transação id=" + id, e);
        }
    }

    @Override
    public List<Transacao> listarTodos() {
        try (Connection con = conexao.obterConexao();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            return mapearTodos(rs);
        } catch (SQLException e) {
            throw erro("Erro ao listar transações", e);
        }
    }

    /** Transações com data entre {@code inicio} e {@code fim} (inclusive). */
    public List<Transacao> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        try (Connection con = conexao.obterConexao();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR_PERIODO)) {
            ps.setString(1, inicio.toString());
            ps.setString(2, fim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return mapearTodos(rs);
            }
        } catch (SQLException e) {
            throw erro("Erro ao listar transações por período", e);
        }
    }

    @Override
    public int contar() {
        try (Connection con = conexao.obterConexao();
             PreparedStatement ps = con.prepareStatement(SQL_CONTAR);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw erro("Erro ao contar transações", e);
        }
    }

    // ------------------------------------------------------------------ UPDATE

    @Override
    public boolean atualizar(Transacao transacao) {
        Objects.requireNonNull(transacao, "A transação não pode ser nula!");
        if (transacao.getId() == null) {
            return false;
        }
        try (Connection con = conexao.obterConexao();
             PreparedStatement ps = con.prepareStatement(SQL_ATUALIZAR)) {
            preencherCampos(ps, transacao);
            ps.setLong(7, transacao.getId());
            boolean atualizado = ps.executeUpdate() == 1;
            LOG.fine(() -> "Atualização id=" + transacao.getId() + ": " + atualizado);
            return atualizado;
        } catch (SQLException e) {
            throw erro("Erro ao atualizar transação id=" + transacao.getId(), e);
        }
    }

    // ------------------------------------------------------------------ DELETE

    @Override
    public boolean removerPorId(Long id) {
        try (Connection con = conexao.obterConexao();
             PreparedStatement ps = con.prepareStatement(SQL_REMOVER)) {
            ps.setLong(1, id);
            boolean removido = ps.executeUpdate() == 1;
            LOG.fine(() -> "Remoção id=" + id + ": " + removido);
            return removido;
        } catch (SQLException e) {
            throw erro("Erro ao remover transação id=" + id, e);
        }
    }

    // -------------------------------------------------------------- auxiliares

    private void inserir(Connection con, Transacao transacao) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            preencherCampos(ps, transacao);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    transacao.setId(chaves.getLong(1));
                }
            }
        }
    }

    /** Parâmetros 1..6 comuns a INSERT e UPDATE. */
    private void preencherCampos(PreparedStatement ps, Transacao t) throws SQLException {
        ps.setString(1, t.getDescricao());
        ps.setString(2, t.getValor().toPlainString()); // texto: preserva a precisão decimal
        ps.setString(3, t.getTipo().name());
        ps.setString(4, t.getData().toString());       // ISO-8601: yyyy-MM-dd
        ps.setString(5, t.getObservacao());
        if (t instanceof TransacaoMensal mensal) {
            ps.setInt(6, mensal.getDiaDoMes());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
    }

    private List<Transacao> mapearTodos(ResultSet rs) throws SQLException {
        List<Transacao> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        return lista;
    }

    /** Converte a linha atual do ResultSet em {@link Transacao} ou {@link TransacaoMensal}. */
    private Transacao mapear(ResultSet rs) throws SQLException {
        String descricao = rs.getString("descricao");
        BigDecimal valor = new BigDecimal(rs.getString("valor"));
        TipoTransacao tipo = TipoTransacao.valueOf(rs.getString("tipo"));
        LocalDate data = LocalDate.parse(rs.getString("data"));
        String observacao = rs.getString("observacao");
        int dia = rs.getInt("dia_recorrencia");

        Transacao t = rs.wasNull()
                ? new Transacao(descricao, valor, tipo, data, observacao)
                : new TransacaoMensal(descricao, valor, tipo, data, observacao, dia);
        t.setId(rs.getLong("id"));
        return t;
    }

    private PersistenciaException erro(String mensagem, SQLException e) {
        LOG.log(Level.SEVERE, mensagem + " [SQLState=" + e.getSQLState() + "]", e);
        return new PersistenciaException(mensagem, e);
    }
}
