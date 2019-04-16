package meatbol;

import java.util.ArrayList;

public class Arrayz {
    public int bounds;
    //public String name;
    public ArrayList<ResultValue> arr = new ArrayList<ResultValue>();
    private ResultValue owner;


    public Arrayz(ResultValue owner){
        this.owner = owner;
    }

    public void update(int index, ResultValue addition) throws Exception{
        if(index >= bounds || index < 0){
            throw new Exception("Array index out of bounds");
        }
        arr.set(index, addition);
    }

    public void add(ResultValue value) throws Exception{
        arr.add(value);
    }

    public String toString(){
        return this.arr.toString();
    }

}
