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
            throw new Exception("Error: Variable '" + variable + "' has not already been instantiated");
        }

        variables.put(variable, value);
    }

    public ResultValue getVariableResultValue(String variable) throws Exception {
        if (!variables.containsKey(variable)) {
            throw new Exception("Error: Variable '" + variable + "' does not exist");
        }

        ResultValue rv = variables.get(variable);
        return rv;
    }
}