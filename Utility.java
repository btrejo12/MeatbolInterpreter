package meatbol;

import javax.xml.transform.Result;

public class Utility {
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
            default:
                res = compareNums(parser, n1, n2, operator);
                break;
                // will throw exception if there is an unrecognizable operator
                //throw new Exception("Unrecognizable operator!");
        }
        return res;
    }

    public ResultValue compareNums(Parser parser, Numeric n1, Numeric n2, String operator) throws Exception {
        String[] operators = {"==", "!=", "<", "<=", ">", ">="};
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

    public SubClassif findHighestOrder(Numeric n1, Numeric n2){
        //if(n1.type == SubClassif.FLOAT || n2.type == SubClassif.FLOAT)
        //    return SubClassif.FLOAT;
        if(n1.type == SubClassif.FLOAT)
            return SubClassif.FLOAT;
        return SubClassif.INTEGER;
    }
}
