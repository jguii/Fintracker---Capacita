package model;

import java.time.LocalDate;
import utils.Formatador;

public class Transacao {
    private String descricao;
    private double valor;
    private boolean ehReceita;
    private LocalDate data;

    public Transacao(String descricao, double valor, boolean ehReceita, LocalDate data) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição não pode estar vazia!");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero!");
        }
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula!");
        }
        this.descricao = descricao;
        this.valor = valor;
        this.ehReceita = ehReceita;
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isEhReceita() {
        return ehReceita;
    }

    public void setEhReceita(boolean ehReceita) {
        this.ehReceita = ehReceita;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - R$ %.2f - %s",
                Formatador.formatarTipo(ehReceita), descricao, valor, Formatador.formatarData(data));
    }
}
