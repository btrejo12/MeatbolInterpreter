package meatbol;

import java.util.ArrayList;
import java.util.Arrays;

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
        storageMgr = new StorageManager();
        util = new Utility();
        this.st = st;
        scan = new Scanner(filename, st);
        scan.sManager = storageMgr;
        this.expr = new Expression(this, scan, storageMgr, st);
        try {
            while (scan.trigger){
                scan.getNext();
                //scan.currentToken.printToken();
                //debug();
                if (scan.currentToken.subClassif == SubClassif.FLOW){
                    //def, if, for, while,
                    if (scan.currentToken.tokenStr.equals("if")){
                        ifStmt(true);
                    } else if (scan.currentToken.tokenStr.equals("def")){
                        System.err.println("User defined functions are not being used in this programming assignment.");
                    } else if (scan.currentToken.tokenStr.equals("for")){
                        //TODO: Handle for loops bruh
                        forStmt(true);
                        //error("Trying to run a for loop and we havent written code for this");
                    } else if (scan.currentToken.tokenStr.equals("while")){
                        whileStmt(true);
                    }
                    //scan.currentToken.printToken(); //for debugging
                } else if (scan.currentToken.subClassif == SubClassif.DECLARE){
                    //System.out.println("Declare is.." + scan.currentToken.tokenStr);
                    scan.getNext();
                    if(scan.currentToken.subClassif != SubClassif.IDENTIFIER)
                        error("Expected identifier after declare " + scan.currentToken.tokenStr);
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
                                        error("'=' missing. Arrays without initial bounds must have initial values " + scan.nextToken);
                                    }
                                    scan.getNext(); // on equal sign
                                    scan.getNext(); // on values
                                    assignArrayNoSize(array, ";");
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
                                        assignArray(";", array);
                                    } else if (!scan.currentToken.tokenStr.equals(";"))
                                        error("Expected ';' after array declaration");
                                }
                            } else if(!scan.nextToken.tokenStr.equals(";")) {
                                System.out.println("Mmm..." + scan.nextToken.tokenStr);
                                error("Must end with ';' to finish variable declaration");
                            }
                        } else if (scan.nextToken.primClassif == Classif.OPERATOR){
                            assignmentStmt(true);
                        } else {
                            //Not sure here
                            scan.nextToken.printToken();
                        }
                    }
                }
                else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER){
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
                        updateArrayValue(target, index, value);
                    } else {
                        assignmentStmt(true);
                    }

                } else if(scan.currentToken.primClassif == Classif.FUNCTION){
                        handleFunction(true);
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
                } else if (scan.currentToken.tokenStr.equals("for")){
                    forStmt(bExec);
                } else {
                    error("Brenda wtf is wrong with you there's only 3 flows:" + scan.currentToken.tokenStr);
                }
            } else if (scan.currentToken.subClassif == SubClassif.IDENTIFIER) {
                // This is just declaring a variable
                if (scan.nextToken.primClassif == Classif.SEPARATOR) {
                    // Check if an array variable
                    if(scan.nextToken.tokenStr.equals("[")){
                        Token array = scan.currentToken;
                        scan.getNext();
                        if(scan.nextToken.tokenStr.equals("]")){
                            // No size, make sure there's an equal sign
                            scan.getNext(); // should be on ']'
                            if(!scan.nextToken.tokenStr.equals("=")){
                                error("Must declare size when instantiating an array object: ", array.tokenStr);
                            } else {
                                // TODO: Call whatever method will get these array variables and save it to Storage manager
                                scan.getNext(); // Puts us on the equal
                            }
                        } else {
                            ResultValue size = expr.evaluateExpression("]");
                            // TODO: Put this array in the Storage Manager with this declared size
                            if(scan.nextToken.primClassif == Classif.OPERATOR){ // Probably an equal sign
                                // TODO: Do whatever we are going to call to show this datatype.
                            }
                        }
                    } else {
                        continue;
                    }
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
     * <p>This method is used when an assignment to a variable must be done.</p>
     * @param bExec The boolean that decied whether we apply this assignment or not
     * @return  The ResultValue that was assigned to the variable
     * @throws Exception
     */
    private ResultValue assignmentStmt(Boolean bExec) throws Exception{
        //System.out.println("....Enter assignment...");
        ResultValue res = new ResultValue();
        if(!bExec){
            skipTo(";");
            return res;
        }
        if(scan.currentToken.subClassif != SubClassif.IDENTIFIER){
            error("Expected a variable for assignment");
        }

        if(expr.isArray(scan.currentToken)){
            error("We are trying to copy an existing array into another, we dont have anything to handle it");
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
                res2 = expr.evaluateExpression(";");
                //System.out.println("From assignment>>>" + res2);
                res = assign(targetVariable, res2);
                break;
            case "-=": // x -= 5+1;
                res2 = expr.evaluateExpression(";");
                num2 = new Numeric(this, res2, "-=", "2nd operator");
                res1 = storageMgr.getVariableValue(targetVariable);
                num1 = new Numeric(this, res1, "-=", "1st operator");
                res = assign(targetVariable, util.subtract(this, num1, num2));
                break;
            case "+=":
                res2 = expr.evaluateExpression(";");
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
                rv = executeStatements(bExec);
                if (!rv.terminatingStr.equals("endwhile"))
                    error("Expected endwhile after while");
                scan.setPosition(lineNum, colPos);
            }
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
        ResultValue result = expr.evaluateExpression(":");

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
                if(expr.isArray(scan.currentToken)){
                    variable = expr.evaluateExpression(",)");
                }else {
                    variable = expr.evaluateExpression(",)");
                }
                System.out.print(variable.value);
                if(scan.currentToken.primClassif != Classif.SEPARATOR)
                    scan.getNext();
            }else if (scan.currentToken.tokenStr.equals("#")){
                // We're going to concatenate it anyway, just skip this token and go to what is being added
                scan.getNext();
                continue;
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
                //TODO find out why the parentheses and comma are lost when unary minus is given
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

    public void updateArrayValue(Token target, ResultValue index, ResultValue value) throws Exception{
        ResultValue targetRV = storageMgr.getVariableValue(target);
        targetRV.arr.updateElement(index, value);

    }

    public void setSize(Token target, ResultValue bounds) throws Exception {
        ResultValue targetRV = storageMgr.getVariableValue(target);
        targetRV.arr.setBounds(bounds);
        targetRV.structure = "fixed-array";
        storageMgr.updateVariable(target.tokenStr, targetRV);
        //targetRV = storageMgr.getVariableValue(target);
        //System.out.println("Setting size for " + target.tokenStr + " of type " + targetRV.structure + " to " + bounds.value);

    }

    public void assignArrayNoSize(Token target, String endTerm) throws Exception{
        ResultValue targetRV = storageMgr.getVariableValue(target.tokenStr);
        SubClassif type = targetRV.type;
        StringBuilder valueString = new StringBuilder();
        int index = 0;
        ArrayList<ResultValue> buffer = new ArrayList<>();

        while(!scan.currentToken.tokenStr.equals(endTerm)){

            if(scan.currentToken.primClassif == Classif.SEPARATOR) {
                valueString.append(scan.currentToken.tokenStr);
                scan.getNext();
                continue;
            }
            String element;
            if(type == SubClassif.INTEGER){
                element =  Integer.toString((int)(Float.parseFloat(scan.currentToken.tokenStr)));
            } else if (type == SubClassif.FLOAT){
                element = Float.toString(Float.parseFloat(scan.currentToken.tokenStr));
            } else { // String variables
                element = scan.currentToken.tokenStr;
            }
            valueString.append(element);
            buffer.add(new ResultValue(element, "primitive", type));
            scan.getNext();
            index++;
        }
        ResultValue [] array = new ResultValue[index];
        for(int i = 0; i < buffer.size(); i++){
            array[i] = buffer.get(i);
        }
        targetRV.arr.setBounds(index);
        targetRV.structure = "fixed-array";
        targetRV.arr.arr = array;
        targetRV.value = valueString.toString();
        //System.out.print("Array assignment: " + targetRV);
        storageMgr.updateVariable(target.tokenStr, targetRV);

    }

    public void assignArray(String endTerm, Token tokAssign) throws Exception {
        ResultValue targetRV = storageMgr.getVariableValue(tokAssign.tokenStr);
        SubClassif type = targetRV.type;
        StringBuilder valueString = new StringBuilder();
        int bounds = targetRV.arr.getBounds();
        int index = 0;
        ResultValue [] array = new ResultValue[bounds];

        while(!scan.currentToken.tokenStr.equals(endTerm)){
            if (index >= bounds) {
                error("Number of variables assigned to " + tokAssign.tokenStr +
                        " exceeds " + bounds + " found " + index);
            }
            if(scan.currentToken.primClassif == Classif.SEPARATOR) {
                valueString.append(scan.currentToken.tokenStr);
                scan.getNext();
                continue;
            }
            String element;
            if(type == SubClassif.INTEGER){
                element =  Integer.toString((int)(Float.parseFloat(scan.currentToken.tokenStr)));
            } else if (type == SubClassif.FLOAT){
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


        /*while (!scan.currentToken.tokenStr.equals(endTerm)) {
            // in the event that one of the tokens is a float and the array is an int array
            if (scan.currentToken.subClassif != type) {
                if (scan.currentToken.subClassif == SubClassif.FLOAT) {
                    int periodIndex = scan.currentToken.tokenStr.indexOf(".");
                    String casted = scan.currentToken.tokenStr.substring(0, periodIndex);
                    ResultValue addRV = new ResultValue(casted, "primitive", type);
                    rvList.add(addRV);
                    scan.getNext();
                    continue;
                }
            }

            ResultValue addRV = new ResultValue(scan.currentToken.tokenStr, "primitive", type);

            rvList.add(addRV);
            scan.getNext();
        }
        targetRV.value = rvList.toString();
        targetRV.arr.arr = (ResultValue[]) rvList.toArray();
        storageMgr.updateVariable(tokAssign.tokenStr, targetRV);*/
    }
    public void forStmt(boolean bExec) throws Exception {
        int colPos = 0, lineNum = 0;
        colPos = scan.currentToken.iColPos;
        lineNum = scan.currentToken.iSourceLineNr;

        ResultValue rv;
        if (bExec) {

            //initialize ctrl variable. Current is on the operand
            scan.getNext();
            rv = setupCtrlVar();
            while (evalForStmt()) {
                rv = executeStatements(bExec);
                if (!rv.terminatingStr.equals("endfor"))
                    error("Expected endfor after while");
                scan.setPosition(lineNum, colPos);
            }
            rv = executeStatements(false);
        }
    }

    public ResultValue setupCtrlVar() throws Exception {
        ResultValue rv;
        Token ctrl;

        // variable must be instantiated first before entering for loop
        ctrl = scan.currentToken;
        rv = storageMgr.getVariableValue(ctrl);

        if (rv.type != SubClassif.INTEGER) {
            error("Control variable must be of type int", scan.currentToken);
        }

        // current token is pointing at token after the operand
        scan.getNext();

        if (scan.currentToken.tokenStr.equals("=")) {

            // check to see if its a int value
            try {
                Integer.parseInt(scan.nextToken);
            } catch (Exception e) {
                error("Value must be of type int", scan.nextToken);
            }

            rv.value = scan.nextToken.tokenStr;
            storageMgr.updateVariable(ctrl);

            // now, scan.nextToken should be 'to'
            scan.getNext();
            if (!scan.nextToken.tokenStr.equals("to")) {
                error("'to' expected. Syntax error", scan.nextToken);
            }

            // shift the tokens to where scan.currentToken is now pointing at the beginning of the sentinel value
            scan.getNext();
            scan.getNext();
        } else if (scan.currentToken.tokenStr.equals("in")) {
            
        } else if (scan.currentToken.tokenStr.equals("to")) {

        } else {
            error("Syntax error: token cannot be used in for statement", scan.currentToken);
        }

        rv = storageMgr.getVariableValue(ctrl);
        return rv;
    }
        /*
        if (!bExec) {
            skipTo(":");
            rv = executeStatements(false);
        } else {

            if (scan.nextToken.subClassif == SubClassif.IDENTIFIER) {

            //Shift the current token to the identifier

            //There should be two situations:
            //1. It is an identifier that will be assigned a value (e.g. i = 0)
            //   The variable must be an integer.
            //2. It is an identifier within an array (basically a for each)
             //
                scan.getNext();

                // This is situation 1
                if (scan.nextToken.tokenStr.equals("=")) {
                //
                We first need to check if the variable (currentToken) already exists
                in the StorageManager. If it doesn't, throw an error stating that the
                variable needs to exist in order to do an assignment on it.
                //
                    //ResultValue rv;
                    String ctrlVar = scan.currentToken.tokenStr;

                    rv = storageMgr.getVariableValue(scan.currentToken);


                    if (rv.type != SubClassif.INTEGER) {
                        error("Control variable must be an integer if an assignment is done in the for loop."
                                , scan.currentToken);
                    }

                    // current Token is now the = and the new value we're assigning ctrlVar to
                    scan.getNext();

                    rv.value = scan.nextToken.tokenStr;
                    storageMgr.updateVariable(ctrlVar, rv);

                    // current = new value, next should "to"
                    scan.getNext();

                    if (!scan.nextToken.tokenStr.equals("to")) {
                        error("Expected 'to' for constructed for loop", scan.nextToken);
                    }

                    //So currentToken is the first element of the expression
                    scan.getNext(); // current = 'to', next = beginning of expr
                    scan.getNext(); // current = beginning of expr, next = either next part of expr or ':'

                    ResultValue endCond = expr.evaluateExpression(":");

                    // Compare the ctrlVar and endCond values
                    int ctrl = 0;
                    int end = 0;

                    //ctrl = Integer.parseInt(rv.value);
                    end = Integer.parseInt(endCond.value);

                    ResultValue rvBool;
                    for (ctrl=Integer.parseInt(rv.value); ctrl < end; ctrl++) {
                        rvBool = executeStatements(bExec);
                        if (!rvBool.terminatingStr.equals("endfor")) {
                            error("Expected 'endfor' for created for loop");
                        }
                        scan.setPosition(lineNum, colPos);
                    }
                }
            }*/


        /*
        //if(!rv.terminatingStr.equals("endfor"))
        //    error("Expected 'endfor' after while loop");
        scan.getNext();
        if(!scan.currentToken.tokenStr.equals(";"))
            error("Expected ';' after 'endfor'");*/


    public boolean evalForStmt() throws Exception {
        boolean bExec;
        ResultValue rv;
        Token ctrl;

        // shift current token to be the identifier
        scan.getNext();

        if (scan.currentToken.subClassif != SubClassif.IDENTIFIER) {
            error("Identifier expected.", scan.currentToken);
        }

        // check to see if the variable has already been instantiated and check to see if the variable is an int
        rv = storageMgr.getVariableValue(scan.currentToken);
        if (rv.type != SubClassif.INTEGER) {
            error("Variable in for loop must be of type integer", scan.currentToken, rv.type);
        }

        if (scan.nextToken.tokenStr.equals("=")) {
            ctrl = scan.currentToken;

            // move currentToken to be the =
            scan.getNext();

            try {
                Integer.parseInt(scan.nextToken);
            } catch (Exception e) {
                error("Value being declared must be of type int", scan.nextToken);
            }

            rv.value = scan.nextToken.tokenStr;
            storageMgr.updateVariable(ctrl.tokenStr, rv);

            // current token is the value, next token should be 'to'
            scan.getNext();

            if (!scan.nextToken.tokenStr.equals("to")) {
                error("Expected 'to' token. Token:", scan.nextToken);
            }

            // current token is now the 'to'
            scan.getNext();

            // 3 situations: scan.nextToken can be 3 things
            // an int value
            // an identifier (needs to be int)
            // built-in function

            // if its a plain int
        }


    }
}
