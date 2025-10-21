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
