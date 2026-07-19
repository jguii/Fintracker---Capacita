package model;

import java.time.LocalDate;
import utils.Formatador;

public class TransacaoMensal extends Transacao {
    private int diaDoMes;

    public TransacaoMensal(String descricao, double valor, boolean ehReceita, LocalDate data, int diaDoMes) {
        super(descricao, valor, ehReceita, data);
        if (diaDoMes < 1 || diaDoMes > 31) {
            throw new IllegalArgumentException("O dia do mês deve estar entre 1 e 31!");
        }
        this.diaDoMes = diaDoMes;
    }

    public int getDiaDoMes() {
        return diaDoMes;
    }

    public void setDiaDoMes(int diaDoMes) {
        this.diaDoMes = diaDoMes;
    }

    @Override
    public String toString() {
        String tipo = Formatador.formatarTipo(isEhReceita()) + " Mensal";
        return String.format("[%s] %s - R$ %.2f - %s - Dia %d do mês",
                tipo, getDescricao(), getValor(), Formatador.formatarData(getData()), diaDoMes);
    }
}
