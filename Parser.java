package meatbol;

import javax.xml.transform.Result;

public class Parser {
    private Scanner scan;
    private StorageManager storageMgr;
    private SymbolTable st;

    public Parser(String filename, SymbolTable st){
        System.out.println("Parser entry");
        storageMgr = new StorageManager();
        this.st = st;
        scan = new Scanner(filename, st);
        scan.sManager = storageMgr;
        try {
            while (scan.trigger){
                scan.getNext();
                System.out.print("Parse while entry, current: ");
                scan.currentToken.printToken();
                System.out.print("Parse while entry, next: ");
                scan.nextToken.printToken();
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
                    } else {
                        //Not sure here
                        scan.nextToken.printToken();
                    }
                } else if(scan.currentToken.primClassif == Classif.FUNCTION){
                        handleFunction();
                } else {
                    System.err.println("Weve hit an unknown token");
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

    private ResultValue expr(String endingDelimiter){
        ResultValue res = new ResultValue();

        return res;
    }

    /**
     * This function is called when the grammer has type 'variable = expr ;'. The current token should be set to 'variable'.
     * @param bExec the trigger to determine whether the assignment statement should run (it may be inside a false if statement
     *              therefore we should not run it in this case)
     * @return the ResultValue of this assignment
     */
    private ResultValue assignmentStmt(Boolean bExec){
        ResultValue res = new ResultValue();
        if(!bExec){
            skipTo(';');
            res.type = "empty";
        }

        return res;
    }


    /**
     * This function is called when a outer most if statement is scanned. I don't think this is used in nexted if statements tho,
     * only the executeStatements method, so if you're calling from Parser's constructor then bExec should be set to true.
     * @param bExec the trigger whether to run the code inside of the if (based on the condition) or not.
     */
    private void ifStmt(Boolean bExec){

    }

    /**
     * This method is a little more tricky in that it should use Scanner's setPosition to figure out where to loop back to.
     * @param bExec
     */
    private void whileStmt(Boolean bExec){

    }

    /**
     * Once you return a result value, you should assign it to the left most variable (variableString) and return what you
     * assigned to it. This method should use StorageManager to check for data type.
     * @param variableString The variable string to be looked up in StorageManager to confirm data type and declaration.
     * @param res2 The ResultValue you're assigning to the variableString
     * @return The ResultValue that was assigned
     */
    private ResultValue assign(String variableString, ResultValue res2){
        ResultValue res= new ResultValue();

        return res;
    }

    /**
     * This method is called when you find a control-flow variable (if, while) in order to determine whether the conditional
     * is true or false. This method should use getNext to be able execute the tokens and determine the boolean they result in.
     * @return The boolean value of whether this condition is true or false.
     */
    private ResultValue evalCond(){
        ResultValue res = new ResultValue();

        return res;
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
                ResultValue variableEval = expr(","); //only evaluate this expression
                System.out.print(variableEval.value);
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

    }

}
