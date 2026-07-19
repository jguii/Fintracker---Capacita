package controller;

import model.Transacao;
import exceptions.EntradaInvalidaException;

import java.util.ArrayList;
import java.util.List;

public class FinTracker {
    private List<Transacao> transacoes;

    public FinTracker() {
        this.transacoes = new ArrayList<>();
    }

    public void adicionarTransacao(Transacao transacao) throws EntradaInvalidaException {
        if (transacao == null) {
            throw new EntradaInvalidaException("Transação não pode ser nula!");
        }
        if (transacao.getValor() <= 0) {
            throw new EntradaInvalidaException("O valor da transação deve ser maior que zero!");
        }
        if (transacao.getDescricao() == null || transacao.getDescricao().trim().isEmpty()) {
            throw new EntradaInvalidaException("A descrição não pode estar vazia!");
        }
        transacoes.add(transacao);
    }

    public List<Transacao> listarTransacoes() {
        return new ArrayList<>(transacoes);
    }

    public boolean removerTransacao(int indice) throws EntradaInvalidaException {
        if (indice < 0 || indice >= transacoes.size()) {
            throw new EntradaInvalidaException("Índice inválido! Selecione uma transação válida.");
        }
        transacoes.remove(indice);
        return true;
    }

    public double calcularSaldoTotal() {
        double saldo = 0;
        for (Transacao t : transacoes) {
            if (t.isEhReceita()) {
                saldo += t.getValor();
            } else {
                saldo -= t.getValor();
            }
        }
        return saldo;
    }

    public int getTotalTransacoes() {
        return transacoes.size();
    }
}
