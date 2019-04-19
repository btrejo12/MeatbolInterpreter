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

    public ForLoopControl(Parser parser, Scanner scan, StorageManager stoMgr, Expression expr){
        this.parser = parser;
        this.scan = scan;
        this.stoMgr = stoMgr;
        this.expr = expr;
    }

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
                controlVariable = limit.arr.get(index);
                ResultValue rv = limit.arr.elem();
                bounds = Integer.parseInt(rv.value);
            }
        }
        stoMgr.updateVariable(target.tokenStr, controlVariable);
        //System.out.println("End for set up, cv: " + controlVariable.value + ", limit: "
                //+ limit.value + ", incr: " + incr.value + ", bounds: " + bounds);
    }

    public boolean evaluateCondition() throws Exception{

        int idx = Integer.parseInt(index.value);
        if(idx >= bounds)
            return false;
        if(isArray){
            controlVariable = limit.arr.get(index);
            idx = idx + Integer.parseInt(incr.value);
            index = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            stoMgr.updateVariable(target.tokenStr, controlVariable);
            return true;
        } else if(isSplit){
            String [] tokens = limit.value.split(incr.value);
            controlVariable = new ResultValue(tokens[idx], "primitive", SubClassif.STRING);
            idx++;
            index = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            stoMgr.updateVariable(target.tokenStr, controlVariable);
            return true;
        } else {
            controlVariable = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            idx = idx + Integer.parseInt(incr.value);
            index = new ResultValue(Integer.toString(idx), "primitive", SubClassif.INTEGER);
            stoMgr.updateVariable(target.tokenStr, controlVariable);
            return true;
        }
    }
}
