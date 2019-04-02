package meatbol;

public class ParserException extends Exception {

    public int iLineNr;
    public String diagnostic;
    public String sourceFileName;

    /**
     * Initialize new exception to be thrown when finding an error during parsing
     * @param iLineNr       The line number the error was found on
     * @param diagnostic    The text to output to give information about the error
     * @param sourceFileName    The source file this error was found in
     */
    public ParserException(int iLineNr, String diagnostic, String sourceFileName)
    {
        this.iLineNr = iLineNr;
        this.diagnostic = diagnostic;
        this.sourceFileName = sourceFileName;
    }

    /**
     * toString method that must be included for Exceptions in order to be printed
     * @return  the Exception string to print
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Line ");
        sb.append(Integer.toString(iLineNr));
        sb.append(" ");
        sb.append(diagnostic);
        sb.append(", File: ");
        sb.append(sourceFileName);
        return sb.toString();
    }
}