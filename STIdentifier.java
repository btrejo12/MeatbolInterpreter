package meatbol;

public class STIdentifier extends STEntry{

    public SubClassif dclType;
    public String structure;
    public String parm;
    public int nonLocal;

    /**
     * <p>STIdentifier specifies STEntry as an Identifier and populates other requires attributes.</p>
     * @param string The string variable for this STEntry
     * @param classification the primary classification of this STEntry as specified by the super class
     * @param declare the declare type of this identifier
     */
    public STIdentifier(String string, Classif classification, SubClassif declare){
        super(string, classification);
        dclType = declare;
    }
}
