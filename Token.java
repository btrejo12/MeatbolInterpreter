package meatbol;

/**
 * This class represents a token for the Scanner Class.  
 */
public class Token
{
    /** string from the source program, possibly modified for literals
     */
    public String tokenStr = "";
    /** Parser uses this to help simplify parsing since many subclasses are 
     * combined.  Some values: OPERAND, SEPARATOR, OPERATOR, EMPTY
     */
    public Classif primClassif = Classif.EMPTY;
    /** a sub-classification of a token also used to simplify parsing.
     * Some values for OPERANDs: IDENTIFIER, INTEGER constant, FLOAT constant,
     * STRING constant.
     */
    public SubClassif subClassif = SubClassif.EMPTY;
    /** Line number location in the source file for this token.  Line numbers are
     * * relative to 1.
     */
    public int iSourceLineNr = 0;
    /** Column location in the source file for this token.  column positions are 
     * relative to zero.
     */
    public int iColPos = 0;
    
    public Token(String value)
    {
        this.tokenStr = value;
    }
    public Token()
    {
        this("");   // invoke the other constructor
    }
    /** 
     * Prints the primary classification, sub-classification, and token string
     * <p>
     * If the classification is EMPTY, it uses "**garbage**".
     * If the sub-classification is EMPTY, it uses "-".
     */
    public void printToken()
    {
        String primClassifStr;
        String subClassifStr;

        if (primClassif != Classif.EMPTY)
            primClassifStr = primClassif.toString();
        else
            primClassifStr = "**garbage**";

        if (subClassif != SubClassif.EMPTY)
            subClassifStr = subClassif.toString();
        else
            subClassifStr = "-";
    
        System.out.printf("%-11s %-12s %s\n"
            , primClassifStr
            , subClassifStr
            , tokenStr);
    }

}      