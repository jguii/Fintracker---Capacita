package fintrack.dao;

import fintrack.exceptions.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ponto único de acesso ao banco de dados.
 * <p>
 * Centraliza a URL JDBC, a verificação do driver e a criação do schema.
 * Cada operação do DAO obtém uma {@link Connection} nova via {@link #obterConexao()}
 * e a fecha com try-with-resources — evitando vazamento de conexões.
 *
 * <ul>
 *   <li>{@link #padrao()} — arquivo {@code ~/.fintrack/fintrack.db}
 *       (ou a URL da propriedade de sistema {@value #PROPRIEDADE_URL});</li>
 *   <li>{@link #emMemoria()} — SQLite em memória, isolado, ideal para testes.</li>
 * </ul>
 */
public final class Conexao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(Conexao.class.getName());

    /** Permite apontar para outro banco: {@code -Dfintrack.db.url=jdbc:sqlite:C:/dados/fin.db}. */
    public static final String PROPRIEDADE_URL = "fintrack.db.url";
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String SCRIPT_SCHEMA = "/db/schema.sql";

    private final String url;

    /**
     * Um banco SQLite em memória existe apenas enquanto houver uma conexão aberta.
     * Esta conexão "âncora" mantém o banco vivo entre as operações do DAO.
     */
    private Connection conexaoAncora;

    private Conexao(String url, boolean manterAberta) {
        verificarDriver();
        this.url = url;
        if (manterAberta) {
            try {
                this.conexaoAncora = DriverManager.getConnection(url);
            } catch (SQLException e) {
                throw new PersistenciaException("Não foi possível abrir o banco em memória", e);
            }
        }
    }

    /** Banco padrão da aplicação (arquivo local). */
    public static Conexao padrao() {
        String url = System.getProperty(PROPRIEDADE_URL);
        if (url == null || url.isBlank()) {
            Path arquivo = Path.of(System.getProperty("user.home"), ".fintrack", "fintrack.db");
            criarDiretorio(arquivo.getParent());
            url = "jdbc:sqlite:" + arquivo.toAbsolutePath();
        }
        return new Conexao(url, false);
    }

    /** Banco a partir de uma URL JDBC explícita. */
    public static Conexao paraUrl(String url) {
        return new Conexao(url, false);
    }

    /**
     * Banco SQLite em memória com cache compartilhado e nome único:
     * cada instância é um banco limpo e isolado (perfeito para {@code @BeforeEach}).
     */
    public static Conexao emMemoria() {
        String nome = "fintrack-" + UUID.randomUUID();
        return new Conexao("jdbc:sqlite:file:" + nome + "?mode=memory&cache=shared", true);
    }

    public Connection obterConexao() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public String getUrl() {
        return url;
    }

    /** Executa o script {@code db/schema.sql} (idempotente). */
    public void inicializarBanco() {
        String script = lerScript();
        try (Connection con = obterConexao(); Statement st = con.createStatement()) {
            for (String comando : script.split(";")) {
                if (!comando.isBlank()) {
                    st.execute(comando);
                }
            }
            LOG.info(() -> "Banco de dados inicializado: " + url);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao criar o schema do banco", e);
            throw new PersistenciaException("Falha ao inicializar o banco de dados", e);
        }
    }

    @Override
    public void close() {
        if (conexaoAncora != null) {
            try {
                conexaoAncora.close();
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Erro ao fechar a conexão em memória", e);
            } finally {
                conexaoAncora = null;
            }
        }
    }

    // ------------------------------------------------------------- auxiliares

    private static void verificarDriver() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException("Driver JDBC não encontrado no classpath: " + DRIVER, e);
        }
    }

    private static void criarDiretorio(Path diretorio) {
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível criar o diretório " + diretorio, e);
        }
    }

    /** Lê o script e remove comentários "--" para facilitar a divisão por ";". */
    private static String lerScript() {
        try (InputStream in = Conexao.class.getResourceAsStream(SCRIPT_SCHEMA)) {
            if (in == null) {
                throw new IllegalStateException("Script não encontrado: " + SCRIPT_SCHEMA);
            }
            String conteudo = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return conteudo.replaceAll("--[^\\r\\n]*", "");
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao ler " + SCRIPT_SCHEMA, e);
        }
    }
}
