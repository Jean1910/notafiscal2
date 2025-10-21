package Pck_Model;

import java.math.BigDecimal;

public class ItemNota {
    private int idProduto;
    private String descricao;
    private int qtde;
    private BigDecimal valorUnit;
    private BigDecimal valorTotal;

    public ItemNota() {}

    public ItemNota(int idProduto, String descricao, int qtde, BigDecimal valorUnit, BigDecimal valorTotal) {
        this.idProduto = idProduto; this.descricao = descricao; this.qtde = qtde;
        this.valorUnit = valorUnit; this.valorTotal = valorTotal;
    }

    public int getIdProduto() { return idProduto; }
    public String getDescricao() { return descricao; }
    public int getQtde() { return qtde; }
    public BigDecimal getValorUnit() { return valorUnit; }
    public BigDecimal getValorTotal() { return valorTotal; }
}
