package meatbol;

import jdk.jshell.spi.ExecutionControlProvider;

import java.util.ArrayList;

public class Expression {
    private Scanner scan;
    private StorageManager storageMgr;
    private SymbolTable st;
    private Utility util;
    private final static String operators = "+-*/<>=!#^";
    //private ArrayList<Token> fullExpression;
    private Out out;
    private Stack stack;

    public Expression(Scanner scanner, StorageManager storageMgr, SymbolTable st) {
        this.scan = scanner;
        this.storageMgr = storageMgr;
        this.st = st;
    }




    private ArrayList<Token> getExpression(String delimiter) throws Exception {
        ArrayList<Token> fullExpression = new ArrayList<>();
        while(scan.nextToken.tokenStr != delimiter){
            fullExpression.add(scan.currentToken);
            //try {
                scan.getNext();
            //} catch(Exception e){
                //throw new ParserException();
            //}
        }
        return fullExpression;
    }


    private ResultValue popOut(ArrayList<Token> fullExpression) {
        return new ResultValue();
    }
    /*
    private ResultValue expr(String endingDelimiter) throws Exception{

        ResultValue res = new ResultValue();
        String expr = "... ";

        // Unary minus
        if(scan.currentToken.primClassif == Classif.OPERATOR && scan.nextToken.subClassif == SubClassif.IDENTIFIER){
            // Negate the operand
            if(!scan.currentToken.tokenStr.equals("-")){ error("Unknown operator before operand"); }
            res = storageMgr.getUnaryVariableValue(scan.nextToken.tokenStr);
            scan.getNext();
        }
        // Single Variable
        else if (scan.currentToken.primClassif == Classif.OPERAND && endingDelimiter.contains(scan.nextToken.tokenStr)){
            if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                res = storageMgr.getVariableValue(scan.currentToken.tokenStr);
            }
            else {
                res = new ResultValue(scan.currentToken.tokenStr, "primitive", scan.currentToken.subClassif);
            }
            scan.getNext();
        }
        // Simple expression
        else if(scan.currentToken.primClassif == Classif.OPERAND && scan.nextToken.primClassif == Classif.OPERATOR){
            //print(scan.currentToken.tokenStr);
            String firstToken = scan.currentToken.tokenStr;
            ResultValue res1;
            ResultValue res2;

            if(scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                res1 = new ResultValue(scan.currentToken.tokenStr, "primitive", scan.currentToken.subClassif);
            else
                res1 = storageMgr.getVariableValue(scan.currentToken.tokenStr);

            scan.getNext(); //moves to operator -, +, etc
            if(scan.nextToken.primClassif != Classif.OPERAND){

                scan.nextToken.printToken();
                error("Expected second argument to be of type operand");
            }

            if(scan.nextToken.subClassif != SubClassif.IDENTIFIER)
                res2 = new ResultValue(scan.nextToken.tokenStr, "primitive", scan.nextToken.subClassif);
            else
                res2 = storageMgr.getVariableValue(scan.nextToken.tokenStr);
            Numeric num1 = new Numeric(this, res1, scan.currentToken.tokenStr, "1st operand");
            Numeric num2 = new Numeric(this, res2,scan.currentToken.tokenStr, "2nd operand");
            res = util.doMath(this, num1, num2, scan.currentToken.tokenStr);
            expr = expr+res1.value+" "+scan.currentToken.tokenStr+" "+res2.value;
            scan.getNext();
            showExpr(expr,res);
        }
        // Unknown error
        else {
            error("Cannot recognize expression statement. Current token: " + scan.currentToken.tokenStr + " on line " + scan.currentToken.iSourceLineNr);
        }
        return res;
    }
}*/
}
