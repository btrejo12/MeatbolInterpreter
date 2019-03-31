package meatbol;

import javax.xml.transform.Result;

public class Parser {
    private Scanner scan;
    private StorageManager storageMgr;
    private SymbolTable st;
    private Utility util;

    private boolean bShowExpr = false;
    private boolean bShowAssign = false;

    public Parser(String filename, SymbolTable st){
        System.out.println("Parser entry");
        storageMgr = new StorageManager();
        util = new Utility();
        this.st = st;
        scan = new Scanner(filename, st);
        scan.sManager = storageMgr;
        try {
            while (scan.trigger){
                scan.getNext();

                debug();
                if (scan.currentToken.subClassif == SubClassif.FLOW){
                    //def, if, for, while,
                    if (scan.currentToken.tokenStr.equals("if")){
                        ifStmt(true);
                    } else if (scan.currentToken.tokenStr.equals("def")){
                        System.err.println("User defined functions are not being used in this programming assignment.");
                    } else if (scan.currentToken.tokenStr.equals("for")){
                        System.err.println("For loops are not being used in this programming assignment");
                    } else if (scan.currentToken.tokenStr.equals("while")){
                        whileStmt(true);
                    }
                    scan.currentToken.printToken(); //for debugging
                } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER){
                    // This is a variable being decalred only
                    if(scan.nextToken.primClassif == Classif.SEPARATOR){
                        System.out.println("Continue on seperator,");
                        continue;
                    } else if (scan.nextToken.primClassif == Classif.OPERATOR){
                        ResultValue assignmentResult = assignmentStmt(true);
                        //TODO: Assign the return value to the currentToken using Storage Manager
                    } else {
                        //Not sure here
                        scan.nextToken.printToken();
                    }
                } else if(scan.currentToken.primClassif == Classif.FUNCTION){
                        handleFunction();
                } else if (scan.currentToken.primClassif == Classif.DEBUG){
                        handleDebug();
                }
            }
        } catch(Exception e){
            //TODO: Call error method to create a ParserException
            e.printStackTrace();
        }
    }

    /**
     * This function is called from a nested if or while, in order to determine whether the nested condition should
     * run or not.
     * @param bExec the trigger to determine whether the nested block needs to be run based on the condition.
     * @return The ResultValue of...something
     */
    private ResultValue executeStatements(Boolean bExec){
        ResultValue res = new ResultValue();

        if(bExec){

        }

        return res;
    }

    private ResultValue expr(String endingDelimiter) throws Exception{
        //print("Hello from expr " + scan.currentToken.tokenStr);
        ResultValue res = new ResultValue();

        /**
         * Expression Cases:
         *      Unary minus (-A)
         *      Single Variable (A)
         *      Simple expression (A + B)
         *      Conditional?
         */

        // Unary minues
        if(scan.currentToken.primClassif == Classif.OPERATOR && scan.nextToken.subClassif == SubClassif.IDENTIFIER){
            // Negate the operand
            if(!scan.currentToken.tokenStr.equals("-")){ error("Unknown operator before operand"); }
            res = storageMgr.getUnaryVariableValue(scan.currentToken.tokenStr);
            scan.getNext();
        }
        // Single Variable
        else if (scan.currentToken.primClassif == Classif.OPERAND && endingDelimiter.contains(scan.nextToken.tokenStr)){
            if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                res = storageMgr.getVariableValue(scan.currentToken.tokenStr);
            } else if (scan.currentToken.subClassif == SubClassif.STRING){
                res = new ResultValue(scan.currentToken.tokenStr, "String", scan.currentToken.subClassif);
            }
            scan.getNext();
        }
        // Simple expression
        else if(scan.currentToken.subClassif == SubClassif.IDENTIFIER && scan.nextToken.primClassif == Classif.OPERATOR){
            print(scan.currentToken.tokenStr);
            String firstToken = scan.currentToken.tokenStr;
            scan.getNext(); //moves to operator -, +, etc
            if(scan.nextToken.primClassif != Classif.OPERAND){
                print(scan.nextToken.tokenStr);
                scan.nextToken.printToken();
                error("Expected second argument to be of type operand");
            }
            print(firstToken);
            print(scan.currentToken.tokenStr);
            print(scan.nextToken.tokenStr);
            ResultValue res1 = storageMgr.getVariableValue(firstToken);
            ResultValue res2 = storageMgr.getVariableValue(scan.nextToken.tokenStr);
            Numeric num1 = new Numeric(this, res1, scan.currentToken.tokenStr, "1st operand");
            Numeric num2 = new Numeric(this, res2,scan.currentToken.tokenStr, "2nd operand");
            res = util.doMath(this, num1, num2, scan.nextToken.tokenStr);
            showExpr(res);

        }
        // Who knows
        else {
            debug();
            error("Brenda you dumb hoe you shouldn't be here");
        }

        return res;
    }

    /**
     * This function is called when the grammer has type 'variable = expr ;'. The current token should be set to 'variable'.
     * @param bExec the trigger to determine whether the assignment statement should run (it may be inside a false if statement
     *              therefore we should not run it in this case)
     * @return the ResultValue of this assignment
     */
    private ResultValue assignmentStmt(Boolean bExec) throws Exception{
        //print("In assignmentStmt");
        ResultValue res = new ResultValue();
        if(!bExec){
            skipTo(';');
            return res;
        }
        if(scan.currentToken.subClassif != SubClassif.IDENTIFIER){
            error("Expected a variable for assignment");
        }

        String targetVariable = scan.currentToken.tokenStr;
        scan.getNext(); //Move current token to be on the operator
        if(scan.currentToken.primClassif != Classif.OPERATOR){
            error("Expected an assignment operator token after variable.");
        }

        //Declare variables that might need to be used
        ResultValue res1;
        ResultValue res2;
        Numeric num1;
        Numeric num2;

        switch(scan.currentToken.tokenStr){
            case "=":
                //print("Equal sign assignment");
                scan.getNext();
                res2 = expr(";");
                res = assign(targetVariable, res2);
                break;
            case "-=": // x -= 5+1;
                res2 = expr(";");
                num2 = new Numeric(this, res2, "-=", "2nd operator");
                res1 = storageMgr.getVariableValue(targetVariable);
                num1 = new Numeric(this, res1, "-=", "1st operator");
                res = assign(targetVariable, util.subtract(this, num1, num2));
                break;
            case "+=":
                res2 = expr(";");
                num2 = new Numeric(this, res2, "+=", "2nd operator");
                res1 = storageMgr.getVariableValue(targetVariable);
                num1 = new Numeric(this, res1, "+=", "1st operator");
                res = assign(targetVariable, util.add(this, num1, num2));
                break;
            default:
                print(scan.currentToken.tokenStr);

                error("Expected an assignment operator token after variable");
        }

        //TODO: Make sure this shows the variable and the value it was assigned
        showAssign(targetVariable, res);
        return res;
    }


    /**
     * This function is called when a outer most if statement is scanned. I don't think this is used in nexted if statements tho,
     * only the executeStatements method, so if you're calling from Parser's constructor then bExec should be set to true.
     * @param bExec the trigger whether to run the code inside of the if (based on the condition) or not.
     */
    private void ifStmt(boolean bExec) throws Exception {
        System.out.println("Inside If");

        //TODO: Delete this later
        scan.currentToken.printToken();

        //TODO: Delete next two lines. evalCond() will do this.
        // Move nextToken to currentToken so we can get rid of the initial "if"
        //scan.getNext();

        // test the condition in the if statement and execute if the condition is correct
        boolean testIfCond = evalCond();

        /* TODO: Delete this later
            if(scan.nextToken.subClassif != SubClassif.BOOLEAN) {
                scan.nextToken.printToken();
                error("Invalid if Statement");
            } else
                scan.nextToken.printToken();
            if(!evalCond()) {
                System.out.print("eval was false. ");
                while (!scan.nextToken.tokenStr.equals("endif")) {
                    if (scan.getNext() == "")
                        error("Reached end of file");
                    if(scan.currentToken.tokenStr.equals("else")) {
                        System.out.println("Found an else");
                        scan.getNext();
                        return;
                    }
                }
            } else {
                System.out.println("eval was true");
                //Return from function after finding end of line
                //while (!scan.nextToken.tokenStr.equals(":"))
                //    scan.getNext();
                return;
            }
            */
    }

    /**
     * This method is a little more tricky in that it should use Scanner's setPosition to figure out where to loop back to.
     * @param bExec
     */
    private void whileStmt(Boolean bExec){
        int colPos, lineNum;
        colPos = scan.iColPos;
        lineNum = scan.iSourceLineNr;

        System.out.println("Inside While");

        //scan.setPosition(lineNum,colPos);
    }

    /**
     * Once you return a result value, you should assign it to the left most variable (variableString) and return what you
     * assigned to it. This method should use StorageManager to check for data type.
     * @param variableString The variable string to be looked up in StorageManager to confirm data type and declaration.
     * @param res2 The ResultValue you're assigning to the variableString
     * @return The ResultValue that was assigned
     */
    private ResultValue assign(String variableString, ResultValue result) throws Exception{
        ResultValue res= new ResultValue();

        ResultValue target = storageMgr.getVariableValue(variableString);

        return res;
    }

    /**
     * This method is called when you find a control-flow variable (if, while) in order to determine whether the conditional
     * is true or false. This method should use getNext to be able execute the tokens and determine the boolean they result in.
     * @return The boolean value of whether this condition is true or false.
     */
    private boolean evalCond() throws Exception{

        //init variables
        ResultValue rv = null;
        String endDelim = "";
        boolean cond = true;

        if (scan.currentToken.tokenStr.equals("if")) {
            endDelim = "endif";
        } else if (scan.currentToken.tokenStr.equals("while")) {
            endDelim = "endwhile";
        } else {
            error("Unknown flow control statement", scan.currentToken.tokenStr);
        }

        //move the nextToken to currentToken to make shift out the (if, while) statement
        scan.getNext();

        // get the ResultValue based on the ending delimiter.
        rv = expr(endDelim);

        // test to see if the condition is T or F based on the returned ResultValue
        // first, make sure the ResultValue is indeed of type Boolean
        if (rv.type != SubClassif.BOOLEAN) {
            error("Invalid test. ReturnValue is not of type Boolean: " + rv.type);
        }

        if (rv.value.equals("T")) {
            cond = true;
        } else if (rv.value.equals("F")) {
            cond = false;
        } else {
            error("Error in Utility.java. ResultValue is of type Boolean, but does not return " +
                    "a Boolean value. Value: " + rv.value);
        }

        return cond;

        /* TODO: Delete this later
        if(scan.currentToken.tokenStr.equals(":"))
            error("Invalid condition statement");
        if(scan.currentToken.tokenStr.equals("!")) {
            scan.getNext();
            Boolean b = evalCond();
            b = !b;
            return b;
        } else if(scan.currentToken.tokenStr.equals("F"))
            return false;
        else if(scan.currentToken.tokenStr.equals("T"))
            return true;
        else if(scan.currentToken.subClassif == meatbol.SubClassif.IDENTIFIER){
            meatbol.STEntry stEntry = st.getSymbol(scan.currentToken.tokenStr);
            ResultValue rv = storageMgr.getVariableValue(stEntry.symbol);
            if(rv.type == meatbol.SubClassif.BOOLEAN){
                if(rv.value.equals("T"))
                    return true;
                else return false;
            }
        }
        else if(scan.currentToken.subClassif == meatbol.SubClassif.INTEGER) {
            //Check for mathematical comparison
            if(scan.nextToken.tokenStr.equals("<")){
                meatbol.Token token = new meatbol.Token();
                token.tokenStr = scan.currentToken.tokenStr;
                scan.getNext();
            }
            //
            if (Integer.parseInt(scan.currentToken.tokenStr) != 0)
                return true;
        }
        return false;
        */
    }

    /**
     * When you're skipping over something, it's nice to be able to use getNext() to traverse to the ending token,
     * which is ';' for assignmentStatement and ':' for control statements.
     * @param endingDelimiter
     */
    private void skipTo(Character endingDelimiter){
        //TODO: Move the tokens over until you reach the endingDelimiter
    }

    /**
     * When Scanner returns a built in function or user-defined function, it should be handled here.
     */
    private void handleFunction() throws Exception{
        if(scan.currentToken.tokenStr.equals("print")){
            printFunction();
        }
    }

    private void printFunction() throws Exception{
        scan.getNext();
        //Next token should be an open parenthesis
        if(!scan.currentToken.tokenStr.equals("(")){
            //TODO: Throw parser exception, open parenthesis was expected for print function
            System.err.println("Open parenthesis expected in print function");
            return;
        }
        scan.getNext();
        while(true) {
            if (scan.currentToken.subClassif == SubClassif.STRING) {
                //This is a string literal, we should print it
                System.out.print(scan.currentToken.tokenStr);
                scan.getNext();
            } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                ResultValue variableEval = expr(")"); //only evaluate this expression
                System.out.print(variableEval.value);
                scan.getNext();
            }
            if(scan.currentToken.tokenStr.equals(",")){
                System.out.print(" ");
                continue;
            }
            if(scan.currentToken.tokenStr.equals(")")){
                System.out.println();
                scan.getNext();
                break;
            }
            if (scan.currentToken.tokenStr.equals(";")){
                //TODO: Throw error, expected ')' instead got ';'
                System.err.println("Never recieved ending ')' in print statement");
                return;
            }
        }
    }

    /**
     * Clark's notes specify how to set up this error function, although the lineNumber and sourcefile part are confusing.
     * @param fmt
     * @param varArgs
     * @throws Exception
     */
    public void error(String fmt, Object...varArgs) throws Exception{
        String diagnosticTxt = String.format(fmt, varArgs);
        throw new ParserException(scan.currentToken.iSourceLineNr, diagnosticTxt, scan.sourceFileNm);
    }

    public void handleDebug() throws Exception{
        //current token is on debug
        scan.getNext();

        String command = scan.nextToken.tokenStr;
        if (scan.currentToken.tokenStr.equals("Expr")){
            if(command.equals("on")){
                bShowExpr = true;
            } else if(command.equals("off")){
                bShowExpr = false;
            } else {
                error("Unknown trigger for Expr, should be 'on' or 'off'");
            }
            scan.getNext(); //sits on trigger
        } else if (scan.currentToken.tokenStr.equals("Token")){
            if (command.equals("on")){
                scan.bShowToken = true;
            } else if (command.equals("off")){
                scan.bShowToken = false;
            } else {
                error("Unknown trigger for Token, should be 'on' or 'off'");
            }
            scan.getNext(); //sits on trigger
        } else if (scan.currentToken.tokenStr.equals("Assign")){
            if (command.equals("on")){
                bShowAssign = true;
            } else if (command.equals("off")){
                bShowAssign = false;
            } else {
                error("Unknown trigger for Token, should be 'on' or 'off'");
            }
            scan.getNext(); //sits on trigger
        } else {
            error("Unknown Debug Command");
        }
        if(scan.nextToken.tokenStr.equals(";")){
            error("Expected semicolon after debugging assignment");
        }
        scan.getNext(); //sits on ';'
    }

    public void showExpr(ResultValue result){
        if (bShowExpr){ System.out.print("\t\t..." + result.value);}
    }

    public void showAssign(String variable, ResultValue result){
        if (bShowAssign){ System.out.println("\t\t..." +variable + " = " + result.value);}
    }

    //TODO: delete me cause im layz
    private void print(String printMe){
        System.out.println(printMe);
    }

    //TODO: delete me cause im layz
    private void debug(){
        System.out.println("Current: " + scan.currentToken.tokenStr);
    }

}
