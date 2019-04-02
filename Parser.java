package meatbol;

import sun.jvm.hotspot.StackTrace;

public class Parser {
    private Scanner scan;
    private StorageManager storageMgr;
    private SymbolTable st;
    private Utility util;

    private boolean bShowExpr = false;
    private boolean bShowAssign = false;

    /**
     * <p>Parser's constructor is responsible for traversing through the file and applying the appropriate operations</p>
     * @param filename  The filename to be passed to Scanner to be used for reading
     * @param st        The global symbol table used for the Parser
     */
    public Parser(String filename, SymbolTable st) throws Exception{
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
                        assignmentStmt(true);
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
            e.printStackTrace();
        }
    }

    /**
     * <p>Used inside of 'if/while', the embedded statements to be executed are run here</p>
     * @param bExec     The boolean that decides whether the embedded statements should be executed
     * @return          The endflow statement that terminated the statement execution
     * @throws Exception
     */
    private ResultValue executeStatements(Boolean bExec) throws Exception{
        ResultValue res = new ResultValue();

        if (!scan.currentToken.tokenStr.equals(":"))
            error("Expected a ':' currentToken value is " + scan.currentToken.tokenStr);

        // shift so the currentToken is the first token after the :
        scan.getNext();

        while (scan.currentToken.subClassif != SubClassif.END) {
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
                //error("There's an issue with the currentToken, Clarence why." + scan.currentToken.tokenStr);
            }

            // shift
            scan.getNext();
        }

        // lets see what the end flow statement is lmaooo
        res.terminatingStr = scan.currentToken.tokenStr;
        res.type = SubClassif.END;
        res.structure = "";
        return res;
    }

    /**
     * <p>Used to evaluation an expression and return the result of it</p>
     * @param endingDelimiter   The delimiter to stop at for this specific expression
     * @return  The ResultValue that was production from the expression operation
     * @throws Exception
     */
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

    /**
     * <p>This method is used when an assignment to a variable must be done.</p>
     * @param bExec The boolean that decied whether we apply this assignment or not
     * @return  The ResultValue that was assigned to the variable
     * @throws Exception
     */
    private ResultValue assignmentStmt(Boolean bExec) throws Exception{
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

                error("Expected an assignment operator token after variable");
        }
        return res;
    }


    /**
     * <p>This method is used to parse through an if statement code block</p>
     * @param bExec  The boolean that decides whether the statements in the code block should be executed
     * @throws Exception
     */
    private void ifStmt(boolean bExec) throws Exception {


        if(bExec) {
            // test the condition in the if statement and execute if the condition is correct
            boolean testIfCond = evalCond();
            scan.getNext();     // Move to the ':'
            if (testIfCond) {
                // Cond returned true, execute the statements below it
                ResultValue res = executeStatements(true);

                // Once the 'if' returns, we should either be on an else or an endif;
                if (res.terminatingStr.equals("else")) {
                    scan.getNext();     // Move to the ':'
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' token after 'else");

                    // Finish the else block but dont execute them
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
        return;
    }

    /**
     * <p>This method is executed when a while statement code block is found</p>
     * @param bExec The boolean that decides whether the statements in the code block should be executed
     * @throws Exception
     */
    private void whileStmt(Boolean bExec) throws Exception{
        int colPos, lineNum;
        colPos = scan.currentToken.iColPos;
        lineNum = scan.currentToken.iSourceLineNr;

        ResultValue rv;
        if(bExec) {
            while (evalCond()) {
                scan.getNext();         // Moves us to the ':'
                rv = executeStatements(bExec);
                if (!rv.terminatingStr.equals("endwhile"))
                    error("Expected endwhile after while");
                scan.setPosition(lineNum, colPos);
            }
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
     * <p>When a variable requires a new assignment this method updates its value in the StorageManager</p>
     * @param variableString    The variable that will be assigned a new value
     * @param result    The ResultValue to be assigned to the variable
     * @return The ResultValue that was assigned to the variable
     * @throws Exception
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
     * <p>Evaluated the value of a condition for flow statements and returns it's boolean value</p>
     * @return  The boolean value dependent on the condition
     * @throws Exception
     */
    private boolean evalCond() throws Exception{
        // Move off the if or while
        scan.getNext();
        ResultValue result = expr(":");

        if(result.value.equals("T"))
            return true;
        else
            return false;
    }

    /**
     * <p>Moves current token to the specified string</p>
     * @param endingDelimiter   The String to stop the current token at
     * @throws Exception
     */
    private void skipTo(String endingDelimiter) throws Exception{
        while(!scan.currentToken.tokenStr.equals(endingDelimiter))
            scan.getNext();
    }

    /**
     * <p>This method is responsible for handling functions the parser comes across</p>
     * @param bExec Decides whether the function should be executed or not
     * @throws Exception
     */
    private void handleFunction(boolean bExec) throws Exception{
        if(bExec) {
            if (scan.currentToken.tokenStr.equals("print")) {
                printFunction();
            }
        } else
            skipTo(";");
    }

    /**
     * <p>Prints the parameters in the function</p>
     * @throws Exception
     */
    private void printFunction() throws Exception{
        int counter=0;
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
            }/* else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                ResultValue variableEval = expr(",)"); //only evaluate this expression
                System.out.print(variableEval.value);
                if(scan.currentToken.primClassif != Classif.SEPARATOR){
                    scan.getNext();
                }
            } */else if (scan.currentToken.primClassif == Classif.OPERAND){
                ResultValue variable = expr(",)");
                System.out.print(variable.value);
                if(scan.currentToken.primClassif != Classif.SEPARATOR)
                    scan.getNext();
            }else {
                //System.out.print("From print function: " + scan.currentToken.tokenStr);
                ResultValue variable = expr(",)");
                System.out.print(variable.value);
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
            counter++;
            if(counter > 100)
                error("Sorry, we had an infinite loop and had to exit");
        }
    }

    /**
     * <p>Throws a ParserException when an error in the code is found</p>
     * @param fmt   The string to specify what caused the error
     * @param varArgs   I don't know why you made me include this parameter, Clark
     * @throws Exception
     */
    public void error(String fmt, Object...varArgs) throws Exception{
        String diagnosticTxt = String.format(fmt, varArgs);
        throw new ParserException(scan.currentToken.iSourceLineNr, diagnosticTxt, scan.sourceFileNm);
    }

    /**
     * <p>This method is responsible for triggering the debug statements requested by the programmer</p>
     * @throws Exception
     */
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

    /**
     * Used to print the following expr and it's result. Used for debugging and output printing
     * @param expr  The expression to be printed
     * @param result The ResultValue that was returned by the expression
     */
    public void showExpr(String expr, ResultValue result){
        if (bShowExpr){ System.out.println(expr+ " is " + result.value);}
    }

    /**
     * <p>Used to print variable assignments for debugging</p>
     * @param variable  The variable that is receiving the new assignment
     * @param result    The ResultValue that is being assigned to the variable
     */
    public void showAssign(String variable, ResultValue result){
        if (bShowAssign){ System.out.println("... Assign result into '" +variable + "' is '" + result.value + "'");}
    }
}
