package meatbol;

public class Arrayz {
    private int bounds;
    //public String name;
    public ResultValue []  arr;
    private ResultValue owner;
    private SubClassif type;


    /**
     * Arrayz constructor attaches itself to it's ResultValue and sets its bounds to undeclared (-1) or the length
     * of the String that it is attached to.
     * @param owner The ResultValue this Arrayz object belongs to
     */
    public Arrayz(ResultValue owner){
        this.owner = owner;
        this.type = owner.type;
        if(owner.type == SubClassif.STRING){
            bounds = owner.value.length();
        } else {
            bounds = -1; //undeclared size
        }
    }

    /**
     * A setter used to initialize the array object with a set size, which is why this variable is private, so
     * DO NOT CHANGE IT TO PUBLIC
     * @param limit The ResultValue size this array was initialized to be
     * @throws Exception throws an exception if an the size is not a valid integer
     */
    public void setBounds(ResultValue limit) throws Exception{
        if (limit.type != SubClassif.INTEGER)
            throw new Exception("Array size must be of type integer, found:" + limit.value);
        arr = new ResultValue[Integer.parseInt(limit.value)];
        bounds = Integer.parseInt(limit.value);
    }

    /**
     * A setter used to initialize the array size by changing the bounds, this does not set up the array.
     * @param bounds The changed size of the array
     */
    public void setBounds(int bounds){
        arr = new ResultValue[bounds];
        this.bounds = bounds;
    }

    /**
     * A getter to get the bounds since this variable is private
     * @return The size of this arrayz object
     */
    public int getBounds(){
        return bounds;
    }


    /**
     * Returns the element at the specified index
     * @param index The ResultValue of the index requested of this arrayz object
     * @return The ResultValue at the index specified
     * @throws Exception Throws exception when index is not a valid integer
     */
    public ResultValue get(ResultValue index) throws Exception{
        if(owner.type == SubClassif.STRING){
            int position = Integer.parseInt(index.value);
            if (position < 0) { // negative subscript
                ResultValue length = elem();
                int iLength = Integer.parseInt(length.value);
                position = position - iLength;
                if (position < 0) { // out of index, negative too big
                    throw new Exception("Index " + position + " is out of bounds for length: " + owner.value.length());
                }
            }
            String element = Character.toString(owner.value.charAt(position));
            return new ResultValue(element, "primitive", owner.type);
        }

        ResultValue rv;
        int ind;
        try{
            ind = Integer.parseInt(index.value);
            if(ind < 0) {
                ResultValue length = elem();
                int iLength = Integer.parseInt(length.value);
                ind = ind - iLength;
                if (ind < 0){
                    throw new Exception("Index " + ind + " is out of bounds for length: " + bounds);
                }
            }
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
        if(owner.type == SubClassif.STRING && owner.value != null)
            return new ResultValue(Integer.toString(owner.value.length()), "primitive", SubClassif.INTEGER);
        int ret = 0;
        for(int i = arr.length-1; i >=0; i--){
            if(arr[i] != null) {
                ret = i;
                break;
            }

        }
        if(ret != 0)
            ret++; // Cause clark said plus one for some odd reason
        return new ResultValue(Integer.toString(ret), "primitive", SubClassif.INTEGER);
    }

    /**
     * Returns the number of populated elements.
     * @return Returns a new ResultValue reflecting the current bounds of the array
     */
    public ResultValue maxelem(){
        return new ResultValue(Integer.toString(bounds), "primitive", SubClassif.INTEGER);
    }

    /**
     * If this array object is a string, we return it's length
     * @return The ResultValue containing this string's length
     */
    public ResultValue stringLength(){
        //if(owner.type != SubClassif.STRING)
            //throw new Exception("Function 'LENGTH' can only be used on Strings");
        return new ResultValue(Integer.toString(owner.value.length()), "primitive", SubClassif.INTEGER);
    }

    /**
     * Returns the number of spaces in this String array object.
     * @return The ResultValue containing the number of spaces
     * @throws Exception Throws a new exception if the owner is not of type string
     */
    public ResultValue stringSpaces() throws Exception{
        if(owner.type != SubClassif.STRING)
            throw new Exception("Function 'SPACES' can only be used on Strings");
        String valueString = owner.value;
        String rvBool="T";
        if(valueString.length() == 0){
            rvBool = "T";
        }else {
            for (int i = 0; i < valueString.length(); i++) {
                if (valueString.charAt(i) != ' '){
                    rvBool = "F";
                }
            }
        }
        return new ResultValue(rvBool, "primitive", SubClassif.BOOLEAN);
    }

    /**
     * Updates and element of this array and updates the string of it's ResultValue
     * @param index The index to be changed
     * @param value The value the index will be changed to
     * @throws Exception Throws a new Exception if the element cannot be converted to the proper type
     */
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
        ResultValue newValue = new ResultValue();
        newValue.value = value.value;
        newValue.type = value.type;
        newValue.structure = value.structure;
        newValue.terminatingStr = value.terminatingStr;
        newValue.arr = value.arr;
        arr[idx] = newValue;
        //System.err.println("After array update element: " + Arrays.toString(arr));
        if(value.type != SubClassif.STRING)
            updateString();
    }

    /**
     * Copies an existing array to this array object.
     * @param source The array to be copied into this one
     * @param end The ending index of where to stop copying
     * @throws Exception Throws an exception if the two arrays are not of the same type
     */
    public void copyArray(ResultValue source, int end) throws Exception {
        if(owner.type != source.type)
            throw new Exception("Expected target array and assignment array to be of same type.");
        if(owner.type == SubClassif.STRING){
            // Copy the contents of source's string into this string
            StringBuilder sb = new StringBuilder();
            for(int i=0; i < end; i++){
                sb.append(source.value.charAt(i));
            }
            owner.value = sb.toString();
        } else {
            for(int i=0; i < end; i++){
                arr[i] = source.arr.arr[i];
            }
        }
    }

    /**
     * Updates the ResultValue's string when the array it changed.
     */
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

    /**
     * This is supposed to be for easy printing but I doubt it works
     * @return Returns a string of the array characters
     */
    public String toString(){
        return this.arr.toString();
    }

}
