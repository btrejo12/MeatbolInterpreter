package meatbol;
import java.util.ArrayList;

public class STFunction extends STEntry{

    public SubClassif returnType;
    public SubClassif definedBy;
    public int numArgs;
    public ArrayList<String> paramList;
    public SymbolTable symbolTable;

    /**
     * <p>STFunction declares a type of STEntry that is a function. It also populates additional attributes</p>
     * @param string The string name for this STEntry
     * @param classification The primary classification for this STEntry, which is FUNCTION.
     * @param returnT The return type for this function
     * @param defined Specifies whether this function is user-defined or built in
     * @param iArguments The number of arguments to this function.
     */
    public STFunction(String string, Classif classification, SubClassif returnT, SubClassif defined,
                      int iArguments){
        super(string, classification);
        returnType = returnT;
        definedBy = defined;
        numArgs = iArguments;
    }
}
