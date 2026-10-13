-- =====================================================================
-- FinTrack - criação da base de dados (MySQL)
-- Executado automaticamente por Conexao.inicializarBanco() na
-- inicialização da aplicação (idempotente: IF NOT EXISTS).
-- =====================================================================

CREATE TABLE IF NOT EXISTS transacoes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao       VARCHAR(100) NOT NULL,
    valor           DECIMAL(15, 2) NOT NULL,
    tipo            VARCHAR(10)  NOT NULL,
    data            VARCHAR(10)  NOT NULL,
    observacao      TEXT,
    dia_recorrencia INT,
    criado_em       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transacoes_data (data)
);
