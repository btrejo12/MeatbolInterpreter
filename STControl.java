package meatbol;

public class STControl extends STEntry{

    public SubClassif subClassif;

    public STControl(String string, Classif classification, SubClassif subclassification) {
        super(string, classification);
        subClassif = subclassification;
    }
}
