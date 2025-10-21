package Pck_Persistencia;

import Pck_DAO.ConexaoMySql;
import java.sql.*;

public class PedidoPersistencia {
    private final ConexaoMySql conn = new ConexaoMySql();

    // cria pedido -> retorna A02_codigo
    public int criarPedido(int a01CodigoCliente) {
        int idPedido = -1;
        String call = "{ CALL PROC_CRIA_PEDIDO(?, ?) }";
        try (Connection c = conn.getConexao();
             CallableStatement cs = c.prepareCall(call)) {
            cs.setInt(1, a01CodigoCliente);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            idPedido = cs.getInt(2);
        } catch (SQLException e) { e.printStackTrace(); }
        finally { conn.desconectar(); }
        return idPedido;
    }

    public void inserirItem(int a02CodigoPedido, int a03CodigoProduto, int qtde) {
        String call = "{ CALL PROC_INSERE_ITEM_PEDIDO(?, ?, ?) }";
        try (Connection c = conn.getConexao();
             CallableStatement cs = c.prepareCall(call)) {
            cs.setInt(1, a02CodigoPedido);
            cs.setInt(2, a03CodigoProduto);
            cs.setInt(3, qtde);
            cs.execute();
        } catch (SQLException e) { e.printStackTrace(); }
        finally { conn.desconectar(); }
    }

    public void atualizarTotal(int a02CodigoPedido) {
        String call = "{ CALL PROC_ATUALIZA_TOTAL_PEDIDO(?) }";
        try (Connection c = conn.getConexao();
             CallableStatement cs = c.prepareCall(call)) {
            cs.setInt(1, a02CodigoPedido);
            cs.execute();
        } catch (SQLException e) { e.printStackTrace(); }
        finally { conn.desconectar(); }
    }
}
