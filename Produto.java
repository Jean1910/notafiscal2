package Pck_Model;

import java.math.BigDecimal;

public class Produto {
    private int id;                 // A03_codigo
    private String descricao;       // A03_descricao
    private BigDecimal valorUnit;   // A03_valor_unitario

    public Produto() {}
    public Produto(int id, String descricao, BigDecimal valorUnit) {
        this.id = id; this.descricao = descricao; this.valorUnit = valorUnit;
    }

    public int getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorUnit() { return valorUnit; }

    @Override public String toString() { return descricao; }
}
