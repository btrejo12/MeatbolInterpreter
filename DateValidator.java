package meatbol;

public class DateValidator {

    private static final int TOTAL_MONTHS = 12;
    private static final int[] DAYS31 = {1,3,5,7,8,10,12};
    private int index = 0;
    private static final String NUMBERS = "1234567890";

    private String year = null;
    private String month = null;
    private String day = null;

    public DateValidator() { }

    public boolean validateDate(String strTok) {
        String str = null;

        if (strTok.length() != 10) {
            return false;
        }

        for (int i = 0; i < strTok.length(); i++) {
            if (strTok.charAt(i) == '-') {

            }
        }
        return true;
    }
}