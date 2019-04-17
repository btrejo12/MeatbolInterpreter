package meatbol;

import javax.xml.transform.Result;
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
    private ArrayList<Token> out;

    public Expression(Parser parse, Scanner scanner, StorageManager storageMgr, SymbolTable st) {
        this.parser = parse;
        this.scan = scanner;
        this.storageMgr = storageMgr;
        this.st = st;
    }

    public ResultValue evaluateExpression(String terminatingToken) throws Exception{
        ArrayList<Token> exprTokens = new ArrayList<>();

        // Save expression tokens into an array list
        while (!terminatingToken.contains(scan.currentToken.tokenStr)){
            if(scan.currentToken.tokenStr.equals("(")){ //embedded parenthesis
                while (!scan.currentToken.tokenStr.equals(")")){
                    exprTokens.add(scan.currentToken);
                    scan.getNext();
                }
                exprTokens.add(scan.currentToken);
            }else
                exprTokens.add(scan.currentToken);
            scan.getNext();
        }
        ResultValue res;
        if (exprTokens.size() == 1){    // This is only one token, convert to RV and return
            try {
                Token token = exprTokens.get(0);
                if(token.subClassif == SubClassif.IDENTIFIER)
                    res = storageMgr.getVariableValue(token);
                else
                    res = new ResultValue(token.tokenStr, "primitive", token.subClassif);
                return res;
            } catch (Exception e) {
                System.out.println(terminatingToken);
                parser.error(e.getMessage() + scan.iSourceLineNr);
            }

        } //else if (exprTokens.size() == 2){     // Unary minus maybe?
        //    if(exprTokens.get(0)) == "-"
        //    parser.error("Expression only contains two tokens: ", Arrays.toString(exprTokens.toArray()));
        //}

        // Convert this expression to post fix
        ArrayList<Token> postfix = convertToPostfix(exprTokens);
        // Evaluate the expression and return its result
        res = evalPostfix(postfix);
        return res;
    }

    private ResultValue evalPostfix(ArrayList<Token> tokens) throws Exception{
        Stack<ResultValue> stack = new Stack<>();
        //System.out.print("\nEval:" + Arrays.toString(tokens.toArray()));
        while(!tokens.isEmpty()){
            Token token = tokens.remove(0);
            //System.out.print(token.tokenStr);
            switch(token.primClassif){
                case OPERAND:
                    //System.out.print("Operand.." + token.tokenStr);
                    if(isArray(token)){
                        //System.out.print("..is array\n");
                        if(!stack.isEmpty()) {
                            ResultValue value = getArrayValue(token, stack.pop());
                            stack.push(value);
                        } else {
                            ResultValue rv = storageMgr.getVariableValue(token);
                            stack.push(rv);
                        }
                    } else {
                        //System.out.println(token.tokenStr);
                        ResultValue rv = storageMgr.getVariableValue(token);
                        stack.push(rv);
                    }
                    break;
                case OPERATOR:
                    ResultValue res2 = stack.pop();
                    ResultValue res1;
                    if(stack.isEmpty()){
                        res2 = storageMgr.getUnaryVariableValue(res2.value);
                        stack.push(res2);
                    } else {
                        res1 = stack.pop();
                        Numeric num1 = new Numeric(parser, res1, token.tokenStr, "First operand");
                        Numeric num2 = new Numeric(parser, res2, token.tokenStr, "Second operand");
                        ResultValue res3 = parser.util.doMath(parser, num1, num2, token.tokenStr);
                        stack.push(res3);
                    }
                    break;
                case FUNCTION:
                    ResultValue functionReturn = handleFunction(token, stack.pop());
                    stack.push(functionReturn);
                    break;
                default:
                    parser.error("Invalid token: '", token.tokenStr, "'");
                    break;
            }
        }
        ResultValue finalRes = stack.pop();

        if(!stack.isEmpty())
             parser.error("Stack was expected to be empty after evaluating the postfix expr. last thing popped: "+finalRes.value+" next value on stack: " + stack.pop().value);
        return finalRes;
    }

    private ArrayList<Token> convertToPostfix(ArrayList<Token> tokens) throws Exception{
        Stack<Token> stack = new Stack<>();
        ArrayList<Token> out = new ArrayList<>();
        //System.out.print("\nConversion: " + Arrays.toString(tokens.toArray()));
        for (Token token: tokens){
            //System.out.print(token.tokenStr+" ");
            switch(token.primClassif){
                case OPERAND:
                    //System.out.print("Operand: " + token.tokenStr);
                    if(isArray(token)){
                        //System.out.print(" is array");
                        stack.push(token); // because array's are higher than everything
                    } else
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
                    break;
                case SEPARATOR:
                    switch(token.tokenStr){
                        case "(":
                            stack.push(token);
                            break;
                        case ")":
                            boolean parenthesisCheck = false;
                            while(!stack.isEmpty()){
                                Token popped = stack.pop();
                                if (popped.tokenStr.equals("(")){
                                    parenthesisCheck = true;
                                    if(isFunction(stack.peek()))
                                        out.add(stack.pop());
                                    break;
                                }
                                out.add(popped);
                            }
                            if (!parenthesisCheck)
                                parser.error("Did not find left parenthesis match in expression");
                            break;
                        case "[":
                            stack.push(token);
                            break;
                        case "]":
                            boolean bracketCheck = false;
                            while(!stack.isEmpty()){
                                Token popped = stack.pop();
                                if (popped.tokenStr.equals("[")){
                                    Token array = stack.pop();
                                    //System.out.println("Should be arr:" + array.tokenStr);
                                    out.add(array);
                                    bracketCheck = true;
                                    break;
                                }
                                out.add(popped);
                            }
                            if (!bracketCheck) {
                                parser.error("Did not find left bracket match in expression");
                            }
                            break;
                        //case "-":
                            //if()
                          //  stack.push(token);
                        default:
                            token.printToken();
                            parser.error("Invalid separator within expression: '"+token.tokenStr +"' ");
                            break;
                    } // seperator switch
                    break;
                case FUNCTION:
                    stack.push(token);
                    break;
                default:
                    parser.error("Invalid token within expression: ", token.tokenStr);
                    break;
            } // postfix switch
        } // for loop
        //Empty stack into out
        while(!stack.isEmpty()){
            Token token = stack.pop();
            out.add(token);
        }
        return out;
    }

    public boolean isArray(Token token) throws Exception{
        ResultValue rv = storageMgr.getVariableValue(token);
        if(rv.structure.equals("fixed-array"))
            return true;
        else
            return false;
    }

    public boolean isFunction(Token token) throws Exception{
        ResultValue rv = storageMgr.getVariableValue(token);
        if(rv.type == SubClassif.BUILTIN || rv.type == SubClassif.USER)
            return true;
        else
            return false;
    }

    private ResultValue handleFunction(Token function, ResultValue parameter) throws Exception{
        ResultValue rv = new ResultValue();
        switch(function.tokenStr){
            case "LENGTH":
                if(parameter.type != SubClassif.STRING)
                    parser.error("Function 'LENGTH' can only be used on String");
                return parameter.arr.stringLength();
            case "SPACES":
                if(parameter.type != SubClassif.STRING)
                    parser.error("Function'SPACES' can only be used on String");
                return parameter.arr.stringSpaces();
            case "ELEM":
                if(!parameter.structure.equals("fixed-array"))
                    parser.error("ELEM can only be used on arrays");
                return parameter.arr.elem();
            case "MAXELEM":
                if(!parameter.structure.equals("fixed-array"))
                    parser.error("MAXELEM can only be ued on arrays");
                return parameter.arr.maxelem();
            default:
                parser.error("Unknown function defined");
                break;
        }
        return rv;
    }

    private ResultValue getArrayValue(Token array, ResultValue index) throws Exception{
        ResultValue element = storageMgr.getVariableValue(array.tokenStr);
        //System.out.println("From Storage Manager..." + element.value);
        ResultValue rv = element.arr.get(index);
        //System.out.println("Array value is..." + rv.value);
        return rv;
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
    private boolean checkPrecedence(Token first, Token second) throws Exception {
        if(first.primClassif == Classif.SEPARATOR)
            return false;
        if(getPrecedence(first) >= getPrecedence(second))
            return true;
        return false;
    }

    private int getPrecedence(Token token) throws Exception{
      String [] precedence = {"and or", "not", "in notin", "<= >= != < > ==", "#", "+ -", "* /", "^", "U-", "(", "arr[ fun"};
      String tokenStr = token.tokenStr;

      if(token.subClassif == SubClassif.UNARY)
          tokenStr = "U-";
      else if(isArray(token))
          tokenStr = "arr[";
      else if(token.primClassif == Classif.FUNCTION)
          tokenStr = "fun";

      int index = -1;

      for(int i = 0; i< precedence.length; i++) {
          if(precedence[i].contains(tokenStr)){
              index = i;
              break;
          }
      }
      if(index == -1)
          parser.error("Invalid operator token", token.tokenStr);
      return index;
    }


    //TODO Delete me
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
