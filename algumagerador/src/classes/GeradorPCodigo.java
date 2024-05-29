package classes;
public class AlgumaGeradorPcodigo extends AlgumaBaseVisitor<String> {

    TabelaDeSimbolos tabela = new TabelaDeSimbolos();
    int enderecoAtual = 0;
    int label = 0;

    @Override
    public String visitPrograma(AlgumaParser.ProgramaContext ctx) {
        String pcod = "";
        ctx.declaracao().forEach(dec -> visitDeclaracao(dec));
        for (var c : ctx.comando()) {
            pcod += visitComando(c);
        }
        pcod += "stp\n";
        return pcod;
    }

    @Override
    public String visitDeclaracao(AlgumaParser.DeclaracaoContext ctx) {
        tabela.adicionar(ctx.VARIAVEL().getText(), enderecoAtual++);
        return null;
    }

    @Override
    public String visitExpressaoAritmetica(AlgumaParser.ExpressaoAritmeticaContext ctx) {
        String pcod = "";
        pcod += visitTermoAritmetico(ctx.termoAritmetico(0));
        for (int i = 1; i < ctx.termoAritmetico().size(); i++) {
            pcod += visitTermoAritmetico(ctx.termoAritmetico(i));
            if (ctx.OP_ARIT1(i - 1).getText().equals("+")) {
                pcod += "adi\n";
            } else if (ctx.OP_ARIT1(i - 1).getText().equals("-")) {
                pcod += "sbi\n";
            }
        }
        return pcod;
    }

    @Override
    public String visitTermoAritmetico(AlgumaParser.TermoAritmeticoContext ctx) {
        String pcod = "";
        pcod += visitFatorAritmetico(ctx.fatorAritmetico(0));
        for (int i = 1; i < ctx.fatorAritmetico().size(); i++) {
            pcod += visitFatorAritmetico(ctx.fatorAritmetico(i));
            if (ctx.OP_ARIT2(i - 1).getText().equals("*")) {
                pcod += "mpi\n";
            } else if (ctx.OP_ARIT2(i - 1).getText().equals("/")) {
                pcod += "dvi\n";
            }
        }
        return pcod;
    }

    @Override
    public String visitFatorAritmetico(AlgumaParser.FatorAritmeticoContext ctx) {
        if (ctx.NUMINT() != null) {
            return "ldc " + ctx.NUMINT().getText() + "\n";
        } else if (ctx.NUMREAL() != null) {
            return "ldc " + ctx.NUMREAL().getText() + "\n";
        } else if (ctx.VARIAVEL() != null) {
            int endereco = tabela.verificarEndereco(ctx.VARIAVEL().getText());
            return "lod " + endereco + "\n";
        } else {
            return visitExpressaoAritmetica(ctx.expressaoAritmetica());
        }
    }

    @Override
    public String visitExpressaoRelacional(AlgumaParser.ExpressaoRelacionalContext ctx) {
        String pcod = visitTermoRelacional(ctx.termoRelacional(0));
        for (int i = 1; i < ctx.termoRelacional().size(); i++) {
            pcod += visitTermoRelacional(ctx.termoRelacional(i));
            if (ctx.OP_BOOL(i - 1).getText().equals("E")) {
                pcod += "and\n";
            } else if (ctx.OP_BOOL(i - 1).getText().equals("OU")) {
                pcod += "or\n";
            }

        }
        return pcod;
    }

    @Override
    public String visitTermoRelacional(AlgumaParser.TermoRelacionalContext ctx) {
        String pcod = "";
        if (ctx.expressaoRelacional() != null) {
            pcod = visitExpressaoRelacional(ctx.expressaoRelacional());

        } else {
            pcod += visitExpressaoAritmetica(ctx.expressaoAritmetica(0)) + visitExpressaoAritmetica(ctx.expressaoAritmetica(1));
            switch (ctx.OP_REL().getText()) {
                case ">":
                    pcod += "grt\n";
                    break;
                case ">=":
                    pcod += "gte\n";
                    break;
                case "<":
                    pcod += "let\n";
                    break;
                case "<=":
                    pcod += "lte\n";
                    break;
                case "<>":
                    pcod += "neq\n";
                    break;
                case "=":
                    pcod += "equ\n";
                    break;
                default:
                    break;
            }
        }

        return pcod;
    }

    @Override
    public String visitComando(AlgumaParser.ComandoContext ctx) {
        if (ctx.comandoAtribuicao() != null) {
            return visitComandoAtribuicao(ctx.comandoAtribuicao());
        } else if (ctx.comandoEntrada() != null) {
            return visitComandoEntrada(ctx.comandoEntrada());
        } else if (ctx.comandoSaida() != null) {
            return visitComandoSaida(ctx.comandoSaida());
        } else if (ctx.comandoCondicao() != null) {
            return visitComandoCondicao(ctx.comandoCondicao());
        } else if (ctx.comandoRepeticao() != null) {
            return visitComandoRepeticao(ctx.comandoRepeticao());
        } else if (ctx.subAlgoritmo() != null) {
            return visitSubAlgoritmo(ctx.subAlgoritmo());
        }
        return null;
    }

    @Override
    public String visitComandoAtribuicao(AlgumaParser.ComandoAtribuicaoContext ctx) {
        int endereco = tabela.verificarEndereco(ctx.VARIAVEL().getText());
        return "lda " + endereco + "\n"
                + visitExpressaoAritmetica(ctx.expressaoAritmetica())
                + "sto\n";
    }

    @Override
    public String visitComandoEntrada(AlgumaParser.ComandoEntradaContext ctx) {
        int endereco = tabela.verificarEndereco(ctx.VARIAVEL().getText());
        return "lda " + endereco + "\n"
                + "rdi\n";
    }

    @Override
    public String visitComandoSaida(AlgumaParser.ComandoSaidaContext ctx) {
        if (ctx.expressaoAritmetica() != null) {
            return visitExpressaoAritmetica(ctx.expressaoAritmetica()) +
                    "wri\n";
        }
        return "";
    }

    @Override
    public String visitComandoCondicao(AlgumaParser.ComandoCondicaoContext ctx) {
        String pcod;

        int label1 = label++;
        pcod = visitExpressaoRelacional(ctx.expressaoRelacional());
        pcod += "fjp L" + label1 + "\n";
        pcod += visitComando(ctx.comando(0));
        if (ctx.comando().size() > 1) {
            int label2 = label++;
            pcod += "ujp L" + label2 + "\n";
            pcod += "lab L" + label1 + "\n";
            pcod += visitComando(ctx.comando(1));
            pcod += "lab L" + label2 + "\n";
        } else {
            pcod += "lab L" + label1 + "\n";
        }

        return pcod;
    }

    @Override
    public String visitComandoRepeticao(AlgumaParser.ComandoRepeticaoContext ctx) {
        String pcod;
        int label1 = label++;
        int label2 = label++;
        pcod = "lab L" + label1 + "\n";
        pcod += visitExpressaoRelacional(ctx.expressaoRelacional());
        pcod += "fjp L" + label2 + "\n";
        pcod += visitComando(ctx.comando());
        pcod += "ujp L" + label1 + "\n";
        pcod += "lab L" + label2 + "\n";

        return pcod;
    }

    @Override
    public String visitSubAlgoritmo(AlgumaParser.SubAlgoritmoContext ctx) {
        String pcod = "";
        for (var c : ctx.comando()) {
            pcod += visitComando(c);
        }
        return pcod;
    }
}public class AlgumaGeradorPcodigo extends AlgumaBaseVisitor<String> {

    TabelaDeSimbolos tabela = new TabelaDeSimbolos();
    int enderecoAtual = 0;
    int label = 0;

    @Override
    public String visitPrograma(AlgumaParser.ProgramaContext ctx) {
        String pcod = "";
        ctx.declaracao().forEach(dec -> visitDeclaracao(dec));
        for (var c : ctx.comando()) {
            pcod += visitComando(c);
        }
        pcod += "stp\n";
        return pcod;
    }

    @Override
    public String visitDeclaracao(AlgumaParser.DeclaracaoContext ctx) {
        tabela.adicionar(ctx.VARIAVEL().getText(), enderecoAtual++);
        return null;
    }

    @Override
    public String visitExpressaoAritmetica(AlgumaParser.ExpressaoAritmeticaContext ctx) {
        String pcod = "";
        pcod += visitTermoAritmetico(ctx.termoAritmetico(0));
        for (int i = 1; i < ctx.termoAritmetico().size(); i++) {
            pcod += visitTermoAritmetico(ctx.termoAritmetico(i));
            if (ctx.OP_ARIT1(i - 1).getText().equals("+")) {
                pcod += "adi\n";
            } else if (ctx.OP_ARIT1(i - 1).getText().equals("-")) {
                pcod += "sbi\n";
            }
        }
        return pcod;
    }

    @Override
    public String visitTermoAritmetico(AlgumaParser.TermoAritmeticoContext ctx) {
        String pcod = "";
        pcod += visitFatorAritmetico(ctx.fatorAritmetico(0));
        for (int i = 1; i < ctx.fatorAritmetico().size(); i++) {
            pcod += visitFatorAritmetico(ctx.fatorAritmetico(i));
            if (ctx.OP_ARIT2(i - 1).getText().equals("*")) {
                pcod += "mpi\n";
            } else if (ctx.OP_ARIT2(i - 1).getText().equals("/")) {
                pcod += "dvi\n";
            }
        }
        return pcod;
    }

    @Override
    public String visitFatorAritmetico(AlgumaParser.FatorAritmeticoContext ctx) {
        if (ctx.NUMINT() != null) {
            return "ldc " + ctx.NUMINT().getText() + "\n";
        } else if (ctx.NUMREAL() != null) {
            return "ldc " + ctx.NUMREAL().getText() + "\n";
        } else if (ctx.VARIAVEL() != null) {
            int endereco = tabela.verificarEndereco(ctx.VARIAVEL().getText());
            return "lod " + endereco + "\n";
        } else {
            return visitExpressaoAritmetica(ctx.expressaoAritmetica());
        }
    }

    @Override
    public String visitExpressaoRelacional(AlgumaParser.ExpressaoRelacionalContext ctx) {
        String pcod = visitTermoRelacional(ctx.termoRelacional(0));
        for (int i = 1; i < ctx.termoRelacional().size(); i++) {
            pcod += visitTermoRelacional(ctx.termoRelacional(i));
            if (ctx.OP_BOOL(i - 1).getText().equals("E")) {
                pcod += "and\n";
            } else if (ctx.OP_BOOL(i - 1).getText().equals("OU")) {
                pcod += "or\n";
            }

        }
        return pcod;
    }

    @Override
    public String visitTermoRelacional(AlgumaParser.TermoRelacionalContext ctx) {
        String pcod = "";
        if (ctx.expressaoRelacional() != null) {
            pcod = visitExpressaoRelacional(ctx.expressaoRelacional());

        } else {
            pcod += visitExpressaoAritmetica(ctx.expressaoAritmetica(0)) + visitExpressaoAritmetica(ctx.expressaoAritmetica(1));
            switch (ctx.OP_REL().getText()) {
                case ">":
                    pcod += "grt\n";
                    break;
                case ">=":
                    pcod += "gte\n";
                    break;
                case "<":
                    pcod += "let\n";
                    break;
                case "<=":
                    pcod += "lte\n";
                    break;
                case "<>":
                    pcod += "neq\n";
                    break;
                case "=":
                    pcod += "equ\n";
                    break;
                default:
                    break;
            }
        }

        return pcod;
    }

    @Override
    public String visitComando(AlgumaParser.ComandoContext ctx) {
        if (ctx.comandoAtribuicao() != null) {
            return visitComandoAtribuicao(ctx.comandoAtribuicao());
        } else if (ctx.comandoEntrada() != null) {
            return visitComandoEntrada(ctx.comandoEntrada());
        } else if (ctx.comandoSaida() != null) {
            return visitComandoSaida(ctx.comandoSaida());
        } else if (ctx.comandoCondicao() != null) {
            return visitComandoCondicao(ctx.comandoCondicao());
        } else if (ctx.comandoRepeticao() != null) {
            return visitComandoRepeticao(ctx.comandoRepeticao());
        } else if (ctx.subAlgoritmo() != null) {
            return visitSubAlgoritmo(ctx.subAlgoritmo());
        }
        return null;
    }

    @Override
    public String visitComandoAtribuicao(AlgumaParser.ComandoAtribuicaoContext ctx) {
        int endereco = tabela.verificarEndereco(ctx.VARIAVEL().getText());
        return "lda " + endereco + "\n"
                + visitExpressaoAritmetica(ctx.expressaoAritmetica())
                + "sto\n";
    }

    @Override
    public String visitComandoEntrada(AlgumaParser.ComandoEntradaContext ctx) {
        int endereco = tabela.verificarEndereco(ctx.VARIAVEL().getText());
        return "lda " + endereco + "\n"
                + "rdi\n";
    }

    @Override
    public String visitComandoSaida(AlgumaParser.ComandoSaidaContext ctx) {
        if (ctx.expressaoAritmetica() != null) {
            return visitExpressaoAritmetica(ctx.expressaoAritmetica()) +
                    "wri\n";
        }
        return "";
    }

    @Override
    public String visitComandoCondicao(AlgumaParser.ComandoCondicaoContext ctx) {
        String pcod;

        int label1 = label++;
        pcod = visitExpressaoRelacional(ctx.expressaoRelacional());
        pcod += "fjp L" + label1 + "\n";
        pcod += visitComando(ctx.comando(0));
        if (ctx.comando().size() > 1) {
            int label2 = label++;
            pcod += "ujp L" + label2 + "\n";
            pcod += "lab L" + label1 + "\n";
            pcod += visitComando(ctx.comando(1));
            pcod += "lab L" + label2 + "\n";
        } else {
            pcod += "lab L" + label1 + "\n";
        }

        return pcod;
    }

    @Override
    public String visitComandoRepeticao(AlgumaParser.ComandoRepeticaoContext ctx) {
        String pcod;
        int label1 = label++;
        int label2 = label++;
        pcod = "lab L" + label1 + "\n";
        pcod += visitExpressaoRelacional(ctx.expressaoRelacional());
        pcod += "fjp L" + label2 + "\n";
        pcod += visitComando(ctx.comando());
        pcod += "ujp L" + label1 + "\n";
        pcod += "lab L" + label2 + "\n";

        return pcod;
    }

    @Override
    public String visitSubAlgoritmo(AlgumaParser.SubAlgoritmoContext ctx) {
        String pcod = "";
        for (var c : ctx.comando()) {
            pcod += visitComando(c);
        }
        return pcod;
    }
}