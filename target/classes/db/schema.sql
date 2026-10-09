-- =====================================================================
-- FinTrack - criação da base de dados (SQLite)
-- Executado automaticamente por Conexao.inicializarBanco() na
-- inicialização da aplicação (idempotente: IF NOT EXISTS).
-- =====================================================================

CREATE TABLE IF NOT EXISTS transacoes (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    descricao       TEXT    NOT NULL CHECK (length(trim(descricao)) > 0),
    valor           NUMERIC NOT NULL CHECK (valor > 0),
    tipo            TEXT    NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    data            TEXT    NOT NULL,
    observacao      TEXT,
    dia_recorrencia INTEGER CHECK (dia_recorrencia BETWEEN 1 AND 31),
    criado_em       TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- data no formato ISO-8601 (yyyy-MM-dd): ordenação e BETWEEN funcionam como texto
CREATE INDEX IF NOT EXISTS idx_transacoes_data ON transacoes (data);
