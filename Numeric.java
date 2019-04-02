package meatbol;

public class Numeric {

    public int integerValue;
    public float floatValue;
    //public double doubleValue;
    public String strValue;
    public SubClassif type;
    public String title;

    /**
     * <p>Numberic constructor that doesn't need to be used right now.</p>
     */
    public Numeric(){}

    /**
     * Create a generic Numeric object for all data types used in assignment.
     * @param parse The Parser object to reference for errors.
     * @param res   The ResultValue used to convert into a Numeric type.
     * @param operator   The operator symbol used for error throwing (maybe)
     * @param title     The title, order of operand, to be used in error throwing
     * @throws Exception
     */
    public Numeric(Parser parse, ResultValue res, String operator, String title) throws Exception{
        this.strValue = res.value;
        this.title = title;
        this.type = res.type;

        if(res.type == SubClassif.INTEGER){
            this.integerValue = Integer.parseInt(this.strValue);
        } else if (res.type == SubClassif.FLOAT) {
            this.floatValue = Float.parseFloat(this.strValue);
        } else if(res.type == SubClassif.STRING) {
            return;
        } else {
            parse.error("Cannot recognize primitive type of " + this.title);
        }
    }

    /**
     * Scanner calls checkNumType through a Numeric object to validate if the passed-in variable
     * is a number. Tests for integer or float. Throws an exception if the formatting of the number is
     * incorrect. Returns the subClassif for the number being tested.
     *
     * @param var for numeric testing
     * @return the subClassif of the variable entered
     */
    public SubClassif checkNumType(String var) throws Exception {
        int decCounter = 0;
        SubClassif sClassif = null;

        for (int i = 0; i < var.length(); i++) {
            if (var.charAt(i) == '.')
                decCounter++;
            else if (Character.isLetter(var.charAt(i))) {
                throw new Exception("Letter instead of number");
            }
        }
        if (decCounter > 1) {
            throw new Exception("Incorrect number formatting");
        }
        else if (decCounter == 0) {
            sClassif = SubClassif.INTEGER;
        } else {
            sClassif = SubClassif.FLOAT;
        }
        return sClassif;
    }
}