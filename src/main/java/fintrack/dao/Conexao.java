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
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ponto único de acesso ao banco de dados MySQL (e H2 em memória nos testes).
 *
 * Centraliza a URL JDBC, credenciais, verificação do driver e criação do schema.
 * As credenciais são carregadas do arquivo {@code db.properties} ou de propriedades do sistema.
 */
public final class Conexao implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(Conexao.class.getName());

    public static final String PROPRIEDADE_URL = "fintrack.db.url";
    public static final String PROPRIEDADE_USER = "fintrack.db.user";
    public static final String PROPRIEDADE_PASSWORD = "fintrack.db.password";

    private static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
    private static final String DRIVER_H2 = "org.h2.Driver";
    private static final String SCRIPT_SCHEMA = "/db/schema.sql";

    private final String url;
    private final String usuario;
    private final String senha;

    /**
     * No H2 em memória, uma conexão âncora mantém o banco vivo entre operações.
     */
    private Connection conexaoAncora;

    private Conexao(String url, String usuario, String senha, boolean manterAberta) {
        verificarDriver(url);
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
        if (manterAberta) {
            try {
                this.conexaoAncora = obterConexao();
            } catch (SQLException e) {
                throw new PersistenciaException("Não foi possível abrir o banco em memória", e);
            }
        }
    }

    /** Banco padrão da aplicação: lê do arquivo db.properties ou propriedades do sistema. */
    public static Conexao padrao() {
        Properties config = carregarConfiguracao();

        String url = System.getProperty(PROPRIEDADE_URL, config.getProperty("db.url",
                "jdbc:mysql://localhost:3306/fintrack?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8"));
        String usuario = System.getProperty(PROPRIEDADE_USER, config.getProperty("db.user", "root"));
        String senha = System.getProperty(PROPRIEDADE_PASSWORD, config.getProperty("db.password", ""));

        return new Conexao(url, usuario, senha, false);
    }

    /** Conexão explícita com credenciais. */
    public static Conexao paraUrl(String url, String usuario, String senha) {
        return new Conexao(url, usuario, senha, false);
    }

    /**
     * Banco H2 em memória com modo de compatibilidade MySQL:
     * limpo, isolado e ultrarrápido para suíte de testes unitários.
     */
    public static Conexao emMemoria() {
        String nome = "fintrack_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + nome + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE";
        return new Conexao(url, "sa", "", true);
    }

    public Connection obterConexao() throws SQLException {
        if (usuario != null) {
            return DriverManager.getConnection(url, usuario, senha);
        }
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
                String sql = comando.trim();
                if (!sql.isBlank()) {
                    st.execute(sql);
                }
            }
            LOG.info(() -> "Banco de dados inicializado com sucesso: " + url);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao inicializar o banco de dados", e);
            if (e.getErrorCode() == 1045 || (e.getMessage() != null && e.getMessage().contains("Access denied"))) {
                throw new PersistenciaException("Falha de autenticação no MySQL. Verifique o usuário e a senha no arquivo 'db.properties'.", e);
            }
            if (e.getErrorCode() == 1049 || (e.getMessage() != null && e.getMessage().contains("Unknown database"))) {
                throw new PersistenciaException("O banco de dados 'fintrack' não foi encontrado no MySQL. Execute 'CREATE DATABASE fintrack;' no MySQL.", e);
            }
            throw new PersistenciaException("Falha ao inicializar o banco de dados: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        if (conexaoAncora != null) {
            try {
                conexaoAncora.close();
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Erro ao fechar a conexão âncora", e);
            } finally {
                conexaoAncora = null;
            }
        }
    }

    // ------------------------------------------------------------- auxiliares

    private static void verificarDriver(String url) {
        String driver = url.startsWith("jdbc:h2:") ? DRIVER_H2 : DRIVER_MYSQL;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException("Driver JDBC não encontrado no classpath: " + driver, e);
        }
    }

    private static Properties carregarConfiguracao() {
        Properties props = new Properties();
        Path arquivo = Path.of("db.properties");
        if (Files.exists(arquivo)) {
            try (InputStream in = Files.newInputStream(arquivo)) {
                props.load(in);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Não foi possível ler o arquivo db.properties", e);
            }
        }
        return props;
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
