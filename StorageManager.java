package meatbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StorageManager {

    private HashMap<String, ResultValue> variables = null;

    public StorageManager() {
        this.variables = new HashMap<String, ResultValue>();
    }

    public void addVariable(String variable, ResultValue value) throws Exception {
        if (variables.containsKey(variable)) {
            throw new Exception("Error: Variable '" + variable + "' has already been instantiated");
        }

        variables.put(variable, value);
    }

    public void updateVariable(String variable, ResultValue value) throws Exception {
        if (!variables.containsKey(variable)) {
            try {
                addVariable(variable, value);
            } catch (Exception e) {
                throw e;
            }
            //throw new Exception("Error: Variable '" + variable + "' has not already been instantiated");
        }

        variables.put(variable, value);
    }

    public ResultValue getVariableValue(String variable) throws Exception {
        
        //Initialize an empty ResultValue
        ResultValue rv = new ResultValue();

        // test to see if the variable is just an undeclared integer
        try {
            Integer.parseInt(variable);

            //if it is just an integer, set up rv as int
            rv.type = SubClassif.INTEGER;
            rv.value = variable;
            rv.structure = "primitive";
        } catch (Exception e) {

            // test to see if the variable is just a straight up float
            try {
                Float.parseFloat(variable);

                //setup float rv
                rv.type = SubClassif.FLOAT;
                rv.value = variable;
                rv.structure = "primitive";

            } catch (Exception e) {
                // TODO: delete this eventually. This is for debugging
                System.out.println("This is a potential variable");
            }
        }

        // if the variable passed through "variable" is not found in the SM
        if (!variables.containsKey(variable)) {
            throw new Exception("Error: Variable '" + variable + "' does not exist");
        } else {
            rv = variables.get(variable);
        }

        return rv;
    }

    public ResultValue getUnaryVariableValue(String variable) throws Exception {
        if (!variables.containsKey(variable)){
            throw new Exception("Error: Variable '" + variable + "' does not exists");
        }
        ResultValue rv = variables.get(variable);
        rv.value = "-" + rv.value;
        variables.put(variable, rv);
        return rv;
    }
}