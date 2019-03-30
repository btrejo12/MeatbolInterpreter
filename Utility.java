package meatbol;

import javax.xml.transform.Result;

public class Utility {
    public ResultValue subtract(Parser parser, Numeric n1, Numeric n2){
       ResultValue result = new ResultValue();
       result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);
        if(result.type == SubClassif.INTEGER)
            result.value = String.valueOf(n1.integerValue - n2.integerValue);
        else
            result.value = String.valueOf(n1.floatValue - n2.floatValue);
       return result;
    }
    public ResultValue add(Parser parser, Numeric n1, Numeric n2) {
        ResultValue result = new ResultValue();
        result.type = findHighestOrder(n1,n2);
        result.structure = "primitive";
        if(result.type == SubClassif.INTEGER)
            result.value = String.valueOf(n1.integerValue + n2.integerValue);
        else
            result.value = String.valueOf(n1.floatValue + n2.floatValue);
        return result;
    }
    public ResultValue multiply(Parser parser, Numeric n1, Numeric n2) {
        ResultValue result = new ResultValue();
        result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);
        if(result.type == SubClassif.INTEGER)
            result.value = String.valueOf(n1.integerValue * n2.integerValue);
        else
            result.value = String.valueOf(n1.floatValue * n2.floatValue);
        return result;
    }
    public ResultValue divide(Parser parser, Numeric n1, Numeric n2) {
        ResultValue result = new ResultValue();
        result.structure = "primitive";
        result.type = findHighestOrder(n1,n2);
        if(result.type == SubClassif.INTEGER)
            result.value = String.valueOf(n1.integerValue / n2.integerValue);
        else
            result.value = String.valueOf(n1.floatValue / n2.floatValue);
        return result;
    }

    public ResultValue doMath(Parser parser, Numeric n1, Numeric n2, String operand) throws Exception{
        ResultValue res;
        switch(operand){
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
            default:
                throw new Exception("Unrecognizable operand!");
        }
        return res;
    }

    public SubClassif findHighestOrder(Numeric n1, Numeric n2){
        if(n1.type == SubClassif.FLOAT || n2.type == SubClassif.FLOAT)
            return SubClassif.FLOAT;
        return SubClassif.INTEGER;
    }
}
