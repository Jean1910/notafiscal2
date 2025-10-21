package Pck_Control;

import Pck_Persistencia.PedidoPersistencia;

public class PedidoControl {
    private final PedidoPersistencia persist = new PedidoPersistencia();
    private int a02CodigoAtual = -1;

    public int criarPedido(int a01CodigoCliente) {
        a02CodigoAtual = persist.criarPedido(a01CodigoCliente);
        return a02CodigoAtual;
    }

    public void adicionarItem(int a03CodigoProduto, int qtde) {
        if (a02CodigoAtual <= 0) throw new IllegalStateException("Crie o pedido primeiro.");
        persist.inserirItem(a02CodigoAtual, a03CodigoProduto, qtde);
    }

    public void finalizar() {
        if (a02CodigoAtual <= 0) throw new IllegalStateException("Crie o pedido primeiro.");
        persist.atualizarTotal(a02CodigoAtual);
    }

    public int getA02CodigoAtual() { return a02CodigoAtual; }
}
