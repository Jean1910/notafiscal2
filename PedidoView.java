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
