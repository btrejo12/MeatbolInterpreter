package meatbol;
import java.util.ArrayList;

public class STFunction extends STEntry{

    public SubClassif returnType;
    public SubClassif definedBy;
    public int numArgs;
    public ArrayList<String> paramList;
    public SymbolTable symbolTable;

    public STFunction(String string, Classif classification, SubClassif returnT, SubClassif defined,
                      int iArguments){
        super(string, classification);
        returnType = returnT;
        definedBy = defined;
        numArgs = iArguments;
    }
}
