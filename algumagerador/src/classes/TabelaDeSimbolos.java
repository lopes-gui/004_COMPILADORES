package classes;

import java.util.HashMap;
import java.util.Map;


public class TabelaDeSimbolos {
    public enum TipoAlguma {
        INTEIRO,
        REAL,
        INVALIDO
    }
    
    class EntradaTabelaDeSimbolos {
        String nome;
        TipoAlguma tipo;
        int endereco;

        private EntradaTabelaDeSimbolos(String nome, TipoAlguma tipo) {
            this.nome = nome;
            this.tipo = tipo;
        }

        private EntradaTabelaDeSimbolos(String nome, int endereco) {
            this.nome = nome;
           this.endereco = endereco;
        }
    }
    
    private final Map<String, EntradaTabelaDeSimbolos> tabela;
    
    public TabelaDeSimbolos() {
        this.tabela = new HashMap<>();
    }
    
    public void adicionar(String nome, TipoAlguma tipo) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, tipo));
    }

    public void adicionar(String nome, int endereco) {
        tabela.put(nome, new EntradaTabelaDeSimbolos(nome, endereco));
    }
    
    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }
    
    public TipoAlguma verificar(String nome) {
        return tabela.get(nome).tipo;
    }
    
    public int verificarEndereco(String nome) {
       return tabela.get(nome).endereco;
    }
}