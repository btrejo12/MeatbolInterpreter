package meatbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Expression {
    private Scanner scan;
    private StorageManager storageMgr;
    private SymbolTable st;
    private Parser parser;
    private Utility util;
    private final static String operators = "+-*/<>=!#^";
    private Out out;

    public Expression(Parser parse, Scanner scanner, StorageManager storageMgr, SymbolTable st) {
        this.parser = parse;
        this.scan = scanner;
        this.storageMgr = storageMgr;
        this.st = st;
    }

    public ResultValue evaluateExpression(String terminatingToken) throws Exception{
        /**
         * Possible situations for expression to handle:
         *      Evaluating assignment statements, e.g. Int i = 3*4+5;
         *      Evaluating conditions, e.g. if 2*4 > 6*5:
         *      Print functions e.g. print("Hello", i, 5+6);
         *      Array size declarations, e.g. Int array[variable]
         *      (Maybe) Array populating, e.g. array[] = 10, 20, 30, 50;
         *
         *      1st Step:
         *          We don't want to use Scanner's get next while converting, so save
         *          all the elements of this expression into an ArrayList
         *      2nd Step:
         *          Convert the expression to postfix if the length is greater than 1,
         *          we need to be careful here because it might be unary minus for expressions
         *          of length two.
         *      3rd Step:
         *          Evaluate the postfix expression and return the result
         */
        ArrayList<Token> exprTokens = new ArrayList<Token>();

        // Save expression tokens into an array list
        while (!terminatingToken.contains(scan.nextToken.tokenStr)){
            exprTokens.add(scan.currentToken);
        }

        if (exprTokens.size() == 1){    // This is only one token, convert to RV and return
            //TODO: Convert this token to a RV and return it

        } else if (exprTokens.size() == 2){     // Unary minus maybe?
            parser.error("Expression only contains two tokens: ", Arrays.toString(exprTokens.toArray()));
        }

        // Convert this expression to post fix
        ArrayList<Token> postfix = convertToPostfix(exprTokens);
        // Evaluate the expression and return its result
        ResultValue res = evalPostfix(postfix);
        return res;
    }

    private ResultValue evalPostfix(ArrayList<Token> tokens){
        Stack<Token> stack = new Stack<Token>();

        while(!tokens.isEmpty()){

        }
    }

    private ArrayList<Token> convertToPostfix(ArrayList<Token> tokens) throws Exception{
        Stack<Token> stack = new Stack();
        ArrayList<Token> out = new ArrayList<Token>();

        for (Token token: tokens){
            switch(token.primClassif){
                case OPERAND:
                    out.add(token);
                    break;
                case OPERATOR:
                    if (stack.isEmpty()) {
                        stack.push(token);
                        break;
                    } else {
                        if (checkPrecedence(stack.peek(), token)){     // Returns true if what
                            Token popped = stack.pop();                // is in the stack
                            stack.push(token);                         // has higher precedence
                            out.add(popped);
                        } else {
                            stack.push(token);
                        }
                    }
                case SEPARATOR:
                    switch(token.tokenStr){
                        case "(":
                            stack.push(token);
                            break;
                        case ")":
                            boolean parenthesisCheck = false;
                            while(!stack.isEmpty()){
                                Token popped = stack.pop();
                                if (popped.equals("(")){
                                    parenthesisCheck = true;
                                    break;
                                }
                                out.add(popped);
                            }
                            if (!parenthesisCheck)
                                parser.error("Did not find left parenthesis match in expression");
                            break;
                        default:
                            parser.error("Invalid separator within expression");
                            break;
                    } // seperator switch
                    break;
                default:
                    parser.error("Invalid token within expression: ", token.tokenStr);
                    break;
            } // postfix switch
        } // for loop
        return out;
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

    /**
     * Check if the first operator has a higher precedence than the second operator
     * @param first The first operator in the expression
     * @param second The second operator in the expression
     * @return True if the first operator has a higher precedence, false if not or same
     */
    private boolean checkPrecedence(Token first, Token second) throws Exception{
        String[] arrlist = {"^","*/", "+-", "#", "<=>=!=<>==", "not", "andor"};

        int firstIndex= -1;
        int secondIndex= -1;

        for(int i = 0; i < arrlist.length; i++){
            if(arrlist[i].contains(first.tokenStr)){
                firstIndex = i;
            }
            if (arrlist[i].contains(second.tokenStr)){
                secondIndex = i;
            }
        }

        if(firstIndex == -1 || secondIndex == -1)
            parser.error("Invalid operator token for either", first.tokenStr, " or ", second.tokenStr);

        if(firstIndex < secondIndex)
            return true;
        else
            return false;
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
