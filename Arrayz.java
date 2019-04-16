package meatbol;

import javax.xml.transform.Result;
import java.util.ArrayList;

public class Arrayz {
    private int bounds;
    //public String name;
    public ResultValue []  arr;
    private ResultValue owner;


    public Arrayz(ResultValue owner){
        this.owner = owner;
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
    public int elem(){
        int ret = -1;
        for(int i = arr.length; i >=0; i--){
            if(arr[i] != null)
                ret = i;
        }
        return ret;
    }

    /**
     * Returns the number of populated elements.
     * @return
     */
    public int maxelem(){
        int counter = 0;
        for(int i = 0; i < arr.length; i++){
            if (arr[i] != null)
                counter++;
        }
        return counter;
    }

    public String toString(){
        return this.arr.toString();
    }

}
