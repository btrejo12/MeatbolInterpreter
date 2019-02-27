package meatbol;

public class DateValidator {

    //private static final int TOTAL_MONTHS = 12;
    //private static final String[] DAYS31 = {"1","3", "5", "7", "8", "10", "12"};
    private int index = 0;
    private static final String NUMBERS = "1234567890";

    private String year = null;
    private String month = null;
    private String day = null;

    /**
     * <p>DateValidator constructor that doesn't need to be used right now</p>
     */
    public DateValidator() { }

    /**
     * ValidateDate validates the string as a Date object
     * @param strTok is the string format of the date to be validated
     * @return whether the passed in string is in a valid date format
     */
    public boolean validateDate(String strTok){
        String str = null;
        int index = 0;
        if (strTok.length() != 10) {
            return false;
        }

        for (int i = 0; i < strTok.length(); i++) {
            if (strTok.charAt(i) == '-') {
                if (year == null) {
                    String potYear = strTok.substring(index, i);
                    boolean isYear = testYear(potYear);
                    if (!isYear) {
                        return false;
                    }
                    this.year = potYear;
                    index = i + 1;
                } else if (month == null) {
                    String potMonth = strTok.substring(index, i);
                    boolean isMonth = testMonth(potMonth);
                    if (!isMonth) {
                        return false;
                    }
                    this.month = potMonth;
                    index = i + 1;
                }
            }
        }
        return true;
    }

    /**
     * <p>TestYear validates the year part of the date string</p>
     * @param year the year part of the date string
     * @return whether the year is a valid year type
     */
    private boolean testYear(String year) {
        if (year.length() != 4) {
            return false;
        }

        char[] pYear = year.toCharArray();
        for (char c: pYear) {
            if (NUMBERS.indexOf(c) < 0)
                return false;
        }

        return true;
    }

    /**
     * <p>TestMonth validates whether the specified string is a valid month</p>
     * @param month the string to be tested for month
     * @return whether the string is a valid month or not
     */
    private boolean testMonth(String month) {
        if (month.length() > 2 || month.length() < 1) {
            return false;
        }
        // if the month has 31 days
        int iMonth = Integer.parseInt(month);
        if (iMonth < 1 || iMonth > 12) {
            return false;
        }
        return true;
    }
}