package meatbol;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageManager {

    private ArrayList<String> varTable;
    private HashMap<String, String> varValues;
    private HashMap<String, String> varTypes;

    public StorageManager() {
        this.varTable = new ArrayList<String>();
        this.varValues = new HashMap<String, String>();
        this.varValues = new HashMap<String, String>();
    }

    public void createVar(String var, String value) throws Exception {
        if (varTable.contains(var)) {
            throw new Exception("Variable " + var + " already exists");
        }

        varTable.add(var);
        varValues.put(var, value);
        varValues.put(var, value);
    }

    public void updateVar(String var, String value) {
        varValues.put(var, value);
    }

    public void getVarValue(String var) {
        return varValues.get(var);
    }
}
