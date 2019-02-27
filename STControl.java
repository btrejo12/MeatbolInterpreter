package meatbol;

public class STControl extends STEntry{

    public SubClassif subClassif;

    /**
     * <p>STControl declares a type of STEntry that is used for control.</p>
     * @param string The string name for this Control expression
     * @param classification The classification for this control expression
     * @param subclassification The subclassification for this control expression
     */
    public STControl(String string, Classif classification, SubClassif subclassification) {
        super(string, classification);
        subClassif = subclassification;
    }
}
