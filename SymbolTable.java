package meatbol;
import java.util.HashMap;

public class SymbolTable {
    HashMap <String, STEntry> ht = new HashMap<String, STEntry>();
    private static final int VAR_ARGS = -1;

    public SymbolTable(){
        initGlobal();
    }

    public STEntry getSymbol(String symbol){
        return ht.get(symbol);
    }

    public void putSymbol(String symbol, STEntry entry){
        ht.put(symbol, entry);
    }

    private void initGlobal(){
        ht.put("def", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
        ht.put("enddef", new STControl("enddef", Classif.CONTROL, SubClassif.END));
        ht.put("if", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endif", new STControl("endif", Classif.CONTROL, SubClassif.END));
        ht.put("else", new STControl("else", Classif.CONTROL, SubClassif.END));
        ht.put("for", new STControl("for", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endfor", new STControl("endfor", Classif.CONTROL, SubClassif.END));
        ht.put("while", new STControl("while", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endwhile", new STControl("endwhile", Classif.CONTROL, SubClassif.END));
        ht.put("print", new STFunction("print", Classif.FUNCTION, SubClassif.VOID, SubClassif.BUILTIN, VAR_ARGS));
        ht.put("Int", new STControl("Int", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("Float", new STControl("Float", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("String", new STControl("String", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("Bool", new STControl("Bool", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("Date", new STControl("Date", Classif.CONTROL, SubClassif.DECLARE));
        ht.put("LENGTH", new STFunction("LENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1));
        ht.put("MAXLENGTH", new STFunction("MAXLENGTH", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN,1 ));
        ht.put("SPACES", new STFunction("SPACES", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN,1 ));
        ht.put("ELEM", new STFunction("ELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1));
        ht.put("MAXELEM", new STFunction("MAXELEM", Classif.FUNCTION, SubClassif.INTEGER, SubClassif.BUILTIN, 1));
        ht.put("and", new STEntry("and", Classif.OPERATOR));
        ht.put("or", new STEntry("or", Classif.OPERATOR));
        ht.put("not", new STEntry("not", Classif.OPERATOR));
        ht.put("in", new STEntry("in", Classif.OPERATOR));
        ht.put("notin", new STEntry("notin", Classif.OPERATOR));
    }
}
