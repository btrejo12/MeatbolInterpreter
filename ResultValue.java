package meatbol;

public class ResultValue {
    public SubClassif type; // Data type of this ResultValue
    public String value; // Value of this ResultValue
    public String structure; // Structure of this ResultValue (fixed array, primitive, etc)
    public String terminatingStr = ""; // Terminating Statement (endwhile, endfor, etc)
    public Arrayz arr;
    /**
     * Declared an empty ResultValue
     */
    public ResultValue() {}

    /**
     * <p>Populated constructor for easy declaration of ResultValue object </p>
     * @param value     The string value
     * @param structure     The type of structure this ResultValue should be
     * @param type      The type of classification for this ResultValue to be used for error checking and comparisons
     */
    public ResultValue(String value, String structure, SubClassif type){
        this.value = value;
        this.structure = structure;
        this.type = type;
    }

    public ResultValue(String value, String structure, SubClassif type, int bounds){
        this.value = value;
        this.structure = structure;
        this.type = type;
        this.arr = new Arrayz(bounds);
    }

    public String toString(){
        if (this.type != SubClassif.ARRAY){
          return this.value;
        } else {
            return this.arr.toString();
        }
    }
}