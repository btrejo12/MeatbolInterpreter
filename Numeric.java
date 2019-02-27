package meatbol;

public class Numeric {

    /**
     * <p>Numberic constructor that doesn't need to be used right now.</p>
     */
    public Numeric(){}

    /**
     * Scanner calls checkNumType through a Numeric object to validate if the passed-in variable
     * is a number. Tests for integer or float. Throws an exception if the formatting of the number is
     * incorrect. Returns the subClassif for the number being tested.
     *
     * @param variable for numeric testing
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