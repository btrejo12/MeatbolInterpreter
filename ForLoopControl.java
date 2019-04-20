package meatbol;

public class ForLoopControl {

    private ResultValue controlVariable;
    private ResultValue limit;
    private ResultValue incr;

    private Parser parser;
    private Scanner scan;
    private StorageManager stoMgr;
    private Expression expr;

    private boolean incrExists = false;
    private boolean isRange = false;
    private boolean isSplit = false;
    private boolean isArray = false;
    private ResultValue index;
    private Token target;
    private int bounds;

    /**
     * ForLoopControls are only declared when we see a for loop, it is responsible for retrieving the control variable,
     * the limit, and the increment factor of the for loop condition. It updates the control variable in the storage manager
     * for each iteration.
     * @param parser
     * @param scan
     * @param stoMgr
     * @param expr
     */
    public ForLoopControl(Parser parser, Scanner scan, StorageManager stoMgr, Expression expr){
        this.parser = parser;
        this.scan = scan;
        this.stoMgr = stoMgr;
        this.expr = expr;
    }

    /**
     * The initial set up of a for loop condition in order to find the control variable, the limit, and the increment.
     * @throws Exception
     */
    public void setUpCondition() throws Exception{
        // Entering this method, we should be on the first token of the for loop
        // condition.
        //System.out.println("Coming into setting up condition: " + scan.currentToken.tokenStr);
        target = scan.currentToken;
        if(scan.nextToken.primClassif != Classif.CONTROL){ // The control variable is an expr
            scan.getNext();
            if(scan.currentToken.tokenStr.equals("=")){
                scan.getNext(); //put us on the expr
                controlVariable = expr.evaluateExpression("to in from");
            } else {
                scan.currentToken.printToken();
                parser.error("Expected '=' in control variable assignment for 'for' loop: "
                        + scan.currentToken.tokenStr);
            }
        } else { // single variable control, no assignments
            controlVariable = expr.evaluateExpression("to in from");
        }
        // We should now be on a 'in', 'to', or 'from'
        if(scan.currentToken.primClassif != Classif.CONTROL)
            parser.error("Expected a 'to', 'in', or 'from' token following control variable");

        String flow = scan.currentToken.tokenStr;
        scan.getNext(); // puts us on the limit expr
        if(flow.equals("to")){
            // Integer range
            index = controlVariable;
            isRange = true;
            limit = expr.evaluateExpression("by :"); //by is optional
            bounds = Integer.parseInt(limit.value);
            if(scan.currentToken.primClassif == Classif.CONTROL){ //next token is 'by'
                incrExists = true;
                scan.getNext();
                incr = expr.evaluateExpression(":");
            } else if (!scan.currentToken.tokenStr.equals(":")) {
                parser.error("Expected colon following 'for' condition: " + scan.currentToken.tokenStr);
            } else
                incr = new ResultValue("1", "primitive", SubClassif.INTEGER);
        } else if (flow.equals("in")){
            isArray = true;
            limit = expr.evaluateExpression(":");
            incr = new ResultValue("1", "primitive", SubClassif.INTEGER);
        } else if (flow.equals("from")){
            isSplit = true;
            limit = expr.evaluateExpression("by");
            if(!scan.currentToken.tokenStr.equals("by"))
                parser.error("Expected 'by' following 'for' condition: " + scan.currentToken.tokenStr);
            scan.getNext(); // get on the 'by' variable
            incr = expr.evaluateExpression(":");
        } else
            parser.error("Invalid flow statement following 'for' loop control variable: " + flow);

        index = new ResultValue("0", "primitive", SubClassif.INTEGER);
        if(!isRange){
            if(isSplit){
                String [] tokens = limit.value.split(incr.value);
                controlVariable = new ResultValue(tokens[0], "primitive", SubClassif.STRING);
                bounds = tokens.length;
            }else {
                ResultValue res = limit.arr.get(index);
                assignControl(res);
                ResultValue rv = limit.arr.elem();
                bounds = Integer.parseInt(rv.value);
            }
        }
        stoMgr.updateVariable(target.tokenStr, controlVariable);
        //System.out.println("End for set up, cv: " + controlVariable.value + ", limit: "
        //        + limit.value + ", incr: " + incr.value + ", bounds: " + bounds);
    }

    /**
     * Is called during each iteration to decide whether the condition is true or false dependant on the control variable.
     * @return A boolean specifying whether this for loop should keep running
     * @throws Exception
     */
    public boolean evaluateCondition() throws Exception{

        int idx = Integer.parseInt(index.value);
        if(idx >= bounds)
            return false;
        if(isArray){
            ResultValue rv = limit.arr.get(index);
            assignControl(rv);
            idx = idx + Integer.parseInt(incr.value);
            index = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            stoMgr.updateVariable(target.tokenStr, controlVariable);
            return true;
        } else if(isSplit){
            String [] tokens = limit.value.split(incr.value);
            ResultValue rv = new ResultValue(tokens[idx], "primitive", SubClassif.STRING);
            assignControl(rv);
            idx++;
            index = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            stoMgr.updateVariable(target.tokenStr, controlVariable);
            return true;
        } else {
            ResultValue rv = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            assignControl(rv);
            idx = idx + Integer.parseInt(incr.value);
            index = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            stoMgr.updateVariable(target.tokenStr, controlVariable);
            return true;
        }
    }

    /**
     * A stupid function to get around Java's by reference variable assignment.
     * @param assign The ResultValue we're assigning to the control variable
     */
    private void assignControl(ResultValue assign){
        if(assign.type != null)
            controlVariable.type = assign.type;
        controlVariable.value = assign.value;
        controlVariable.structure = assign.structure;
        controlVariable.terminatingStr = assign.terminatingStr;
        controlVariable.arr = assign.arr;
        //System.err.println("From control assignment: " + controlVariable.value + " at " + index.value);
    }
}
