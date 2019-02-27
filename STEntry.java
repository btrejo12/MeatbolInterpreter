package meatbol;

public class STEntry {

    private String symbol;
    Classif primClassif;

    /**
     * <p>STEntry is the abstract class for Symbol Table entries</p>
     * @param string the stirng value for this SymbolTable Entry
     * @param classification the primary classification for this entry
     */
    public STEntry(String string, Classif classification){
        symbol = string;
        primClassif = classification;
    }
}
