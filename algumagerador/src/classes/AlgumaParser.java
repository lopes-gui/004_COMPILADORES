import java.util.ArrayList;
import java.util.List;
import AlgumaLex.Token;
import AlgumaLex.TipoToken
import AlgumaLex.AlgumaLexico;


public class AlgumaParser {

    private final static int TAMANHO_BUFFER = 10;
    List<Token> bufferTokens;
    AlgumaLexico lex;
    boolean chegouNoFim = false;

    public AlgumaParser(AlgumaLexico lex) {
        this.lex = lex;
        bufferTokens = new ArrayList<Token>();
        lerToken();
    }

    private void lerToken() {
        if (bufferTokens.size() > 0) {
            bufferTokens.remove(0);
        }
        while (bufferTokens.size() < TAMANHO_BUFFER && !chegouNoFim) {
            Token proximo = lex.proximoToken();
            bufferTokens.add(proximo);
            if (proximo.nome == TipoToken.Fim) {
                chegouNoFim = true;
            }
        }
        System.out.println("Lido:  " + lookahead(1));
    }

    void match(TipoToken tipo) {
        if (lookahead(1).nome == tipo) {
            System.out.println("Match: " + lookahead(1));
            lerToken();
        } else {
            erroSintatico(tipo.toString());
        }
    }

    Token lookahead(int k) {
        if (bufferTokens.isEmpty()) {
            return null;
        }
        if (k - 1 >= bufferTokens.size()) {
            return bufferTokens.get(bufferTokens.size() - 1);
        }
        return bufferTokens.get(k - 1);
    }

    void erroSintatico(String... tokensEsperados) {
        String mensagem = "Erro sintático: esperando um dos seguintes (";
        for(int i=0;i<tokensEsperados.length;i++) {
            mensagem += tokensEsperados[i];
            if(i<tokensEsperados.length-1)
                mensagem += ",";
        }
        mensagem += "), mas foi encontrado " + lookahead(1);
        throw new RuntimeException(mensagem);
    }
}
//programa : ':' 'DECLARACOES' listaDeclaracoes ':' 'ALGORITMO' listaComandos;
public void programa() {
    match(TipoToken.Delim);
    match(TipoToken.PCDeclaracoes);
    listaDeclaracoes();
    match(TipoToken.Delim);
    match(TipoToken.PCAlgoritmo);
    listaComandos();
    match(TipoToken.Fim);
}

//listaDeclaracoes : declaracao listaDeclaracoes | declaracao;
void listaDeclaracoes() {
    // usaremos lookahead(4)
    // Mas daria para fatorar à esquerda
    // Veja na lista de comandos um exemplo
    if (lookahead(4).nome == TipoToken.Delim) {
        declaracao();
    } else if (lookahead(4).nome == TipoToken.Var) {
        declaracao();
        listaDeclaracoes();
    } else {
        erroSintatico(TipoToken.Delim.toString(), TipoToken.Var.toString());
    }
}

//declaracao : VARIAVEL ':' tipoVar;
void declaracao() {
    match(TipoToken.Var);
    match(TipoToken.Delim);
    tipoVar();
}

//tipoVar : 'INTEIRO' | 'REAL';
void tipoVar() {
    if (lookahead(1).nome == TipoToken.PCInteiro) {
        match(TipoToken.PCInteiro);
    } else if (lookahead(1).nome == TipoToken.PCReal) {
        match(TipoToken.PCReal);
    } else {
        erroSintatico("INTEIRO","REAL");
    }
}

//expressaoAritmetica : expressaoAritmetica '+' termoAritmetico | expressaoAritmetica '-' termoAritmetico | termoAritmetico;
// fatorar à esquerda:
// expressaoAritmetica : expressaoAritmetica ('+' termoAritmetico | '-' termoAritmetico) | termoAritmetico;
// fatorar não é suficiente, pois causa loop infinito
// remover a recursão à esquerda
// expressaoAritmetica : termoAritmetico expressaoAritmetica2
// expressaoAritmetica2 : ('+' termoAritmetico | '-' termoAritmetico) expressaoAritmetica2 | <<vazio>>
void expressaoAritmetica() {
    termoAritmetico();
    expressaoAritmetica2();
}

void expressaoAritmetica2() {
    if (lookahead(1).nome == TipoToken.OpAritSoma || lookahead(1).nome == TipoToken.OpAritSub) {
        expressaoAritmetica2SubRegra1();
        expressaoAritmetica2();
    } else { // vazio
    }
}

void expressaoAritmetica2SubRegra1() {
    if (lookahead(1).nome == TipoToken.OpAritSoma) {
        match(TipoToken.OpAritSoma);
        termoAritmetico();
    } else if (lookahead(1).nome == TipoToken.OpAritSub) {
        match(TipoToken.OpAritSub);
        termoAritmetico();
    } else {
        erroSintatico("+","-");
    }
}

//termoAritmetico : termoAritmetico '*' fatorAritmetico | termoAritmetico '/' fatorAritmetico | fatorAritmetico;
// também precisa fatorar à esquerda e eliminar recursão à esquerda
// termoAritmetico : fatorAritmetico termoAritmetico2
// termoAritmetico2 : ('*' fatorAritmetico | '/' fatorAritmetico) termoAritmetico2 | <<vazio>>
void termoAritmetico() {
    fatorAritmetico();
    termoAritmetico2();
}

void termoAritmetico2() {
    if (lookahead(1).nome == TipoToken.OpAritMult || lookahead(1).nome == TipoToken.OpAritDiv) {
        termoAritmetico2SubRegra1();
        termoAritmetico2();
    } else { // vazio
    }
}

void termoAritmetico2SubRegra1() {
    if (lookahead(1).nome == TipoToken.OpAritMult) {
        match(TipoToken.OpAritMult);
        fatorAritmetico();
    } else if (lookahead(1).nome == TipoToken.OpAritDiv) {
        match(TipoToken.OpAritDiv);
        fatorAritmetico();
    } else {
        erroSintatico("*","/");
    }
}

//fatorAritmetico : NUMINT | NUMREAL | VARIAVEL | '(' expressaoAritmetica ')'
void fatorAritmetico() {
    if (lookahead(1).nome == TipoToken.NumInt) {
        match(TipoToken.NumInt);
    } else if (lookahead(1).nome == TipoToken.NumReal) {
        match(TipoToken.NumReal);
    } else if (lookahead(1).nome == TipoToken.Var) {
        match(TipoToken.Var);
    } else if (lookahead(1).nome == TipoToken.AbrePar) {
        match(TipoToken.AbrePar);
        expressaoAritmetica();
        match(TipoToken.FechaPar);
    } else {
        erroSintatico(TipoToken.NumInt.toString(),TipoToken.NumReal.toString(),TipoToken.Var.toString(),"(");
    }
}

//expressaoRelacional : expressaoRelacional operadorBooleano termoRelacional | termoRelacional;
// Precisa eliminar a recursão à esquerda
// expressaoRelacional : termoRelacional expressaoRelacional2;
// expressaoRelacional2 : operadorBooleano termoRelacional expressaoRelacional2 | <<vazio>>
void expressaoRelacional() {
    termoRelacional();
    expressaoRelacional2();
}

void expressaoRelacional2() {
    if (lookahead(1).nome == TipoToken.OpBoolE || lookahead(1).nome == TipoToken.OpBoolOu) {
        operadorBooleano();
        termoRelacional();
        expressaoRelacional2();
    } else { // vazio
    }
}

//termoRelacional : expressaoAritmetica OP_REL expressaoAritmetica | '(' expressaoRelacional ')';
void termoRelacional() {
    if (lookahead(1).nome == TipoToken.NumInt
            || lookahead(1).nome == TipoToken.NumReal
            || lookahead(1).nome == TipoToken.Var
            || lookahead(1).nome == TipoToken.AbrePar) {
        // Há um não-determinismo aqui.
        // AbrePar pode ocorrer tanto em expressaoAritmetica como em (expressaoRelacional)
        // Tem uma forma de resolver este problema, mas não usaremos aqui
        // Vamos modificar a linguagem, eliminando a possibilidade
        // de agrupar expressões relacionais com parêntesis
        expressaoAritmetica();
        opRel();
        expressaoAritmetica();
    } else {
        erroSintatico(TipoToken.NumInt.toString(),TipoToken.NumReal.toString(),TipoToken.Var.toString(),"(");
    }
}

void opRel() {
    if (lookahead(1).nome == TipoToken.OpRelDif) {
        match(TipoToken.OpRelDif);
    } else if (lookahead(1).nome == TipoToken.OpRelIgual) {
        match(TipoToken.OpRelIgual);
    } else if (lookahead(1).nome == TipoToken.OpRelMaior) {
        match(TipoToken.OpRelMaior);
    } else if (lookahead(1).nome == TipoToken.OpRelMaiorIgual) {
        match(TipoToken.OpRelMaiorIgual);
    } else if (lookahead(1).nome == TipoToken.OpRelMenor) {
        match(TipoToken.OpRelMenor);
    } else if (lookahead(1).nome == TipoToken.OpRelMenorIgual) {
        match(TipoToken.OpRelMenorIgual);
    } else {
        erroSintatico("<>","=",">",">=","<","<=");
    }
}

//operadorBooleano : 'E' | 'OU';
void operadorBooleano() {
    if (lookahead(1).nome == TipoToken.OpBoolE) {
        match(TipoToken.OpBoolE);
    } else if (lookahead(1).nome == TipoToken.OpBoolOu) {
        match(TipoToken.OpBoolOu);
    } else {
        erroSintatico("E","OU");
    }
}

//listaComandos : comando listaComandos | comando;
// vamos fatorar à esquerda
// listaComandos : comando (listaComandos | <<vazio>>)
void listaComandos() {
    comando();
    listaComandosSubRegra1();
}

void listaComandosSubRegra1() {
    if (lookahead(1).nome == TipoToken.PCAtribuir ||
    lookahead(1).nome == TipoToken.PCLer ||
    lookahead(1).nome == TipoToken.PCImprimir ||
    lookahead(1).nome == TipoToken.PCSe ||
    lookahead(1).nome == TipoToken.PCEnquanto ||
    lookahead(1).nome == TipoToken.PCInicio) {
        listaComandos();
    } else {
        // vazio
    }
}

//comando : comandoAtribuicao | comandoEntrada | comandoSaida | comandoCondicao | comandoRepeticao | subAlgoritmo;
void comando() {
    if (lookahead(1).nome == TipoToken.PCAtribuir) {
        comandoAtribuicao();
    } else if (lookahead(1).nome == TipoToken.PCLer) {
        comandoEntrada();
    } else if (lookahead(1).nome == TipoToken.PCImprimir) {
        comandoSaida();
    } else if (lookahead(1).nome == TipoToken.PCSe) {
        comandoCondicao();
    } else if (lookahead(1).nome == TipoToken.PCEnquanto) {
        comandoRepeticao();
    } else if (lookahead(1).nome == TipoToken.PCInicio) {
        subAlgoritmo();
    } else {
        erroSintatico("ATRIBUIR","LER","IMPRIMIR","SE","ENQUANTO","INICIO");
    }
}

//comandoAtribuicao : 'ATRIBUIR' expressaoAritmetica 'A' VARIAVEL;
void comandoAtribuicao() {
    match(TipoToken.PCAtribuir);
    expressaoAritmetica();
    match(TipoToken.PCA);
    match(TipoToken.Var);
}

//comandoEntrada : 'LER' VARIAVEL;
void comandoEntrada() {
    match(TipoToken.PCLer);
    match(TipoToken.Var);
}

//comandoSaida : 'IMPRIMIR'  (VARIAVEL | CADEIA);
void comandoSaida() {
    match(TipoToken.PCImprimir);
    comandoSaidaSubRegra1();
}

void comandoSaidaSubRegra1() {
    if (lookahead(1).nome == TipoToken.Var) {
        match(TipoToken.Var);
    } else if (lookahead(1).nome == TipoToken.Cadeia) {
        match(TipoToken.Cadeia);
    } else {
        erroSintatico(TipoToken.Var.toString(),TipoToken.Cadeia.toString());
    }
}

//comandoCondicao : 'SE' expressaoRelacional 'ENTAO' comando | 'SE' expressaoRelacional 'ENTAO' comando 'SENAO' comando;
// fatorar à esquerda
// comandoCondicao : 'SE' expressaoRelacional 'ENTAO' comando ('SENAO' comando | <<vazio>>)
void comandoCondicao() {
    match(TipoToken.PCSe);
    expressaoRelacional();
    match(TipoToken.PCEntao);
    comando();
    comandoCondicaoSubRegra1();
}

void comandoCondicaoSubRegra1() {
    if (lookahead(1).nome == TipoToken.PCSenao) {
        match(TipoToken.PCSenao);
        comando();
    } else {
        // vazio
    }
}

//comandoRepeticao : 'ENQUANTO' expressaoRelacional comando;
void comandoRepeticao() {
    match(TipoToken.PCEnquanto);
    expressaoRelacional();
    comando();
}

//subAlgoritmo : 'INICIO' listaComandos 'FIM';
void subAlgoritmo() {
    match(TipoToken.PCInicio);
    listaComandos();
    match(TipoToken.PCFim);
}
}