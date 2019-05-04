package meatbol;

import java.util.ArrayList;

public class Parser {
    private Scanner scan;
    private StorageManager storageMgr;
    private SymbolTable st;
    private Expression expr;
    public Utility util;

    private boolean bShowExpr = false;
    private boolean bShowAssign = false;

    /**
     * <p>Parser's constructor is responsible for traversing through the file and applying the appropriate operations</p>
     * @param filename  The filename to be passed to Scanner to be used for reading
     * @param st        The global symbol table used for the Parser
     */
    public Parser(String filename, SymbolTable st) throws Exception{
        storageMgr = new StorageManager(this);
        util = new Utility();
        this.st = st;
        scan = new Scanner(this, filename, st);
        scan.sManager = storageMgr;
        ResultValue rv;
        this.expr = new Expression(this, scan, storageMgr, st);
        try {
            while (scan.trigger){
                scan.getNext();
                //scan.currentToken.printToken();
                //debug();
                if (scan.currentToken.subClassif == SubClassif.FLOW){
                    //def, if, for, while,
                    switch(scan.currentToken.tokenStr) {
                        case "if":
                            rv = ifStmt(ExecMode.EXECUTE);//IfStmt should get to the endif, but save any found breaks or continues in the rv ExecMode
                            if (rv.iExecMode == ExecMode.BREAK_EXEC || rv.iExecMode == ExecMode.CONTINUE_EXEC) {
                                //Yell at them for using break or continue at the wrong place
                                error("Incorrect usage of " + rv.iExecMode);
                            }
                            break;
                        case "def":
                            System.err.println("User defined functions are not being used in this programming assignment.");
                            break;
                        case "for":
                            //TODO: Handle for loops bruh
                            scan.getNext();
                            forStmt(ExecMode.EXECUTE);
                            //error("Trying to run a for loop and we havent written code for this");
                            break;
                        case "while":
                            whileStmt(ExecMode.EXECUTE);
                            break;
                        default:
                            error("Found something we don't know what to do with: %s\n",scan.currentToken.tokenStr);

                    }
                    //scan.currentToken.printToken(); //for debugging
                } else if (scan.currentToken.subClassif == SubClassif.DECLARE){
                    //System.out.println("Declare is.." + scan.currentToken.tokenStr);
                    scan.getNext();
                    if(scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                        error("Expected identifier after declare: " + scan.currentToken.tokenStr);
                    else{
                        if(scan.nextToken.primClassif == Classif.SEPARATOR){
                            if(scan.nextToken.tokenStr.equals("[")){
                                //first find what's inside the brackets
                                Token array = scan.currentToken;
                                scan.getNext();
                                if(scan.nextToken.tokenStr.equals("]")) {
                                    //Do a thing to take in comma separated values into array. Brackets can only be empty if assigning the array in declaration The brackets cannot be empty if not assigning.
                                    scan.getNext();
                                    if (!scan.nextToken.tokenStr.equals("=")) {
                                        error("'=' missing. Arrays without initial bounds must have initial values " + array.tokenStr);
                                    }
                                    scan.getNext(); // on equal sign
                                    scan.getNext(); // on values
                                    assignArrayNoSize(array, ";", ExecMode.EXECUTE);
                                    //Call expression to get a list of resultValues to initialize array.
                                } else{
                                    //Do an expression to get the size of the array. If array already instantiated, then do expression to get location for assignment
                                    scan.getNext(); // on expr
                                    ResultValue size = expr.evaluateExpression("]");
                                    setSize(array, size);
                                    scan.getNext();
                                    //System.out.println("Just finished setting array to size " + size.value + " current token: " + scan.currentToken.tokenStr);
                                    if(scan.currentToken.tokenStr.equals("=")){
                                        scan.getNext();
                                        assignArray(";", array, ExecMode.EXECUTE);
                                    } else if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected ';' after array declaration");
                                }
                            } else if(!scan.nextToken.tokenStr.equals(";")) {
                                System.out.println("Mmm..." + scan.nextToken.tokenStr);
                                error("Must end with ';' to finish variable declaration");
                            }
                        } else if (scan.nextToken.primClassif == Classif.OPERATOR){
                            assignmentStmt(ExecMode.EXECUTE);
                        } else {
                            error("Expected either semi-colon or operator following variable declaration. Saw: " + scan.nextToken.tokenStr);
                        }
                    }
                } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER){
                    // TODO: This is a variable that should already exist, we are assigning something new to it
                    if(scan.nextToken.tokenStr.equals("[")){        // Updating array value
                        Token target = scan.currentToken;
                        scan.getNext(); // [
                        scan.getNext(); // expr
                        ResultValue index = expr.evaluateExpression("]");
                        scan.getNext(); // on '=' i think
                        if(!scan.currentToken.tokenStr.equals("="))
                            error("Expected '=' with this array assignment");
                        scan.getNext();
                        ResultValue value = expr.evaluateExpression(";");
                        updateArrayValue(target, index, value, ExecMode.EXECUTE);
                    } else {
                        assignmentStmt(ExecMode.EXECUTE);
                    }

                } else if(scan.currentToken.primClassif == Classif.FUNCTION){
                        handleFunction(ExecMode.EXECUTE);
                        //System.out.println("Coming back from function..." + scan.currentToken.tokenStr);
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
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private ResultValue executeStatements(ExecMode bExec) throws Exception{
        ResultValue res = new ResultValue();
        if (!scan.currentToken.tokenStr.equals(":"))
            error("Expected a ':' currentToken value is: '" + scan.currentToken.tokenStr + "'");

        // shift so the currentToken is the first token after the :
        scan.getNext();
            while (scan.currentToken.subClassif != SubClassif.END) {
                // Nested if/while
                if (scan.currentToken.subClassif == SubClassif.FLOW) {
                    switch(scan.currentToken.tokenStr){
                        case "if":
                            res = ifStmt(bExec);//TODO figure out what to do if break or continue is found here
                            //ifStmt should return from a break after finding the endif then execute statements will return the break
                            if(res.iExecMode == ExecMode.CONTINUE_EXEC || res.iExecMode == ExecMode.BREAK_EXEC){
                                //We should handle if and stop executing, but still look for an END token. ifStmt should still be at the right place for us to find it
                                bExec = res.iExecMode;
                            }
                            break;
                        case "while":
                            whileStmt(bExec);
                            break;
                        case "for":
                            scan.getNext();
                            forStmt(bExec);
                            break;
                        default:
                            error("Cannot recognize flow token: %s", scan.currentToken.tokenStr );
                            break;
                    }
                } else if (scan.currentToken.subClassif == SubClassif.DECLARE) {
                    //System.out.println("Declare is.." + scan.currentToken.tokenStr);
                    scan.getNext();
                    if (scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                        error("Expected identifier after declare " + scan.currentToken.tokenStr);
                    else {
                        if (scan.nextToken.primClassif == Classif.SEPARATOR) {
                            if (scan.nextToken.tokenStr.equals("[")) {
                                //first find what's inside the brackets
                                Token array = scan.currentToken;
                                scan.getNext();
                                if (scan.nextToken.tokenStr.equals("]")) {
                                    //Do a thing to take in comma separated values into array. Brackets can only be empty if assigning the array in declaration The brackets cannot be empty if not assigning.
                                    scan.getNext();
                                    if (!scan.nextToken.tokenStr.equals("=")) {
                                        error("'=' missing. Arrays without initial bounds must have initial values " + scan.nextToken);
                                    }
                                    scan.getNext(); // on equal sign
                                    scan.getNext(); // on values
                                    assignArrayNoSize(array, ";", bExec);
                                    //Call expression to get a list of resultValues to initialize array.
                                } else {
                                    //Do an expression to get the size of the array. If array already instantiated, then do expression to get location for assignment
                                    scan.getNext(); // on expr
                                    ResultValue size = expr.evaluateExpression("]");
                                    setSize(array, size);
                                    scan.getNext();
                                    //System.out.println("Just finished setting array to size " + size.value + " current token: " + scan.currentToken.tokenStr);
                                    if (scan.currentToken.tokenStr.equals("=")) {
                                        scan.getNext();
                                        assignArray(";", array, bExec);
                                    } else if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected ';' after array declaration");
                                }
                            } else if (!scan.nextToken.tokenStr.equals(";")) {
                                System.out.println("Mmm..." + scan.nextToken.tokenStr);
                                error("Must end with ';' to finish variable declaration");
                            }
                        } else if (scan.nextToken.primClassif == Classif.OPERATOR) {
                            assignmentStmt(bExec);
                        }
                    }
                } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                    // This is just declaring a variable
                    if (scan.nextToken.tokenStr.equals("[")) {        // Updating array value
                        Token target = scan.currentToken;
                        scan.getNext(); // [
                        scan.getNext(); // expr
                        ResultValue index = expr.evaluateExpression("]");
                        scan.getNext(); // on '=' i think
                        if (!scan.currentToken.tokenStr.equals("="))
                            error("Expected '=' with this array assignment");
                        scan.getNext();
                        ResultValue value = new ResultValue();
                        value = expr.evaluateExpression(";");
                        updateArrayValue(target, index, value, bExec);
                    } else {
                        assignmentStmt(bExec);
                    }
                } else if (scan.currentToken.primClassif == Classif.FUNCTION) {
                    // The only function that should work is print
                    handleFunction(bExec);
                } else if (scan.currentToken.primClassif == Classif.DEBUG) {
                    handleDebug();
                }
                // shift
                scan.getNext();
                if(scan.currentToken.subClassif == SubClassif.END){
                    //If we find a break or continue here, it will affect bExec on the next round until we get to a different end token
                    bExec = handleFlow(bExec);
                    //scan.getNext();//We should not return because of a break or continue here
                }
            }
            //If we're not executing the code already, then we shouldn't be doing anything for a break or continue
                res.iExecMode = bExec;
                if(scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue"))
                    res.iExecMode = handleFlow(bExec);
                //If we are executing the code, then whoever called execute statements should know how to handle the break and continue
            // lets see what the end flow statement is lmaooo
            res.terminatingStr = scan.currentToken.tokenStr;
            res.type = SubClassif.END;
            res.structure = "";
            return res;
    }

    /**
     * <p>This method is used when an assignment to a variable must be done.</p>
     * @param bExec The boolean that decied whether we apply this assignment or not
     * @return  The ResultValue that was assigned to the variable
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private ResultValue assignmentStmt(ExecMode bExec) throws Exception{
        //System.out.println("....Enter assignment...");
        ResultValue res = new ResultValue();
        if(!(bExec == ExecMode.EXECUTE)){
            skipTo(";");
            return res;
        }
        if(scan.currentToken.subClassif != SubClassif.IDENTIFIER){
            error("Expected a variable for assignment");
        }

        if(expr.isArray(scan.currentToken)){
            //ResultValue arrayAssignment = arrayToArrayAssignment(scan.currentToken);
            //return arrayAssignment;
            return arrayToArrayAssignment(scan.currentToken);
            //error("We are trying to copy an existing array into another, we dont have anything to handle it: " + scan.currentToken.tokenStr);
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
                res1 = storageMgr.getVariableValue(targetVariable);
                scan.getNext();

                if (res1.type == SubClassif.DATE) {
                    res2 = expr.evaluateDate();
                } else {
                    res2 = expr.evaluateExpression(";");
                }
                //System.out.println("From assignment>>>" + res2);
                res = assign(targetVariable, res2);
                break;
            case "-=": // x -= 5+1;
                scan.getNext();
                res2 = expr.evaluateExpression(";");
                num2 = new Numeric(this, res2, "-=", "2nd operator");
                res1 = storageMgr.getVariableValue(targetVariable);
                num1 = new Numeric(this, res1, "-=", "1st operator");
                res = assign(targetVariable, util.subtract(this, num1, num2));
                break;
            case "+=":
                scan.getNext();
                res2 = expr.evaluateExpression(";");
                num2 = new Numeric(this, res2, "+=", "2nd operator");
                res1 = storageMgr.getVariableValue(targetVariable);
                num1 = new Numeric(this, res1, "+=", "1st operator");
                res = assign(targetVariable, util.add(this, num1, num2));
                break;
            default:

                error("Expected an assignment operator token after variable, saw " + scan.currentToken.tokenStr);
        }
        return res;
    }

    /**
     * <p> This method is used to handle breaks and continues according to the state of the execution</p>
     * @param bExec bExec is the given scenario that handleFlow uses to determine proper flow
     * @return This methos returns the proper ExecMode for the scenario
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private ExecMode handleFlow(ExecMode bExec) throws Exception{
        if(bExec == ExecMode.EXECUTE){
            if(scan.currentToken.tokenStr.equals("break")) {
                bExec = ExecMode.BREAK_EXEC;
                scan.getNext();
            }else if(scan.currentToken.tokenStr.equals("continue")){
                bExec = ExecMode.CONTINUE_EXEC;
                scan.getNext();
            }
        } else {
            if (scan.currentToken.tokenStr.equals("break") || scan.currentToken.tokenStr.equals("continue")) {
                scan.getNext();
            }
        }
        //scan.getNext();//Move on from the break or continue because we should not be doing anything else with it
        return bExec;
    }

    /**
     * <p>This method is used to parse through an if statement code block</p>
     * @param bExec  The ExecMode that decides whether the statements in the code block should be executed, broken, or skipped
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private ResultValue ifStmt(ExecMode bExec) throws Exception {


        if(bExec == ExecMode.EXECUTE) {
            // test the condition in the if statement and execute if the condition is correct
            boolean testIfCond = evalCond();
            if (testIfCond) {
                // Cond returned true, execute the statements below it
                ResultValue res = executeStatements(ExecMode.EXECUTE);
                if(bExec != ExecMode.IGNORE_EXEC)
                    bExec = res.iExecMode;
                // Once the 'if' returns, we should either be on an else or an endif;
                if (res.terminatingStr.equals("else")) {
                    scan.getNext();     // Move to the ':'
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' token after 'else");

                    // Finish the else block but dont execute them
                    res = executeStatements(ExecMode.IGNORE_EXEC);
                } // Handle if we receive a break or continue inside the if. Just pushing it up
                //bExec = res.iExecMode;


                if (!res.terminatingStr.equals("endif")) {
                    error("Expected 'endif' for an 'if'");
                }
                scan.getNext();
                if (!scan.currentToken.tokenStr.equals(";"))
                    error("Expected ';' after 'endif'");
            } else {
                // Condition returned false, execute else or find endif
                ResultValue res = executeStatements(ExecMode.IGNORE_EXEC);
                if (res.terminatingStr.equals("else")) {
                    scan.getNext();
                    if (!scan.currentToken.tokenStr.equals(":"))
                        error("Expected ':' after 'else'");
                    res = executeStatements(ExecMode.EXECUTE);//If a break or continue is found, execute statements will save it in the ResultValue.iExecMode
                    bExec = res.iExecMode;
                }
                if (!res.terminatingStr.equals("endif"))
                    error("Expected 'endif' after 'if'");
            }
        } else{     // Do not execute this ifStmt, but traverse through the statements
            skipTo(":");
            ResultValue res = executeStatements(ExecMode.IGNORE_EXEC); //Should not receive break or continue
            if(res.terminatingStr.equals("else")){
                scan.getNext(); // go to ':'
                if(!scan.currentToken.tokenStr.equals(":"))
                    error("Expected ':' after 'else");
                res = executeStatements(ExecMode.IGNORE_EXEC);
            }
            if(!res.terminatingStr.equals("endif"))
                error("Expected 'endif' after 'if'");
        }
        return new ResultValue(bExec);
    }

    /**
     * <p>This method is executed when a while statement code block is found</p>
     * @param bExec The boolean that decides whether the statements in the code block should be executed
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private void whileStmt(ExecMode bExec) throws Exception{
        int colPos, lineNum;
        colPos = scan.currentToken.iColPos;
        lineNum = scan.currentToken.iSourceLineNr;

        ResultValue rv;
        if(bExec == ExecMode.EXECUTE) {
            while (evalCond()) {
                rv = executeStatements(ExecMode.EXECUTE);
                if(rv.iExecMode == ExecMode.BREAK_EXEC){
                    scan.setPosition(lineNum, colPos);
                    skipTo(":");
                    break;
                } else if(rv.iExecMode == ExecMode.CONTINUE_EXEC){
                    scan.setPosition(lineNum, colPos);
                    continue;
                } else if (!rv.terminatingStr.equals("endwhile"))
                    error("Expected endwhile after while");
                scan.setPosition(lineNum, colPos);
            }
            rv = executeStatements(ExecMode.IGNORE_EXEC);
        } else {
            skipTo(":");
            rv = executeStatements(ExecMode.IGNORE_EXEC);
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
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private ResultValue assign(String variableString, ResultValue result) throws Exception{
        ResultValue target = storageMgr.getVariableValue(variableString);

        if (target.type == result.type){
            target.value = result.value;
        } else {
            if(target.type == SubClassif.INTEGER){
                if(result.type == SubClassif.FLOAT){
                    float tmp = Float.parseFloat(result.value);
                    int tmp2 = (int) tmp;
                    result.value = Integer.toString(tmp2);
                } else {
                    try {
                        result.value = Integer.toString(Integer.parseInt(result.value));
                    } catch(Exception e){
                        error("Invalid type for " + target.type + " type variable. Saw: " + result.type);
                    }
                }
                target.value = result.value;
            } else if (target.type == SubClassif.FLOAT){
                result.value = Float.toString(Float.parseFloat(result.value));
                target.value = result.value;
            } else{
                System.out.println("Target: " + target.type + " Result: " + result.type);
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
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private boolean evalCond() throws Exception{
        // Move off the if or while
        scan.getNext();
        ResultValue result = expr.evaluateExpression(":");

        return result.value.equals("T");
    }
    

    /**
     * <p>Moves current token to the specified string</p>
     * @param endingDelimiter   The String to stop the current token at
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private void skipTo(String endingDelimiter) throws Exception{
        while(!scan.currentToken.tokenStr.equals(endingDelimiter))
            scan.getNext();
    }

    /**
     * <p>This method is responsible for handling functions the parser comes across</p>
     * @param bExec Decides whether the function should be executed or not
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private void handleFunction(ExecMode bExec) throws Exception{
        if(bExec == ExecMode.EXECUTE) {
            if (scan.currentToken.tokenStr.equals("print")) {
                printFunction();
            }
        } else
            skipTo(";");
    }

    /**
     * <p>Prints the parameters in the function</p>
     * @throws Exception Rethrows whatever exception is handed to it
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
            //scan.currentToken.printToken();
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
            } */
            else if (scan.currentToken.primClassif == Classif.OPERAND){
                ResultValue variable;
                variable = expr.evaluateExpression(",)");
                System.out.print(variable.value);
                if(scan.currentToken.primClassif != Classif.SEPARATOR)
                    scan.getNext();
            }else if (scan.currentToken.tokenStr.equals("#")){
                // We're going to concatenate it anyway, just skip this token and go to what is being added
                scan.getNext();
                continue;
            } else if (scan.currentToken.primClassif == Classif.FUNCTION) {
                ResultValue variable = expr.evaluateExpression(",)");
                System.out.print(variable.value);
                if(scan.currentToken.tokenStr.equals(")")){
                    scan.getNext();
                    System.out.println();
                    break;
                }

                scan.getNext();
            }
            else {
                //System.out.print("From print function: " + scan.currentToken.tokenStr);
                ResultValue variable = expr.evaluateExpression(",)");
                System.out.print(variable.value);
                if(scan.currentToken.tokenStr.equals(")")){
                    scan.getNext();
                    System.out.println();
                    break;
                }

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
                if(!scan.currentToken.tokenStr.equals(";"))
                    error("Expected semicolon following print statement.");
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
     * @throws Exception Throws a new ParserException based on the argument string it is given and where we are in the given program
     */
    public void error(String fmt, Object...varArgs) throws Exception{
        String diagnosticTxt = String.format(fmt, varArgs);
        throw new ParserException(scan.currentToken.iSourceLineNr, diagnosticTxt, scan.sourceFileNm);
    }

    /**
     * <p>This method is responsible for triggering the debug statements requested by the programmer</p>
     * @throws Exception Rethrows whatever exception is handed to it
     */
    public void handleDebug() throws Exception{
        //current token is on debug
        scan.getNext();
        String command = scan.nextToken.tokenStr;
        switch(scan.currentToken.tokenStr){
            case "Expr":
                if(command.equals("on")){
                    bShowExpr = true;
                } else if(command.equals("off")){
                    bShowExpr = false;
                } else {
                    error("Unknown trigger for Expr, should be 'on' or 'off'");
                }
                scan.getNext(); //sits on trigger
                    break;
            case "Token":
                if (command.equals("on")){
                    scan.bShowToken = true;
                } else if (command.equals("off")){
                    scan.bShowToken = false;
                } else {
                    error("Unknown trigger for Token, should be 'on' or 'off'");
                }
                scan.getNext(); //sits on trigger
                break;
            case "Assign":
                if (command.equals("on")) {
                    bShowAssign = true;
                } else if (command.equals("off")) {
                    bShowAssign = false;
                } else {
                    error("Unknown trigger for Assign, should be 'on' or 'off'");
                }
                scan.getNext(); //sits on trigger
                break;
            case "Stmt":
                if (command.equals("on")){
                    scan.bPrintLines = true;
                } else if (command.equals("off")){
                    scan.bPrintLines = false;
                } else {
                    error("Unknown trigger for Stmt, should be 'on' or 'off'");
                }
                scan.getNext(); //sits on trigger
                break;
            default:
                error("Unknown Debug Command");
                break;
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
    private void showAssign(String variable, ResultValue result){
        if (bShowAssign){ System.out.println("... Assign result into '" +variable + "' is '" + result.value + "'");}
    }

    private void updateArrayValue(Token target, ResultValue index, ResultValue value, ExecMode bExec) throws Exception{
        if(bExec == ExecMode.EXECUTE) {
            ResultValue targetRV = storageMgr.getVariableValue(target);
            targetRV.arr.updateElement(index, value);
            storageMgr.updateVariable(target.tokenStr, targetRV);
        } else {
            skipTo(";");
        }

    }

    /**
     * Assigned an array object to an existing array object
     * @param target The array that is receiving the assignment
     * @return What was assigned to the array only because this is getting called from assignmentStmt
     * @throws Exception Rethrows whatever exception is handed to it
     */
    private ResultValue arrayToArrayAssignment(Token target) throws Exception{
        /*
        When assigning one array to another, we have two cases:
        1) String arrays
             a) target has the shorter length
             b) expression has the shorter length
        2) Arrays
            a) target has the shorter length
            b) expression has the shorter length
         */
        if(scan.nextToken.primClassif != Classif.OPERATOR)
            error("Expected assignment operator");
        scan.getNext(); // puts us on the assignment operator
        // TODO: For flexible requirements, add other cases for +=, -=, etc
        ResultValue targetRV = storageMgr.getVariableValue(target);

        scan.getNext(); // Puts us on the expr

        ResultValue source = expr.evaluateExpression(";");
        int targetBounds = targetRV.arr.getBounds();
        int exprBounds = source.arr.getBounds();

        try {
            int end = Math.min(targetBounds, exprBounds);
            targetRV.arr.copyArray(source, end);
            storageMgr.updateVariable(target.tokenStr, targetRV);
        } catch(Exception e){
            error(target.tokenStr + " of type " + targetRV.type + " expected same type. Found:" + source.type);
        }
        return targetRV;

    }

    /**
     * When an array is declared with a size, this method is responsible for saving it.
     * @param target The array whose size we are declaring
     * @param bounds The size to declare it to
     * @throws Exception Rethrows whatever exception is handed to it
     */
    public void setSize(Token target, ResultValue bounds) throws Exception {
        ResultValue targetRV = storageMgr.getVariableValue(target);
        targetRV.arr.setBounds(bounds);
        targetRV.structure = "fixed-array";
        storageMgr.updateVariable(target.tokenStr, targetRV);
        //targetRV = storageMgr.getVariableValue(target);
        //System.out.println("Setting size for " + target.tokenStr + " of type " + targetRV.structure + " to " + bounds.value);

    }

    /**
     * This method is responsible for assigning values to an array who's size was not specified. It also sets the size
     * once it counts all of the variables in the assignment.
     * @param target The array receiving the assignment.
     * @param endTerm Uh...the terminating token of the assignment which should a semicolor, idk why this is here
     * @param bExec Whether to execute this assignment or not
     * @throws Exception Rethrows whatever exception is handed to it
     */
    public void assignArrayNoSize(Token target, String endTerm, ExecMode bExec) throws Exception{
        ResultValue targetRV = storageMgr.getVariableValue(target.tokenStr);
        SubClassif type = targetRV.type;
        StringBuilder valueString = new StringBuilder();
        int index = 0;
        ArrayList<ResultValue> buffer = new ArrayList<>();

        if (bExec == ExecMode.EXECUTE) {
            while (!scan.currentToken.tokenStr.equals(endTerm)) {

                if (scan.currentToken.primClassif == Classif.SEPARATOR) {
                    valueString.append(scan.currentToken.tokenStr);
                    scan.getNext();
                    continue;
                }
                String element;
                if (type == SubClassif.INTEGER) {
                    element = Integer.toString((int) (Float.parseFloat(scan.currentToken.tokenStr)));
                } else if (type == SubClassif.FLOAT) {
                    element = Float.toString(Float.parseFloat(scan.currentToken.tokenStr));
                } else { // String variables
                    element = scan.currentToken.tokenStr;
                }
                valueString.append(element);
                buffer.add(new ResultValue(element, "primitive", type));
                scan.getNext();
                index++;
            }
            ResultValue[] array = new ResultValue[index];
            for (int i = 0; i < buffer.size(); i++) {
                array[i] = buffer.get(i);
            }
            targetRV.arr.setBounds(index);
            targetRV.structure = "fixed-array";
            targetRV.arr.arr = array;
            targetRV.value = valueString.toString();
            //System.out.print("Array assignment: " + targetRV);
            storageMgr.updateVariable(target.tokenStr, targetRV);
        } else {
            skipTo(";");
        }

    }

    /**
     * A method responsible for assigning variables to an array whose size was previously specified.
     * @param endTerm The terminating token that shouldn't even be needed
     * @param tokAssign The token receiving the assignment
     * @param bExec Whether to run this assignment or not
     * @throws Exception Rethrows whatever exception is handed to it
     */
    public void assignArray(String endTerm, Token tokAssign, ExecMode bExec) throws Exception {
        ResultValue targetRV = storageMgr.getVariableValue(tokAssign.tokenStr);
        SubClassif type = targetRV.type;
        StringBuilder valueString = new StringBuilder();
        int bounds = targetRV.arr.getBounds();
        int index = 0;
        ResultValue [] array = new ResultValue[bounds];

        if(bExec==ExecMode.EXECUTE) {
            while (!scan.currentToken.tokenStr.equals(endTerm)) {
                if (index >= bounds) {
                    error("Number of variables assigned to " + tokAssign.tokenStr +
                            " exceeds " + bounds);
                }
                if (scan.currentToken.primClassif == Classif.SEPARATOR) {
                    valueString.append(scan.currentToken.tokenStr);
                    scan.getNext();
                    continue;
                }
                String element;
                if (type == SubClassif.INTEGER) {
                    element = Integer.toString((int) (Float.parseFloat(scan.currentToken.tokenStr)));
                } else if (type == SubClassif.FLOAT) {
                    element = Float.toString(Float.parseFloat(scan.currentToken.tokenStr));
                } else { // String variables
                    element = scan.currentToken.tokenStr;
                }
                valueString.append(element);
                array[index] = new ResultValue(element, "primitive", type);
                scan.getNext();
                index++;
            }
            targetRV.arr.arr = array;
            targetRV.value = valueString.toString();
            //System.out.print("Array assignment: " + targetRV);
            storageMgr.updateVariable(tokAssign.tokenStr, targetRV);
        } else {
            skipTo(";");
        }
    }

    /**
     * A method used for handling for loops
     * @param bExec Whether to execute this for loop or not
     * @throws Exception Rethrows whatever exception is handed to it
     */
    public void forStmt(ExecMode bExec) throws Exception {

        ResultValue rv;
        if(bExec == ExecMode.EXECUTE){
            ForLoopControl forControl = new ForLoopControl(this, scan, storageMgr, expr);
            forControl.setUpCondition();
            //scan.currentToken.printToken();
            int iColPos = scan.currentToken.iColPos;
            int iLineNum = scan.currentToken.iSourceLineNr;
            while(forControl.evaluateCondition()){
                rv = executeStatements(ExecMode.EXECUTE);
                scan.setPosition(iLineNum, iColPos);
                if(rv.iExecMode == ExecMode.BREAK_EXEC){
                    break;
                }
            }
            skipTo(":");
            rv = executeStatements(ExecMode.IGNORE_EXEC);

        } else{
            skipTo(":");
            rv = executeStatements(ExecMode.IGNORE_EXEC);
        }
        if(!rv.terminatingStr.equals("endfor"))
            error("Expected 'endfor' to terminate for loop");
        if(!scan.nextToken.tokenStr.equals(";"))
            error("Expected ';' after 'endfor'");

    }
}
