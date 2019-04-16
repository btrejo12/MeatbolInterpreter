package meatbol;

import java.util.ArrayList;

public class Arrayz {
    public int bounds;
    //public String name;
    private String arr[];


    public Arrayz(int bounds){
        //this.name = name;
        this.bounds = bounds;
        this.arr = new String[bounds];
    }

    public void add(int index, String value) throws Exception{
        if(index >= bounds || index < 0){
            throw new Exception();
        }
        arr[index] = value;
    }

    public void add(String value) throws Exception{
        for(int i = 0; i< bounds; i++){
            if(arr[i] != null)
                arr[i] = value;
        }

    }

    public String toString(){
        return this.arr.toString();
    }

}
