package meatbol;

import javax.xml.transform.Result;
import java.util.ArrayList;

public class Arrayz {
    private int bounds;
    //public String name;
    public ResultValue []  arr;
    private ResultValue owner;
    private SubClassif type;


    public Arrayz(ResultValue owner){
        this.owner = owner;
        this.type = owner.type;
        bounds = -1; //undeclared size
    }

    public void setBounds(ResultValue limit) throws Exception{
        if (limit.type != SubClassif.INTEGER)
            throw new Exception("Array size must be of type integer, found:" + limit.value);
        arr = new ResultValue[Integer.parseInt(limit.value)];
        bounds = Integer.parseInt(limit.value);
    }

    public void setBounds(int bounds){
        arr = new ResultValue[bounds];
        this.bounds = bounds;
    }

    public int getBounds(){
        return bounds;
    }

    public void update(int index, ResultValue addition) throws Exception{
        if(index >= bounds || index < 0){
            throw new Exception("Array index out of bounds");
        }
        arr[index] = addition;
        //TODO: Update the RV's value string in StorageManager.
    }

    public void add(ResultValue value) throws Exception{
        // ???
        //TODO: Update the RV's value string in StorageManager
    }

    public ResultValue get(ResultValue index) throws Exception{
        if(owner.type == SubClassif.STRING){
            int position = Integer.parseInt(index.value);
            String element = Character.toString(owner.value.charAt(position));
            return new ResultValue(element, "primitive", owner.type);
        }

        ResultValue rv;
        int ind;
        try{
            ind = Integer.parseInt(index.value);
        } catch (Exception e){
            throw new Exception("Invalid index");
        }
        rv = arr[ind];
        return rv;
    }

    /**
     * Returns the subscript of the highest populated element
     * @return The index of the highest populated element
     */
    public ResultValue elem(){
        int ret = 0;
        for(int i = arr.length-1; i >=0; i--){
            if(arr[i] != null) {
                ret = i;
                break;
            }

        }
        if(ret != 0)
            ret++; // Cause clark said plus one for some odd reason
        ResultValue res = new ResultValue(Integer.toString(ret), "primitive", SubClassif.INTEGER);
        return res;
    }

    /**
     * Returns the number of populated elements.
     * @return
     */
    public ResultValue maxelem(){
        ResultValue res = new ResultValue(Integer.toString(bounds), "primitive", SubClassif.INTEGER);
        return res;
    }

    public ResultValue stringLength() throws Exception{
        if(owner.type != SubClassif.STRING)
            throw new Exception("Function 'LENGTH' can only be used on Strings");
        ResultValue rv = new ResultValue(Integer.toString(owner.value.length()), "primitive", SubClassif.INTEGER);
        return rv;
    }

    public ResultValue stringSpaces() throws Exception{
        if(owner.type != SubClassif.STRING)
            throw new Exception("Function 'SPACES' can only be used on Strings");
        String valueString = owner.value;
        int spaces = 0;
        for(int i = 0; i < valueString.length(); i++){
            if(valueString.charAt(i) == ' ')
                spaces++;
        }
        ResultValue spacesRV = new ResultValue(Integer.toString(spaces), "primitive", SubClassif.INTEGER);
        return spacesRV;
    }

    public void updateElement(ResultValue index, ResultValue value) throws Exception{
        if(owner.type == SubClassif.STRING){
            StringBuilder valueString = new StringBuilder(owner.value);
            int position = Integer.parseInt(index.value);
            if(value.value.length() > 1){
                int end = Math.min(owner.value.length(), position+value.value.length());
                valueString.replace(position, end, value.value);
            } else {
                char change = value.value.charAt(0);
                valueString.setCharAt(position, change);
            }
            owner.value = valueString.toString();
            return;
        }
        // For array objects
        int idx;
        try {
            idx = Integer.parseInt(index.value);
        } catch(Exception e){
            throw new Exception("Invalid index: " + index.value);
        }
        arr[idx] = value;
        if(value.type != SubClassif.STRING)
            updateString();
    }

    public void updateString(){
        StringBuilder sb = new StringBuilder();
        for(ResultValue rv: arr){
            if(rv == null)
                sb.append("null, ");
            else
                sb.append(rv.value + ", ");
        }
        owner.value = sb.toString();
    }

    public String toString(){
        return this.arr.toString();
    }

}
