package meatbol;

import javax.xml.transform.Result;

public class Utility {

    /**
     * <p>Subtracts two numeric objects and returns their result</p>
     * @param parser    The parser object that this function was called from
     * @param n1        The first numeric operand
     * @param n2        The second numeric operand
     * @return          The result saved in a ResultValue object
     */
    public ResultValue subtract(Parser parser, Numeric n1, Numeric n2){
       ResultValue result = new ResultValue();
       result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);
        // Result should be of type integer
        // Cast to type integer
        if(result.type == SubClassif.INTEGER) {
            if(n1.type != n2.type){
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf((int)(n1.floatValue-n2.integerValue));
                else
                    result.value = String.valueOf((int)(n1.integerValue-n2.floatValue));
            } else {
                result.value = String.valueOf(n1.integerValue-n2.integerValue);
            }
        }
        // Cast to float
        else {
            if(n1.type != n2.type){         // Different types, find their value
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf(n1.floatValue-n2.integerValue);
                else
                    result.value = String.valueOf(n1.integerValue-n2.floatValue);
            } else {
                result.value = String.valueOf(n1.floatValue-n2.floatValue);
            }
        }
       return result;
    }

    /**
     * <p>Adds two numeric objects and returns their result</p>
     * @param parser    The parser object that this function was called from
     * @param n1        The first numeric operand
     * @param n2        The second numeric operand
     * @return          The result saved in a ResultValue object
     */
    public ResultValue add(Parser parser, Numeric n1, Numeric n2) {
        ResultValue result = new ResultValue();
        result.type = findHighestOrder(n1,n2);
        result.structure = "primitive";
        // Cast to type integer
        if(result.type == SubClassif.INTEGER) {
            if(n1.type != n2.type){
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf((int)(n1.floatValue+n2.integerValue));
                else
                    result.value = String.valueOf((int)(n1.integerValue+n2.floatValue));
            } else {
                result.value = String.valueOf(n1.integerValue+n2.integerValue);
            }
        }
        // Cast to float
        else {
            if(n1.type != n2.type){         // Different types, find their value
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf(n1.floatValue+n2.integerValue);
                else
                    result.value = String.valueOf(n1.integerValue+n2.floatValue);
            } else {
                result.value = String.valueOf(n1.floatValue+n2.floatValue);
            }
        }
        return result;
    }

    /**
     * <p>Multiply two numeric objects and returns their result</p>
     * @param parser    The parser object that this function was called from
     * @param n1        The first numeric operand
     * @param n2        The second numeric operand
     * @return          The result saved in a ResultValue object
     */
    public ResultValue multiply(Parser parser, Numeric n1, Numeric n2) {
        ResultValue result = new ResultValue();
        result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);
        // Cast to type integer
        if(result.type == SubClassif.INTEGER) {
            if(n1.type != n2.type){
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf((int)(n1.floatValue*n2.integerValue));
                else
                    result.value = String.valueOf(n1.integerValue*(int)n2.floatValue);
            } else {
                result.value = String.valueOf(n1.integerValue*n2.integerValue);
            }
        }
        // Cast to float
        else {
            if(n1.type != n2.type){         // Different types, find their value
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf(n1.floatValue*n2.integerValue);
                else
                    result.value =  String.valueOf(n1.integerValue*(int)n2.floatValue);
            } else {
                result.value = String.valueOf(n1.floatValue*n2.floatValue);
            }
        }
        return result;
    }

    /**
     * <p>Divide two numeric objects and returns their result</p>
     * @param parser    The parser object that this function was called from
     * @param n1        The first numeric operand
     * @param n2        The second numeric operand
     * @return          The result saved in a ResultValue object
     */
    public ResultValue divide(Parser parser, Numeric n1, Numeric n2) {
        ResultValue result = new ResultValue();
        result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);
        // Cast to type integer
        if(result.type == SubClassif.INTEGER) {
            if(n1.type != n2.type){
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf((int)(n1.floatValue/n2.integerValue));
                else
                    result.value = String.valueOf((n1.integerValue/(int)n2.floatValue));
            } else {
                result.value = String.valueOf(n1.integerValue/n2.integerValue);
            }
        }
        // Cast to float
        else {
            if(n1.type != n2.type){         // Different types, find their value
                if(n1.type == SubClassif.FLOAT)
                    result.value = String.valueOf(n1.floatValue/n2.integerValue);
                else
                    result.value = String.valueOf(n1.integerValue/(int)n2.floatValue);
            } else {
                result.value = String.valueOf(n1.floatValue/n2.floatValue);
            }
        }
        return result;
    }

    private ResultValue stringConcatenation(Parser parse, Numeric n1, Numeric n2)throws Exception{
        if(n1.type != SubClassif.STRING){
            parse.error("Expected first operand to be of type string when using '#' operator. Operand: ", n1.strValue);
        }
        String value = n1.strValue + n2.strValue;
        return new ResultValue(value, "primitive", SubClassif.STRING);
    }

    /**
     * <p>When the math operation is unknown, this function figures it out.</p>
     * @param parser    The parser object that this function was called from
     * @param n1        The first numeric object of this operation
     * @param n2        The second numeric object of this operation
     * @param operator  The operator that must be applied to the numeric objects
     * @return          The ResultValue that this operation produced
     * @throws Exception
     */
    public ResultValue doMath(Parser parser, Numeric n1, Numeric n2, String operator) throws Exception{
        ResultValue res;
        switch(operator){
            //arithemetic cases
            case "+":
               res = add(parser, n1, n2);
                break;
            case "-":
                res = subtract(parser, n1, n2);
                break;
            case "*":
                res = multiply(parser, n1, n2);
                break;
            case "/":
                res = divide(parser, n1, n2);
                break;
                // conditional cases
            case "^":
                res = exponentiate(parser, n1, n2);
                break;
            case "#":
                res = stringConcatenation(parser, n1, n2);
                break;
            default:
                res = compareNums(parser, n1, n2, operator);
                break;
                // will throw exception if there is an unrecognizable operator
                //throw new Exception("Unrecognizable operator!");
        }
        return res;
    }

    /**
     * <p>When we have to do String comparisons this function will do the evaluating</p>
     * @param s1    The first string to be compared
     * @param s2    The second string to be compared
     * @param operator  The operation done on the strings
     * @return      The ResultValue that was produced from the comparison
     * @throws Exception
     */
    public ResultValue compareStrings(Parser parser, Numeric s1, Numeric s2, String operator) throws Exception {
        boolean isTrue = true;
        ResultValue res = new ResultValue();

        if (operator.equals("==")) {
            isTrue = s1.strValue.equals(s2.strValue);
        } else if (operator.equals("!=")) {
            isTrue = !s1.strValue.equals(s2.strValue);
        } else {
            isTrue = stringComparison(parser, s1, s2, operator);
        }

        res.type = SubClassif.BOOLEAN;
        if (isTrue) {
            res.value = "T";
        } else {
            res.value = "F";
        }
        res.structure = "primitive";
        return res;
    }

    /**
     * A Python like function to support String comparisons in Meatbol
     * @param parser Parser, duh
     * @param s1 First string in comparison
     * @param s2 Second string in comparison
     * @param operator The operation to be performed on said string
     * @return uhm, i forget tbh
     * @throws Exception
     */
    private boolean stringComparison(Parser parser, Numeric s1, Numeric s2, String operator) throws Exception {
        String first = s1.strValue;
        String second = s2.strValue;

        String [] operators = {">", "<", ">=", "<="};

        int i;
        for(i = 0; i < operators.length; i++){
            if(operator.equals(operators[i]))
                break;
        }

        if(i == operators.length)
            parser.error("Invalid String operator: ", operator);

        int size = Math.min(first.length(), second.length());

        for(int j = 0; j < size; j++){
            char left = first.charAt(j);
            char right = second.charAt(j);

            switch(i){
                case 0: // >
                    if (left == right)
                        continue;
                    else if (left > right)
                        return true;
                    else
                        return false;
                case 1: // <
                    if (left == right)
                        continue;
                    else if (left < right)
                        return true;
                    else
                        return false;
                case 2: // >=
                    if(left == right)
                        continue;
                    else if (left > right)
                        return true;
                    else
                        return false;
                case 3: // <=
                    if (left == right)
                        continue;
                    else if ( left < right)
                        return true;
                    else
                        return false;
            }

        }
        return true;
    }

    /**
     * <p>When comparing Numerics, this function applies the appropriate comparison and returns the result</p>
     * @param parser    The parser object that saw the comparison
     * @param n1        The first Numeric in the operation
     * @param n2        The second Numeric in the operation
     * @param operator  The operation to be applied to the Numerics
     * @return          The ResultValue this comparison produced (boolean)
     * @throws Exception
     */
    public ResultValue compareNums(Parser parser, Numeric n1, Numeric n2, String operator) throws Exception {
        String[] operators = {"==", "!=", "<", "<=", ">", ">=", "or", "and", "not"};
        ResultValue rv = new ResultValue();
        boolean compare;

        // using floats as placeholders
        float num1 = 0;
        float num2 = 0;

        // If the operator is not recognized, it is an unknown operator
        int i = 0;
        for (i = 0; i < operators.length; i++) {
            if (operators[i].equals(operator)) {
                break;
            }
        }

        if (i == operators.length) {
            throw new Exception("Unknown operator: " + operator);
        }

        // if it is a String comparison, call compareStrings();
        if (n1.type == SubClassif.STRING) {
            return compareStrings(parser, n1, n2, operator);
        }
        // cast n1's value to a num1 (float) regardless if its an int or float
        if (n1.type == SubClassif.INTEGER) {
            num1 = (float) n1.integerValue;
        } else {
            num1 = n1.floatValue;
        }

        // cast n2's value to a num2 (float) regardless if its an int or float
        if (n2.type == SubClassif.INTEGER) {
            num2 = (float) n2.integerValue;
        } else {
            num2 = n2.floatValue;
        }

        switch(operator){
            case "==":
                compare = (num1 == num2);
                break;
            case "!=":
                compare = (num1 != num2);
                break;
            case "<":
                compare = (num1 < num2);
                break;
            case "<=":
                compare = (num1 <= num2);
                break;
            case ">":
                compare = (num1 > num2);
                break;
            case ">=":
                compare = (num1 >= num2);
                break;
            case "or":
                compare = n1.boolValue || n2.boolValue;
                break;
            case "and":
                compare = n1.boolValue && n2.boolValue;
                break;
            case "not":
                compare = !n1.boolValue;
                break;
            default:
                throw new Exception("Error in operator identity: " + operator);
        }

        // set rv's values
        if (compare) {
            rv.value = "T";
        } else {
            rv.value = "F";
        }
        rv.type = SubClassif.BOOLEAN;
        rv.structure = "primitive";
        return rv;
    }

    /**
     * <p>Exponentiated the numerics</p>
     * @param parser    The parser object that saw the operation
     * @param n1        The base of the exponential equation
     * @param n2        The exponent in the exponential equation
     * @return          The ResultValue from the exponential equation
     */
    public ResultValue exponentiate(Parser parser, Numeric n1, Numeric n2) {

        ResultValue result = new ResultValue();
        result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);

        if(result.type == SubClassif.INTEGER) {
            result.value = String.valueOf((int)(Math.pow(n1.integerValue, n2.integerValue)));
        } else {
            result.value = String.valueOf(Math.pow(n1.floatValue, n2.floatValue));
        }

        return result;
    }

    /**
     * <p>When comparing two Numerics of different types, the highest order must be decided</p>
     * @param n1    The first numeric of the operation
     * @param n2    The second numeric of the operation
     * @return      The SubClassification of the highest order that was produced
     */
    public SubClassif findHighestOrder(Numeric n1, Numeric n2){
        //if(n1.type == SubClassif.FLOAT || n2.type == SubClassif.FLOAT)
        //    return SubClassif.FLOAT;
        if(n1.type == SubClassif.FLOAT)
            return SubClassif.FLOAT;
        return SubClassif.INTEGER;
    }
}
