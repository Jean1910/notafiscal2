package Pck_Persistencia;

import Pck_DAO.ConexaoMySql;
import Pck_Model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientePersistencia {
    private final ConexaoMySql conn = new ConexaoMySql();

    public List<Cliente> listar() {
        List<Cliente> list = new ArrayList<>();
        String sql = "SELECT A01_codigo, A01_nome FROM CLIENTE_01 ORDER BY A01_nome";
        try (Connection c = conn.getConexao();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Cliente(rs.getInt("A01_codigo"), rs.getString("A01_nome")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        finally { conn.desconectar(); }
        return list;
    }
}
