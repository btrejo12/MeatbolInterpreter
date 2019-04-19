package meatbol;
import java.util.HashMap;

public class SymbolTable {
    HashMap <String, STEntry> ht = new HashMap<String, STEntry>();
    private static final int VAR_ARGS = -1;

    public SymbolTable(){
        initGlobal();
    }

    /**
     * GetSymbol returns the primary and sub classification for specified token using the token's
     * instance of STEntry. If the token is not saved in the hashmap, it is inserted.
     * @param symbol the tokenStr to be looked up in the hashmap
     * @return the tokenStr's STEntry instance
     */
    public STEntry getSymbol(String symbol){
        if (ht.containsKey(symbol))
            return ht.get(symbol);
        else{
            return null;
        }
    }

    public void updateSymbol(String symbol, STEntry modifications){ ht.put(symbol, modifications);}

    /**
     * <p>PutSymbol places this token's string and STEntry instance into the global hashmap</p>
     * @param symbol the text key for the hashmap
     * @param entry the STEntry instance for the variable
     */
    public void putSymbol(String symbol, STEntry entry){
        ht.put(symbol, entry);
    }

    /**
     * <p>InitGloabl populates the gloabl hashmap with system functions and identifiers.</p>
     */
    private void initGlobal(){
        ht.put("def", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
        ht.put("enddef", new STControl("enddef", Classif.CONTROL, SubClassif.END));
        ht.put("if", new STControl("if", Classif.CONTROL, SubClassif.FLOW));
        ht.put("endif", new STControl("endif", Classif.CONTROL, SubClassif.END));
        ht.put("else", new STControl("else", Classif.CONTROL, SubClassif.END));
        ht.put("for", new STControl("for", Classif.CONTROL, SubClassif.FLOW));
        ht.put("to", new STControl("to", Classif.CONTROL, SubClassif.FLOW));
        ht.put("by", new STControl("by", Classif.CONTROL, SubClassif.FLOW));
        ht.put("in", new STControl("in", Classif.CONTROL, SubClassif.FLOW));
        ht.put("from", new STControl("from", Classif.CONTROL, SubClassif.FLOW));
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
        //ht.put("in", new STEntry("in", Classif.OPERATOR));
        ht.put("notin", new STEntry("notin", Classif.OPERATOR));
        ht.put("T", new STIdentifier("T", Classif.OPERAND, SubClassif.BOOLEAN));
        ht.put("F", new STIdentifier("F", Classif.OPERAND, SubClassif.BOOLEAN));
        ht.put("debug", new STIdentifier("debug", Classif.DEBUG, SubClassif.EMPTY));
        ht.put("Token", new STIdentifier("token", Classif.DEBUG, SubClassif.DEBUGTYPE));
        ht.put("Expr", new STIdentifier("token", Classif.DEBUG, SubClassif.DEBUGTYPE));
        ht.put("Assign", new STIdentifier("token", Classif.DEBUG, SubClassif.DEBUGTYPE));
        ht.put("Stmt", new STIdentifier("token", Classif.DEBUG, SubClassif.DEBUGTYPE));
    }
}
