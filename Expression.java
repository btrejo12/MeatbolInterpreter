package meatbol;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

// For date validation
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

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

    /**
     * Evaluates the expression at the correct token
     * @param terminatingToken The token the expression stops at
     * @return The result of this expression
     * @throws Exception
     */
    public ResultValue evaluateExpression(String terminatingToken) throws Exception{
        ArrayList<Token> exprTokens = new ArrayList<>();

        // Save expression tokens into an array list
        while (!terminatingToken.contains(scan.currentToken.tokenStr)){
            if(scan.currentToken.tokenStr.equals("(")) { //embedded parenthesis
                exprTokens = embeddedParenthesis(exprTokens, ")");
            } else {
                exprTokens.add(scan.currentToken);
            }
            scan.getNext();
        }
        //System.out.println("\nConstructor: " + Arrays.toString(exprTokens.toArray()));
        ResultValue res;
        if(exprTokens.size() == 0){
            // Probably assigning an empty string to a variable
            res = new ResultValue("", "primitive", SubClassif.STRING);
            return res;
        }
        else if (exprTokens.size() == 1){    // This is only one token, convert to RV and return
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

    /**
     * Evaluates the postfix conversion
     * @param tokens The tokens of the postfix expression
     * @return The Result of this expression
     * @throws Exception
     */
    private ResultValue evalPostfix(ArrayList<Token> tokens) throws Exception{
        Stack<ResultValue> stack = new Stack<>();
        //System.out.print("\nEval:" + Arrays.toString(tokens.toArray()));
        while(!tokens.isEmpty()){
            Token token = tokens.remove(0);
            //token.printToken();
            //System.out.print(token.tokenStr);
            switch(token.primClassif){
                case OPERAND:
                    //System.out.print("Operand.." + token.tokenStr);
                    if(isArray(token)){
                        //System.out.print("..is array\n");
                        if(token.isArray) { // there's a index we need to get for this array
                            ResultValue value = new ResultValue();
                            value = getArrayValue(token, stack.pop());
                            stack.push(value);
                        } else { // otherwise its just a reference to this array
                            ResultValue rv = storageMgr.getVariableValue(token);
                            stack.push(rv);
                        }
                    } else if(isString(token)){
                        //If it's a string array reference, we need to get the value of it
                        if(token.isArray){
                            ResultValue value = getArrayValue(token, stack.pop());
                            stack.push(value);
                        }else { // Otherwise, we just want to push it on the stack
                            ResultValue value = storageMgr.getVariableValue(token);
                            stack.push(value);
                        }
                    }else {
                        //System.out.println(token.tokenStr);
                        ResultValue rv = storageMgr.getVariableValue(token);
                        stack.push(rv);
                    }
                    break;
                case OPERATOR:
                    ResultValue res2 = stack.pop();
                    ResultValue res1;
                    //System.out.println("Value: " + token.tokenStr + " SubClass: " + token.subClassif);
                    //System.out.println("res2: " + res2.value);
                    if(stack.isEmpty()){

                        if(token.subClassif == SubClassif.UNARY) {
                            res2 = storageMgr.getUnaryVariableValue(res2.value);
                            stack.push(res2);
                        } else if(token.tokenStr.equals("not")){
                             if(res2.value.equals("T"))
                                res2.value = "F";
                            else
                                res2.value = "T";
                            stack.push(res2);
                        }

                    } else {

                        if (token.subClassif == SubClassif.UNARY) {
                            //System.out.println("Top of stack: " + stack.peek());
                            ResultValue temp2 = storageMgr.getUnaryVariableValue(res2.value);
                            stack.push(temp2);
                        } else {

                            res1 = stack.pop();
                            //System.out.println(res1);
                            //System.out.println(res2);
                            Numeric num1 = new Numeric(parser, res1, token.tokenStr, "First operand");
                            Numeric num2 = new Numeric(parser, res2, token.tokenStr, "Second operand");
                            ResultValue res3 = parser.util.doMath(parser, num1, num2, token.tokenStr);
                            stack.push(res3);
                        }
                    }
                    break;
                case FUNCTION:
                    ArrayList<ResultValue> args = new ArrayList<ResultValue>();
                    STEntry sEntry = st.getSymbol(token.tokenStr);
                    if (sEntry == null) {
                        parser.error("Internal Error: Function does not exists", token);
                    }
                    STFunction sFunction = null;

                    if (sEntry instanceof STFunction) {
                        sFunction = (STFunction) sEntry;
                    } else {
                        parser.error("Internal error: Incorrect cast");
                    }

                    if (sFunction == null) {
                        parser.error("Symbol does not exist");
                    }
                    for (int i = 0; i < sFunction.numArgs; i++) {
                        args.add(stack.pop());
                    }

                    ResultValue functionReturn = handleFunction(token, args);
                    stack.push(functionReturn);
                    break;
                default:
                    parser.error("Invalid token: '" + token.tokenStr +"'");
                    break;
            }
        }
        ResultValue finalRes = stack.pop();

        if(!stack.isEmpty())
             parser.error("Stack was expected to be empty after evaluating the postfix expr." +
                     "Last thing popped: "+finalRes.value+" next value on stack: " + stack.pop().value);
        return finalRes;
    }

    /**
     * Converts the expression from infix to postfix
     * @param tokens The tokens to be converted
     * @return The postfix expression
     * @throws Exception
     */
    private ArrayList<Token> convertToPostfix(ArrayList<Token> tokens) throws Exception{
        Stack<Token> stack = new Stack<>();
        ArrayList<Token> out = new ArrayList<>();
        //System.out.print("\nConversion: " + Arrays.toString(tokens.toArray()));
        for (int i = 0; i < tokens.size(); i++){
            Token token = tokens.get(i);
            //System.out.print(token.tokenStr+" ");
            switch(token.primClassif){
                case OPERAND:
                    //System.out.print("Operand: " + token.tokenStr);
                    if(isArray(token)){
                        //System.out.print(" is array");
                        if(tokens.get(i+1).tokenStr.equals("["))
                            token.isArray = true;
                        stack.push(token); // because array's are higher than everything
                    } else if(isString(token)){
                        int index = i+1;
                        if( !(index >= tokens.size()) && tokens.get(index).tokenStr.equals("[")){
                            token.isArray = true;
                            stack.push(token);
                        }else {
                            out.add(token);
                        }
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
                        case ",":
                            break;
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

    /**
     * Used to handle embedded parenthesis within an expression
     * @param exprTokens The tokens in the expr so far
     * @param terminatingString The string to stop reading at, usually a closed parenthesis
     * @return The tokens passed in appended with the tokens seen here
     * @throws Exception
     */
    private ArrayList<Token> embeddedParenthesis(ArrayList<Token> exprTokens, String terminatingString) throws Exception{
        exprTokens.add(scan.currentToken);
        scan.getNext();
        while(!scan.currentToken.tokenStr.equals(terminatingString)){
            if(scan.currentToken.tokenStr.equals("("))
                exprTokens = embeddedParenthesis(exprTokens, terminatingString);
            else
                exprTokens.add(scan.currentToken);
            scan.getNext();
        }
        exprTokens.add(scan.currentToken);
        return exprTokens;
    }

    /**
     * Checks to see whether this token is an array by checking it's ResultValue
     * @param token The token to be checked
     * @return a boolean specifying whether it's an array or not
     * @throws Exception
     */
    public boolean isArray(Token token) throws Exception{
        ResultValue rv = storageMgr.getVariableValue(token);
        if(rv.structure.equals("fixed-array"))
            return true;
        else
            return false;
    }

    /**
     * Checks to see whether this token is a string array (versus a string literal)
     * @param token The token to be checked
     * @return a boolean specifying whether it's a String array or not
     * @throws Exception
     */
    public boolean isString(Token token) throws Exception{
        ResultValue rv = storageMgr.getVariableValue(token);
        if(rv.type == SubClassif.STRING)
            return true;
        else
            return false;
    }

    /**
     * Checks whether to see if this operand is a function or not
     * @param token The token to be checked
     * @return a boolean specifying whether it's a function or not
     * @throws Exception
     */
    public boolean isFunction(Token token) throws Exception{
        ResultValue rv = storageMgr.getVariableValue(token);
        if(rv.type == SubClassif.BUILTIN || rv.type == SubClassif.USER)
            return true;
        else
            return false;
    }

    /**
     * Performs the operations dependant on the type of function it is
     * @param function The token that contains the function
     * @param parameter The token the function will operate on
     * @return The ResultValue of the operation
     * @throws Exception
     */
    private ResultValue handleFunction(Token function, ArrayList<ResultValue> args) throws Exception{
        ResultValue rv = new ResultValue();
        STEntry sEntry = st.getSymbol(function.tokenStr);
        STFunction sFunction;

        if (sEntry instanceof STFunction) {
            sFunction = (STFunction) sEntry;
        }
        switch(function.tokenStr){
            case "LENGTH":
                //if(parameter.type != SubClassif.STRING)
                    //parser.error("Function 'LENGTH' can only be used on String");
                return args.remove(0).arr.stringLength();
            case "SPACES":
                if(args.get(0).type != SubClassif.STRING)
                    parser.error("Function'SPACES' can only be used on String");
                return args.remove(0).arr.stringSpaces();
            case "ELEM":
                if(!args.get(0).structure.equals("fixed-array"))
                    parser.error("ELEM can only be used on arrays");
                return args.remove(0).arr.elem();
            case "MAXELEM":
                if(!args.get(0).structure.equals("fixed-array"))
                    parser.error("MAXELEM can only be ued on arrays");
                return args.remove(0).arr.maxelem();
            case "dateDiff":
                rv = handleDateFunction(function.tokenStr, args.get(0), args.get(1));
                return rv;
            case "dateAdj":
                rv = handleDateFunction(function.tokenStr, args.get(0), args.get(1));
                return rv;
            case "dateAge":
                rv = handleDateFunction(function.tokenStr, args.get(0), args.get(1));
                return rv;
            default:
                parser.error("Unknown function defined");
                break;
        }
        return rv;
    }

    /**
     * Get the array and the specified index
     * @param array The array token
     * @param index The ResultValue index requested
     * @return The ResultValue of this array at the specified index
     * @throws Exception
     */
    private ResultValue getArrayValue(Token array, ResultValue index) throws Exception{
        ResultValue element = storageMgr.getVariableValue(array.tokenStr);
        //System.out.println("From Storage Manager..." + element.value);
        ResultValue rv = element.arr.get(index);
        //System.out.println("Array value is..." + rv.value + " at " + index.value + " index");
        return rv;
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

    /**
     * Literally what the function about is supposed to be
     * @param token The token who's precedence we're checking
     * @return Returns an integer representing the precedence value of this token
     * @throws Exception
     */
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

    public ResultValue evaluateDate() throws Exception {

        ResultValue rv = new ResultValue();
        // Make sure there's only one token, being the actual date value
        // throw an exception if there's an issue
        if (!scan.nextToken.tokenStr.equals(";")) {
            parser.error("Error: Unidentified token: ", scan.nextToken);
        }

        // Using Java functions, test to see if the date is valid.
        String dateToValidate = scan.currentToken.tokenStr;

        // Check the formatting of the date.
        this.checkDateFormatting();
        rv.type = SubClassif.DATE;
        rv.value = scan.currentToken.tokenStr;
        rv.structure = "primitive";
        return rv;
    }

    public void checkDateFormatting() throws Exception {
        String date = scan.currentToken.tokenStr;
        //System.out.println(date);
        checkDateFormatting(date);
    }

    public void checkDateFormatting(String date) throws Exception {
        //String date = scan.currentToken.tokenStr;
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);

        this.manualDateCheck(date);
        try {
            sdf.parse(date);
        } catch (Exception e){
            parser.error("Error: Date does not exist", scan.currentToken);
        }
    }

    public void manualDateCheck() throws Exception {
        String date = scan.currentToken.tokenStr;
        this.manualDateCheck(date);
    }

    public void checkIfDateVar(String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // loose checking
        ResultValue varDate = null;
        try {
            varDate = storageMgr.getVariableValue(date);
            if (varDate.type != SubClassif.DATE) {
                System.out.println(varDate.type);
                throw new Exception("Error: invalid data type");
            }
            return; // if it was successfully saved as a variable, this should be fine.
        } catch (Exception e) {
            try {
                sdf.parse(date);
            } catch (Exception f){
                parser.error("Input is not a date");
            }
        }
    }
    public void manualDateCheck(String date) throws Exception {
        int year=0, month=0, day=0;
        checkIfDateVar(date);
        int[] daysOftheYear = {
                0, 31, 29, 31
                , 30, 31, 30
                , 31, 31, 30
                , 31, 30, 31
        };

        // SANITY CHECK: Manually check the format of the date
        // First, lets make sure the token string is of length 10
        if (date.length() != 10) {
            parser.error("Date value must contain 10 characters", scan.currentToken);
        }

        int hyphenCount = 0;
        for (int i = 0; i < date.length(); i++) {
            if (date.charAt(i) == '-')
                hyphenCount++;
        }

        //Check the string to see if it has 2 hyphens
        if (hyphenCount != 2) {
            parser.error("Error, invalid date syntax", scan.currentToken);
        }

        String[] valToChar = date.split("-");
        //validate that the input is of type int
        try {
            year = Integer.parseInt(valToChar[0]);
            month = Integer.parseInt(valToChar[1]);
            day = Integer.parseInt(valToChar[2]);
        } catch (Exception e) {
            parser.error("Error, invalid date syntax", scan.currentToken);
        }

        // Check to make sure each value is of a correct length
        if (valToChar[0].length() != 4) {
            parser.error("1Error, invalid date syntax", date);
        }

        if (valToChar[1].length() != 2) {
            parser.error("2Error, invalid date syntax", date);
        }

        if (valToChar[2].length() != 2) {
            parser.error("3Error, invalid date syntax", date);
        }

        // Test the months
        if (month < 1 || month > 12)
            parser.error("Error, date does not exist: ", date);

        // Test the days. Each month has a specific day, specified by daysOfTheYear[]
        if (day < 1 || day > daysOftheYear[month])
            parser.error("Error, date does not exist: ", date);

        // if the month is Feb and the day is the 29th, make sure its a leap year
        if (day == 29 && month == 2) {
            if (!(year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)))
                parser.error("Error, date does not exist: ", date);
        }
    }

    public ResultValue handleDateFunction(String func, ResultValue arg1, ResultValue arg2) throws Exception{
        ResultValue rv = new ResultValue();
        //checkDateFormatting(arg1.value);
        checkDateFormatting(arg2.value);
        switch(func){
            case "dateDiff":
                checkDateFormatting(arg1.value);
                rv = dateDiff(arg2, arg1);
                return rv;
            case "dateAge":
                checkDateFormatting(arg1.value);
                rv = yearDiff(arg1, arg2);
                return rv;
            case "dateAdj":
                //System.out.println("arg1: " + arg1.value + " arg2: " + arg2.value);
                rv = this.adjustDate(arg1, arg2);
                return rv;
            default:
                parser.error("In HandleDateFunction: HOW ARE YOU HERE?");
        }
        return rv;
    }

    public ResultValue dateDiff(ResultValue laterDt, ResultValue earlyDt) throws Exception{
        // [0] -> year; [1] -> month; [2] -> day
        ResultValue rv = new ResultValue();
        int days1 = this.convertTotalDays(earlyDt);
        int days2 = this.convertTotalDays(laterDt);
        int diff = days2 - days1;

        rv.structure = "primitive";
        rv.value = Integer.toString(diff);
        rv.type = SubClassif.INTEGER;
        return rv;
    }

    public ResultValue yearDiff(ResultValue laterDt, ResultValue earlyDt) throws Exception {
        ResultValue rv = new ResultValue();
        rv.type = SubClassif.INTEGER;
        rv.structure = "primitive";

        // Configure our formatter
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        // Configure our calendars
        Calendar a = Calendar.getInstance();
        Calendar b = Calendar.getInstance();

        a.setLenient(false);
        b.setLenient(false);

        try {
            Date d1 = sdf.parse(earlyDt.value);
            Date d2 = sdf.parse(laterDt.value);

            a.setTime(d1);
            b.setTime(d2);
        } catch (Exception e) {
            parser.error("Error in date data");
        }

        int diff = a.get(Calendar.YEAR) - b.get(Calendar.YEAR);
        if (b.get(Calendar.MONTH) > a.get(Calendar.MONTH) ||
                (b.get(Calendar.MONTH) == a.get(Calendar.MONTH) && b.get(Calendar.MONTH) > a.get(Calendar.MONTH))) {
            diff--;
        }

        rv.value = String.valueOf(diff);
        return rv;

    }

    public ResultValue adjustDate(ResultValue adjust, ResultValue dateToAdjust) throws Exception {

        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String[] valueDt = dateToAdjust.value.split("-");
        Calendar cal = null;
        int adjDays = 0;

        try {

            // setup Calendar
            cal = new GregorianCalendar();
            cal.setTime(sdf.parse(dateToAdjust.value));

            // parse adjust's value into type Int
            adjDays = Integer.parseInt(adjust.value);

        } catch (Exception e) {
            parser.error("Error: First parameter in dateAdj must be of type int");
        }

        //adjust the date
        cal.add(Calendar.DAY_OF_MONTH, adjDays);
        dateToAdjust.value = sdf.format(cal.getTime());

        return dateToAdjust;
    }

    public int convertTotalDays(ResultValue date) throws Exception {
        // [0] -> year; [1] -> month; [2] -> day
        String[] dateTokens = date.value.split("-");
        int day = 0;
        int year = 0;
        int month = 0;
        int toDays = 0;

        try {
            year = Integer.parseInt(dateTokens[0]);
            month = Integer.parseInt(dateTokens[1]);
            day = Integer.parseInt(dateTokens[2]);
        } catch (Exception e) {
            throw new Exception("You shan't be here");
        }

        if (day > 2) {
            day -= 3;
        } else {
            day += 9;
            year--;
        }

        toDays = 365 * year + year / 4 - year / 100 + year / 400
                + (month * 306 + 5) / 10 + day;
        return toDays;
    }
}
