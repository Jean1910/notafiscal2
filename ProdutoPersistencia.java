package Pck_Persistencia;

import Pck_DAO.ConexaoMySql;
import Pck_Model.Produto;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class ProdutoPersistencia {
    private final ConexaoMySql conn = new ConexaoMySql();

    public List<Produto> listar() {
        List<Produto> list = new ArrayList<>();
        String sql = "SELECT A03_codigo, A03_descricao, A03_valor_unitario FROM PRODUTO_03 ORDER BY A03_descricao";
        try (Connection c = conn.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Produto(
                    rs.getInt("A03_codigo"),
                    rs.getString("A03_descricao"),
                    rs.getBigDecimal("A03_valor_unitario")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { conn.desconectar(); }
        return list;
    }
}
