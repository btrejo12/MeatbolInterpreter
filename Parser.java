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
        storageMgr = new StorageManager();
        util = new Utility();
        this.st = st;
        scan = new Scanner(filename, st);
        scan.sManager = storageMgr;
        try {
            while (scan.trigger){
                scan.getNext();

                //debug();
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
                    //scan.currentToken.printToken(); //for debugging
                } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER){
                    // This is a variable being decalred only
                    if(scan.nextToken.primClassif == Classif.SEPARATOR){
                        continue;
                    } else if (scan.nextToken.primClassif == Classif.OPERATOR){
                        ResultValue assignmentResult = assignmentStmt(true);
                        //TODO: Assign the return value to the currentToken using Storage Manager
                    } else {
                        //Not sure here
                        scan.nextToken.printToken();
                    }
                } else if(scan.currentToken.primClassif == Classif.FUNCTION){
                        handleFunction(true);
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
    private ResultValue executeStatements(Boolean bExec) throws Exception{
        ResultValue res = new ResultValue();

        if (!scan.currentToken.tokenStr.equals(":"))
            error("Expected a ':' currentToken value is " + scan.currentToken.tokenStr);

        // shift so the currentToken is the first token after the :
        scan.getNext();

        while (scan.currentToken.subClassif != SubClassif.END) {
            debug();
            /****** We're checking for subClassifs ******/

            // Nested if/while
            if (scan.currentToken.subClassif == SubClassif.FLOW) {
                if (scan.currentToken.tokenStr.equals("if")) {
                    ifStmt(bExec);
                } else if (scan.currentToken.tokenStr.equals("while")) {
                    whileStmt(bExec);
                } else {
                    error("Brenda wtf is wrong with you there's only 2 flows:" + scan.currentToken.tokenStr);
                }
            } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                // This is just declaring a variable
                if (scan.nextToken.primClassif == Classif.SEPARATOR) {
                    continue;
                } else if (scan.nextToken.primClassif == Classif.OPERATOR) { // assigning a variable to a value
                    assignmentStmt(bExec);
                } else {
                    error("");
                }
            } else if (scan.currentToken.primClassif == Classif.FUNCTION) {
                // The only function that should work is print
                handleFunction(bExec);
            } else if (scan.currentToken.primClassif == Classif.DEBUG) {
                handleDebug();
            } else {
                //error("There's an issue with the currentToken, Clarence you b****." + scan.currentToken.tokenStr);
            }

            // shift
            scan.getNext();
        }

        // lets see what the end flow statement is lmaooo
        scan.currentToken.printToken();
        res.terminatingStr = scan.currentToken.tokenStr;
        res.type = SubClassif.END;
        res.structure = "";
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
            res = storageMgr.getUnaryVariableValue(scan.nextToken.tokenStr);
            scan.getNext();
        }
        // Single Variable
        else if (scan.currentToken.primClassif == Classif.OPERAND && endingDelimiter.contains(scan.nextToken.tokenStr)){
            //debug();
            //print("UHM");
            if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                res = storageMgr.getVariableValue(scan.currentToken.tokenStr);
            } /*else if (scan.currentToken.subClassif == SubClassif.STRING){
                res = new ResultValue(scan.currentToken.tokenStr, "String", scan.currentToken.subClassif);
            }*/
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
                //print(scan.nextToken.tokenStr);
                scan.nextToken.printToken();
                error("Expected second argument to be of type operand");
            }
            //print(firstToken);
            //print(scan.currentToken.tokenStr);
            //print(scan.nextToken.tokenStr);

            //ResultValue res1 = storageMgr.getVariableValue(firstToken);
            if(scan.nextToken.subClassif != SubClassif.IDENTIFIER)
                res2 = new ResultValue(scan.nextToken.tokenStr, "primitive", scan.nextToken.subClassif);
            else
                res2 = storageMgr.getVariableValue(scan.nextToken.tokenStr);
            //print("Res1: " + res1.value + ", Res2: " + res2.value);
            Numeric num1 = new Numeric(this, res1, scan.currentToken.tokenStr, "1st operand");
            Numeric num2 = new Numeric(this, res2,scan.currentToken.tokenStr, "2nd operand");
            res = util.doMath(this, num1, num2, scan.currentToken.tokenStr);
            scan.getNext();
            showExpr("...",res);
        }
        // Who knows
        else {
            debug();
            error("Cannot recognize expression statement. Current token: " + scan.currentToken.tokenStr + " on line " + scan.currentToken.iSourceLineNr);
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
            skipTo(";");
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
        return res;
    }


    /**
     * This function is called when a outer most if statement is scanned. I don't think this is used in nexted if statements tho,
     * only the executeStatements method, so if you're calling from Parser's constructor then bExec should be set to true.
     * @param bExec the trigger whether to run the code inside of the if (based on the condition) or not.
     */
    private void ifStmt(boolean bExec) throws Exception {
        print("Inside If on line " + scan.currentToken.iSourceLineNr);

        //TODO: Delete this later
        scan.currentToken.printToken();

        if(bExec) {
            // test the condition in the if statement and execute if the condition is correct
            boolean testIfCond = evalCond();
            print("EvalCond: " + testIfCond);
            print("After evalCond: " + scan.currentToken.tokenStr);
            scan.getNext();     // Move to the ':'
            print("After next before if work " + scan.currentToken.tokenStr);
            if (testIfCond) {
                // Cond returned true, execute the statements below it
                ResultValue res = executeStatements(true);

                // Once the 'if' returns, we should either be on an else or an endif;
                if (res.terminatingStr.equals("else")) {
                    scan.getNext();     // Move to the ':'
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' token after 'else");

                    // Finish the else block but dont execute them
                    print("On 'else' on line " + scan.currentToken.iSourceLineNr + ". Not executing");
                    res = executeStatements(false);
                }

                if (!res.terminatingStr.equals("endif")) {
                    error("Expected 'endif' for an 'if'");
                }
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected ';' after 'endif'");
            } else {
                // Condition returned false, execute else or find endif
                ResultValue res = executeStatements(false);
                if (res.terminatingStr.equals("else")) {
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' after 'else'");
                    res = executeStatements(true);
                }
                if (!res.terminatingStr.equals("endif"))
                    error("Expected 'endif' after 'if'");
            }
        } else{     // Do not execute this ifStmt, but traverse through the statements
            skipTo(":");
            ResultValue res = executeStatements(false);
            if(res.terminatingStr.equals("else")){
                scan.getNext(); // go to ':'
                if(!scan.currentToken.tokenStr.equals(":"))
                    error("Expected ':' after 'else");
                res = executeStatements(false);
            }
            if(!res.terminatingStr.equals("endif"))
                error("Expected 'endif' after 'if'");
        }
        print("End if on line " + scan.currentToken.iSourceLineNr);
        return;
    }

    /**
     * This method is a little more tricky in that it should use Scanner's setPosition to figure out where to loop back to.
     * @param bExec
     */
    private void whileStmt(Boolean bExec) throws Exception{
        int colPos, lineNum;
        colPos = scan.currentToken.iColPos;
        lineNum = scan.currentToken.iSourceLineNr;

        System.out.println("Inside While on line " + scan.currentToken.iSourceLineNr);
        ResultValue rv;
        if(bExec) {
            while (evalCond()) {
                scan.getNext();         // Moves us to the ':'
                rv = executeStatements(bExec);
                if (!rv.terminatingStr.equals("endwhile"))
                    error("Expected endwhile after while");
                scan.setPosition(lineNum, colPos);
            }
            print("Exiting while loop on line " + scan.currentToken.iSourceLineNr + ". Current is on " + scan.currentToken.tokenStr);
            scan.getNext();         // Move to the ':' after the while condition
            rv = executeStatements(false);
        } else {
            skipTo(":");
            rv = executeStatements(false);
        }
        if(!rv.terminatingStr.equals("endwhile"))
            error("Expected 'endwhile' after while loop");
        scan.getNext();
        if(!scan.currentToken.tokenStr.equals(";"))
            error("Expected ';' after 'endwhile'");
    }

    /**
     * Once you return a result value, you should assign it to the left most variable (variableString) and return what you
     * assigned to it. This method should use StorageManager to check for data type.
     * @param variableString The variable string to be looked up in StorageManager to confirm data type and declaration.
     * @param result The ResultValue you're assigning to the variableString
     * @return The ResultValue that was assigned
     */
    private ResultValue assign(String variableString, ResultValue result) throws Exception{
        ResultValue target = storageMgr.getVariableValue(variableString);

        if (target.type == result.type){
            target.value = result.value;
        } else {
            if(target.type == SubClassif.INTEGER){
                result.value = Integer.toString(Integer.parseInt(result.value));
                target.value = result.value;
            } else if (target.type == SubClassif.FLOAT){
                result.value = Float.toString(Float.parseFloat(result.value));
                target.value = result.value;
            } else{
                error("Not sure why we're here");
            }
        }
        storageMgr.updateVariable(variableString, target);
        showAssign(variableString, target);

        return target;
    }

    /**
     * This method is called when you find a control-flow variable (if, while) in order to determine whether the conditional
     * is true or false. This method should use getNext to be able execute the tokens and determine the boolean they result in.
     * @return The boolean value of whether this condition is true or false.
     */
    private boolean evalCond() throws Exception{
        // Move off the if or while
        scan.getNext();
        print("Entering evalCond, current token: " + scan.currentToken.tokenStr + " on line " + scan.currentToken.iSourceLineNr);
        ResultValue result = expr(":");

        print("evalCond result: " + result.value);
        if(result.value.equals("T"))
            return true;
        else
            return false;
    }

    /**
     * When you're skipping over something, it's nice to be able to use getNext() to traverse to the ending token,
     * which is ';' for assignmentStatement and ':' for control statements.
     * @param endingDelimiter
     */
    private void skipTo(String endingDelimiter) throws Exception{
        while(!scan.currentToken.tokenStr.equals(endingDelimiter))
            scan.getNext();
    }

    /**
     * When Scanner returns a built in function or user-defined function, it should be handled here.
     */
    private void handleFunction(boolean bExec) throws Exception{
        if(bExec) {
            if (scan.currentToken.tokenStr.equals("print")) {
                printFunction();
            }
        } else
            skipTo(";");
    }

    private void printFunction() throws Exception{
        scan.getNext();
        //Next token should be an open parenthesis
        if(!scan.currentToken.tokenStr.equals("(")){
            error("Open parenthesis expected after print function");
            return;
        }
        scan.getNext();
        while(true) {
            if (scan.currentToken.subClassif == SubClassif.STRING) {
                //This is a string literal, we should print it
                System.out.print(scan.currentToken.tokenStr);
                scan.getNext();
            } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                ResultValue variableEval = expr(",)"); //only evaluate this expression
                System.out.print(variableEval.value);
                //debug();
                //print(scan.nextToken.tokenStr);
                //scan.getNext();
                if(scan.currentToken.primClassif != Classif.SEPARATOR){
                    scan.getNext();
                }
            } else {
                ResultValue variable = expr(",)");
                System.out.print(variable.value);
                //debug();
                //print(scan.nextToken.tokenStr);
                scan.getNext();
            }
            if(scan.currentToken.tokenStr.equals(",")){
                System.out.print(" ");
                scan.getNext();
                continue;
            }
            if(scan.currentToken.tokenStr.equals(")")){
                System.out.println();
                scan.getNext();
                break;
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
        } else if (scan.currentToken.tokenStr.equals("Assign")) {
            if (command.equals("on")) {
                bShowAssign = true;
            } else if (command.equals("off")) {
                bShowAssign = false;
            } else {
                error("Unknown trigger for Assign, should be 'on' or 'off'");
            }
            scan.getNext(); //sits on trigger
        }else if (scan.currentToken.tokenStr.equals("Stmt")){
            if (command.equals("on")){
                scan.bPrintLines = true;
            } else if (command.equals("off")){
                scan.bPrintLines = false;
            } else {
                error("Unknown trigger for Stmt, should be 'on' or 'off'");
            }
            scan.getNext(); //sits on trigger
        } else {
            error("Unknown Debug Command");
        }
        if(!scan.nextToken.tokenStr.equals(";")){
            error("Expected semicolon after debugging assignment");
        }
        scan.getNext(); //sits on ';'
    }

    public void showExpr(String expr, ResultValue result){
        if (bShowExpr){ System.out.print(expr+ "is" + result.value);}
    }

    public void showAssign(String variable, ResultValue result){
        if (bShowAssign){ System.out.println("... Assign result into '" +variable + "' is '" + result.value + "'");}
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
