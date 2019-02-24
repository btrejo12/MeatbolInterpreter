package meatbol;

public class STIdentifier extends STEntry{

    public SubClassif dclType;
    public String structure;
    public String parm;
    public int nonLocal;

    public STIdentifier(String string, Classif classification, SubClassif declare, String struct, String params, int nonLocalBase) {
        super(string, classification);
        dclType = declare;
        structure = struct;
        parm = params;
        nonLocal = nonLocalBase;
    }
}
