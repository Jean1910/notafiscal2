# notafiscal2

Show, Fernando — seu diagrama do brModelo (CLIENTE_01, PEDIDO_02, PRODUTO_03, ITEM_04) está claro.
Abaixo deixo tudo alinhado ao seu MER: SQL (DDL + procedures) com os mesmos nomes de tabelas e colunas do desenho, e o Java (MVC + DAO) já apontando para esses nomes. É só copiar e colar.

🗄️ SQL (compatível com o seu MER)

Cole tudo numa aba SQL do Workbench e execute.

USE BD_MVC_DAO;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS ITEM_04;
DROP TABLE IF EXISTS PEDIDO_02;
DROP TABLE IF EXISTS PRODUTO_03;
DROP TABLE IF EXISTS CLIENTE_01;
SET FOREIGN_KEY_CHECKS = 1;

-- CLIENTE_01
CREATE TABLE CLIENTE_01 (
  A01_codigo    INT NOT NULL AUTO_INCREMENT,
  A01_nome      VARCHAR(100) NOT NULL,
  A01_endereco  VARCHAR(120),
  A01_telefone  CHAR(15),
  A01_cpf       CHAR(14),
  A01_credito   DECIMAL(12,2) DEFAULT 0.00,
  PRIMARY KEY (A01_codigo)
);

-- PRODUTO_03
CREATE TABLE PRODUTO_03 (
  A03_codigo         INT NOT NULL AUTO_INCREMENT,
  A03_descricao      VARCHAR(100) NOT NULL,
  A03_valor_unitario DECIMAL(10,2) NOT NULL,
  A03_estoque        INT NOT NULL DEFAULT 0,
  PRIMARY KEY (A03_codigo)
);

-- PEDIDO_02 (cabeçalho)
CREATE TABLE PEDIDO_02 (
  A02_codigo      INT NOT NULL AUTO_INCREMENT,
  A02_data        DATE NOT NULL DEFAULT (CURRENT_DATE),
  A02_valor_total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  A01_codigo      INT NOT NULL,
  PRIMARY KEY (A02_codigo),
  CONSTRAINT fk_pedido_cliente
    FOREIGN KEY (A01_codigo) REFERENCES CLIENTE_01 (A01_codigo)
);

-- ITEM_04 (itens do pedido)
CREATE TABLE ITEM_04 (
  A04_codigo      INT NOT NULL AUTO_INCREMENT,
  A02_codigo      INT NOT NULL,
  A03_codigo      INT NOT NULL,
  A04_quantidade  INT NOT NULL,
  A04_valor_item  DECIMAL(12,2) NOT NULL,  -- total do item (qtde * unit)
  PRIMARY KEY (A04_codigo),
  CONSTRAINT fk_item_pedido
    FOREIGN KEY (A02_codigo) REFERENCES PEDIDO_02 (A02_codigo) ON DELETE CASCADE,
  CONSTRAINT fk_item_produto
    FOREIGN KEY (A03_codigo) REFERENCES PRODUTO_03 (A03_codigo)
);

-- DADOS EXEMPLO
INSERT INTO CLIENTE_01 (A01_nome, A01_endereco, A01_telefone, A01_cpf, A01_credito) VALUES
('Fernando', 'Rua A, 100', '11999990000', '000.000.000-00', 500.00),
('Bruno',    'Rua B, 200', '11988880000', '111.111.111-11', 300.00);

INSERT INTO PRODUTO_03 (A03_descricao, A03_valor_unitario, A03_estoque) VALUES
('Teclado Mecânico', 250.00,  10),
('Mouse Gamer',      150.00,  20),
('Monitor 24"',      899.90,   5);

-- =======================
-- PROCEDURES (nomeadas para o seu MER)
-- =======================
DELIMITER $$

-- 1) Criar pedido: IN cliente -> OUT id do pedido
DROP PROCEDURE IF EXISTS PROC_CRIA_PEDIDO $$
CREATE PROCEDURE PROC_CRIA_PEDIDO(IN p_A01_codigo INT, OUT p_A02_codigo INT)
BEGIN
  INSERT INTO PEDIDO_02 (A01_codigo, A02_valor_total)
  VALUES (p_A01_codigo, 0.00);
  SET p_A02_codigo = LAST_INSERT_ID();
END $$

-- 2) Inserir item: valida estoque, calcula total do item e baixa estoque
DROP PROCEDURE IF EXISTS PROC_INSERE_ITEM_PEDIDO $$
CREATE PROCEDURE PROC_INSERE_ITEM_PEDIDO(IN p_A02_codigo INT, IN p_A03_codigo INT, IN p_qtde INT)
BEGIN
  DECLARE v_unit DECIMAL(10,2);
  DECLARE v_total DECIMAL(12,2);
  DECLARE v_estoque INT;

  SELECT A03_valor_unitario, A03_estoque INTO v_unit, v_estoque
    FROM PRODUTO_03 WHERE A03_codigo = p_A03_codigo;

  IF v_unit IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Produto inexistente';
  END IF;
  IF p_qtde <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Quantidade inválida';
  END IF;
  IF v_estoque < p_qtde THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Estoque insuficiente';
  END IF;

  SET v_total = v_unit * p_qtde;

  INSERT INTO ITEM_04 (A02_codigo, A03_codigo, A04_quantidade, A04_valor_item)
  VALUES (p_A02_codigo, p_A03_codigo, p_qtde, v_total);

  UPDATE PRODUTO_03 SET A03_estoque = A03_estoque - p_qtde
   WHERE A03_codigo = p_A03_codigo;
END $$

-- 3) Atualizar total do pedido somando total dos itens
DROP PROCEDURE IF EXISTS PROC_ATUALIZA_TOTAL_PEDIDO $$
CREATE PROCEDURE PROC_ATUALIZA_TOTAL_PEDIDO(IN p_A02_codigo INT)
BEGIN
  UPDATE PEDIDO_02 p
     SET p.A02_valor_total = (
       SELECT IFNULL(SUM(A04_valor_item),0) FROM ITEM_04 i
        WHERE i.A02_codigo = p_A02_codigo
     )
   WHERE p.A02_codigo = p_A02_codigo;
END $$

DELIMITER ;

💻 Java (apontando exatamente para as tabelas/colunas do MER)

Pré-requisitos:
• mysql-connector-j-*.jar no Classpath
• Ajuste sua senha em ConexaoMySql
• module-info.java com requires java.desktop; requires java.sql;

module-info.java
module Prj_Exemplo_MVC_Persistencia_DAO {
    requires java.desktop;
    requires java.sql;
    // se estiver no Modulepath:
    // requires com.mysql.cj;
}

Pck_DAO/ConexaoMySql.java
package Pck_DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySql {
    private Connection oConn;

    private static final String URL =
        "jdbc:mysql://localhost:3306/BD_MVC_DAO?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "SUA_SENHA_AQUI";

    public Connection getConexao() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            oConn = DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oConn;
    }

    public void desconectar() {
        try {
            if (oConn != null && !oConn.isClosed()) oConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

Pck_Model/Cliente.java
package Pck_Model;

public class Cliente {
    private int    codigo;     // A01_codigo
    private String nome;       // A01_nome

    public Cliente() {}
    public Cliente(int codigo, String nome) { this.codigo = codigo; this.nome = nome; }

    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }

    @Override public String toString() { return nome; }
}

Pck_Model/Produto.java
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

Pck_Persistencia/ClientePersistencia.java
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

Pck_Persistencia/ProdutoPersistencia.java
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

Pck_Persistencia/PedidoPersistencia.java
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

Pck_Control/PedidoControl.java
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

Pck_View/PedidoView.java
package Pck_View;

import Pck_Control.PedidoControl;
import Pck_Model.Cliente;
import Pck_Model.Produto;
import Pck_Persistencia.ClientePersistencia;
import Pck_Persistencia.ProdutoPersistencia;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.List;

public class PedidoView extends JFrame {
    private static final long serialVersionUID = 1L;

    private JComboBox<Cliente> cbCliente;
    private JComboBox<Produto> cbProduto;
    private JTextField tfQtde;
    private JLabel lblPedido;
    private DefaultTableModel modelItens;

    private final PedidoControl control = new PedidoControl();

    public PedidoView() {
        setTitle("Emissão de Pedido (MVC + DAO)");
        setSize(640, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel l1 = new JLabel("Cliente:");
        l1.setBounds(20, 20, 80, 20);
        add(l1);

        cbCliente = new JComboBox<>();
        cbCliente.setBounds(100, 20, 250, 22);
        add(cbCliente);

        JButton btCriar = new JButton("Criar Pedido");
        btCriar.setBounds(370, 20, 130, 26);
        btCriar.addActionListener(this::criarPedido);
        add(btCriar);

        lblPedido = new JLabel("Pedido atual: (nenhum)");
        lblPedido.setBounds(20, 55, 400, 26);
        add(lblPedido);

        JLabel l3 = new JLabel("Produto:");
        l3.setBounds(20, 100, 80, 20);
        add(l3);

        cbProduto = new JComboBox<>();
        cbProduto.setBounds(100, 100, 250, 22);
        add(cbProduto);

        JLabel l4 = new JLabel("Qtde:");
        l4.setBounds(370, 100, 40, 20);
        add(l4);

        tfQtde = new JTextField("1");
        tfQtde.setBounds(410, 100, 50, 22);
        add(tfQtde);

        JButton btAdd = new JButton("Adicionar Item");
        btAdd.setBounds(480, 98, 120, 26);
        btAdd.addActionListener(this::adicionarItem);
        add(btAdd);

        modelItens = new DefaultTableModel(new Object[]{"Produto", "Qtde"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tb = new JTable(modelItens);
        JScrollPane sp = new JScrollPane(tb);
        sp.setBounds(20, 140, 580, 180);
        add(sp);

        JButton btFinalizar = new JButton("Finalizar Pedido");
        btFinalizar.setBounds(20, 335, 150, 26);
        btFinalizar.addActionListener(this::finalizarPedido);
        add(btFinalizar);

        carregarCombos();
    }

    private void carregarCombos() {
        List<Cliente> clientes = new ClientePersistencia().listar();
        for (Cliente c : clientes) cbCliente.addItem(c);

        List<Produto> produtos = new ProdutoPersistencia().listar();
        for (Produto p : produtos) cbProduto.addItem(p);
    }

    private void criarPedido(ActionEvent e) {
        Cliente c = (Cliente) cbCliente.getSelectedItem();
        if (c == null) { JOptionPane.showMessageDialog(this, "Selecione um cliente."); return; }
        int id = control.criarPedido(c.getCodigo());
        lblPedido.setText("Pedido atual: " + id + " | Cliente: " + c.getNome());
        modelItens.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Pedido criado com ID " + id);
    }

    private void adicionarItem(ActionEvent e) {
        if (control.getA02CodigoAtual() <= 0) {
            JOptionPane.showMessageDialog(this, "Crie o pedido primeiro.");
            return;
        }
        Produto p = (Produto) cbProduto.getSelectedItem();
        if (p == null) { JOptionPane.showMessageDialog(this, "Selecione um produto."); return; }

        int qtde;
        try { qtde = Integer.parseInt(tfQtde.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Qtde inválida."); return; }

        try {
            control.adicionarItem(p.getId(), qtde);
            modelItens.addRow(new Object[]{p.getDescricao(), qtde});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar item: " + ex.getMessage());
        }
    }

    private void finalizarPedido(ActionEvent e) {
        if (control.getA02CodigoAtual() <= 0) {
            JOptionPane.showMessageDialog(this, "Crie o pedido primeiro.");
            return;
        }
        control.finalizar();
        JOptionPane.showMessageDialog(this, "Pedido " + control.getA02CodigoAtual() + " finalizado (total atualizado).");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PedidoView().setVisible(true));
    }
}

✅ Como testar

Execute o SQL (tabelas e procedures com nomes do seu diagrama).

Ajuste a senha em ConexaoMySql.

Run PedidoView.main()

Escolha Cliente → Criar Pedido

Escolha Produto + Qtde → Adicionar Item (repare que o estoque vai baixando)

Finalizar Pedido → recalcula A02_valor_total.

No Workbench:

SELECT * FROM PEDIDO_02 ORDER BY A02_codigo DESC;
SELECT * FROM ITEM_04  ORDER BY A04_codigo DESC;
SELECT * FROM PRODUTO_03;


Com isso, Java + Banco ficam exatamente compatíveis com o seu MER (CLIENTE_01, PEDIDO_02, PRODUTO_03, ITEM_04). Se quiser, te monto um PDF de entrega com os prints e explicações.
