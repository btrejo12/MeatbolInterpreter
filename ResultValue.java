package meatbol;

public class ResultValue {
    public SubClassif type; // Data type of this ResultValue
    public String value; // Value of this ResultValue
    public String structure; // Structure of this ResultValue (fixed array, primitive, etc)
    public String terminatingStr; // Terminating Statement (endwhile, endfor, etc)

    public ResultValue() {}

    public ResultValue(String value, String structure, SubClassif type){
        this.value = value;
        this.structure = structure;
        this.type = type;
    }
}