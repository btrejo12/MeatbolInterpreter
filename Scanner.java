package meatbol;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Scanner {

    public String sourceFileNm;
    public ArrayList<String> sourceLineM;
    public SymbolTable symbolTable;
    public char[] textCharM;
    public int iSourceLineNr;
    public int iColPos;
    public Token currentToken;
    public Token nextToken;

    private boolean trigger = true;
    private final static String delimiters = "\t;:()\'\"=!<>+-*/[]#,^\n";
    private final static String operators = "+-*/<>=!";
    private final static String digits = "0123456789";
    private final static String separators = "(),:;[]";

    /**
     * <p>The constructor for the Scanner class initializes local variables from the file passed in.
     * </p>
     * The constructor is in charge of finding the first token in order for the helper methods to find the rest.
     * @param fileName The file to be read in
     * @param symbolTable A table of symbols to be used later
     */
    public Scanner(String fileName, SymbolTable symbolTable){
        BufferedReader reader;
        sourceFileNm = fileName;
        this.symbolTable = symbolTable;

        sourceLineM = new ArrayList<>();
        try {
            //Read source file and populate sourceLineM
            reader = new BufferedReader(new FileReader(sourceFileNm));

            String line = reader.readLine();
            int i = 0;
            while (line != null){
                sourceLineM.add(i, line);
                line = reader.readLine();

                i++;
            }
            reader.close();
        } catch (FileNotFoundException e){
            System.err.println("Input file could not be found");
            e.printStackTrace();
        } catch (IOException e){
            System.err.println("Could not read line from input file");
            e.printStackTrace();
        }

        //Initialize current token and next token (to empty?)
        currentToken = new Token();
        nextToken = new Token();

        //Set first token to nextToken, look at first line of file
        textCharM = sourceLineM.get(0).toCharArray();
        iSourceLineNr = 1;

        //System.out.println(iSourceLineNr + " " + sourceLineM.get(iSourceLineNr-1));

        for(int k = 0; k <= textCharM.length; k++ ){

            char currentChar = textCharM[k];

            if(delimiters.contains(Character.toString(currentChar)) || currentChar == ' '){

                if(k!=0){
                    String variableName = sourceLineM.get(iSourceLineNr-1).substring(0, k);
                    //System.out.println(variableName);
                    if(variableName.equals("Int") || variableName.equals("Float") || variableName.equals("String")){
                        assignNextToken(variableName, Classif.OPERAND, SubClassif.IDENTIFIER);
                        incrementColumnPosition(k);
                        break;
                    }
                }
            }
        }
    }

    /**
     *<p>GetNext is responsible for returning the current token in the sequence and finding the next one as well.</p>
     * @return The token string of current token.
     * @throws Exception in the case of invalid string inputs, number format, syntax errors, etc.
     */
    public String getNext() throws Exception{

        // Move next token into current token
        shiftTokens();

        // Trigger determines if we've reached EOF
        if (trigger) {
            skipEmptyLines();
            for (int i = 0; i <= textCharM.length; i++) {
                int index = iColPos + i;
                char currentChar = textCharM[index];

                //We've hit a space or delimiter
                if (delimiters.indexOf(currentChar) >= 0 || currentChar == ' ') {

                    //Nothing to observe before this character
                    if (i == 0) {

                        //Skip blank space
                        if (currentChar == ' ') {
                            incrementColumnPosition(i);
                            i = i - 1;
                            continue;
                        } //This is a operator token, assign it and update iCol
                        else if (operators.indexOf(currentChar) >= 0) {
                            assignNextToken(Character.toString(currentChar), Classif.OPERATOR, SubClassif.EMPTY);
                            incrementColumnPosition(i);
                            break;
                        } //This is a separator token, assign it and update iCol
                        else if (separators.indexOf(currentChar) >= 0) {
                            assignNextToken(Character.toString(currentChar), Classif.SEPARATOR, SubClassif.EMPTY);
                            incrementColumnPosition(i);
                            break;
                        } //We found string literals, evaluate them.
                        else if (currentChar == '\"') {
                            findStringLiteral('\"', iColPos);
                            break;
                        } else if (currentChar == '\'') {
                            findStringLiteral('\'', iColPos);
                            break;

                        }
                    }

                    //Observe what came before, and we will leave this character for the next iteration
                    if (i != 0) {
                        String variableName = sourceLineM.get(iSourceLineNr - 1).substring(iColPos, (iColPos + i));

                        //identifier?
                        if (variableName.equals("Int") || variableName.equals("Float") || variableName.equals("String")) {
                            assignNextToken(variableName, Classif.OPERAND, SubClassif.IDENTIFIER);
                            incrementColumnPosition(i);
                            break;
                        }
                        /* //if statement?
                        else if(variableName.equals("if")){
                            assignNextToken(variableName, Classif.CONTROL, SubClassif.FLOW);
                            incrementColumnPosition(i);
                            break;
                        } //end if?
                        else if (variableName.equals("end if")){
                            assignNextToken(variableName, Classif.CONTROL, SubClassif.END);
                            incrementColumnPosition(i);
                            break;
                        } //built in function?
                        else if (variableName.equals("print")){
                            assignNextToken(variableName, Classif.FUNCTION, SubClassif.BUILTIN);
                            incrementColumnPosition(i);
                            break;
                        }*/

                        // Digits
                        if (digits.indexOf(variableName.charAt(0)) >= 0) {
                            int numOfDecimals = countDecimals(variableName);

                            //No decimals present
                            if (numOfDecimals == 0) {
                                assignNextToken(variableName, Classif.OPERAND, SubClassif.INTEGER);
                                incrementColumnPosition(i - 1);
                                break;
                            } //One decimal
                            else if (numOfDecimals == 1) {
                                assignNextToken(variableName, Classif.OPERAND, SubClassif.FLOAT);
                                incrementColumnPosition(i - 1);
                                break;
                            } else {
                                throw new Exception("Line " + iSourceLineNr + ": Invalid number format, File: " + sourceFileNm);
                            }
                        }

                    /*
                    if(operators.indexOf(currentChar) >= 0){
                        assignNextToken(variableName, Classif.OPERATOR, SubClassif.EMPTY);
                        incrementColumnPosition(i-1);
                        break;
                    } else if (separators.indexOf(currentChar) >= 0){
                        assignNextToken(variableName, Classif.SEPARATOR, SubClassif.EMPTY);
                        incrementColumnPosition(i-1);
                        break;
                    }*/

                        //variable identifier
                        else {
                            assignNextToken(variableName, Classif.OPERAND, SubClassif.IDENTIFIER);
                            incrementColumnPosition(i - 1);
                            break;
                        }
                    }
                }
            }
        }
        return currentToken.tokenStr;
    }

    /**
     *<p>Assign next token helps reduce code redundancy by assigning the specific parameters to nextToken.</p>
     * @param tokenStr is the String to be assigned to nextToken
     * @param primary is the primary classification of nextToken
     * @param secondary is the secondary classification of nextToken
     */
    private void assignNextToken(String tokenStr, Classif primary, SubClassif secondary){
        nextToken.tokenStr = tokenStr;
        nextToken.primClassif = primary;
        nextToken.subClassif = secondary;
        nextToken.iColPos = iColPos;
    }

    /**
     *<p>shiftTokens is a helper method user to move the nextToken into the currentToken position.</p>
     */
    private void shiftTokens(){
        currentToken.primClassif = nextToken.primClassif;
        currentToken.subClassif = nextToken.subClassif;
        currentToken.tokenStr = nextToken.tokenStr;
        currentToken.iColPos = nextToken.iColPos;
    }

    /**
     *<p>findStringLiteral is a helper method used to find an entire string literal and updates the position in file.</p>
     * @param endingDelimiter is the character that ends the string literal sequence
     * @param startingIndex is the index where the string literal starts
     * @throws Exception is raised when the String Literal is not closed on the same line
     */
    private void findStringLiteral(char endingDelimiter, int startingIndex) throws Exception{
        for(int i=startingIndex+1; i<textCharM.length; i++){

            //System.out.println("Finding " + endingDelimiter+ " at position: " + i);
            char currentChar = textCharM[i];

            //Ending delimiter found and it is not escaped
            if(currentChar == endingDelimiter && textCharM[i-1] != '\\'){
                String variableName = sourceLineM.get(iSourceLineNr-1).substring(startingIndex+1, i);
                assignNextToken(variableName, Classif.OPERAND, SubClassif.STRING);
                iColPos = i+1;
                return;
            }
        }
        throw new Exception("Line " + iSourceLineNr + ": Sting literal must terminate on same line, File: " + sourceFileNm);
    }

    /**
     *<p>incrementColumnPosition updates the iColPos whenever we are moving between tokens.</p>
     * @param relativeIndex is the relative index within the for loop which must be added to iColPos
     */
    private void incrementColumnPosition(int relativeIndex){
        iColPos += (relativeIndex+1);
        //System.out.print(" ColPos: " + iColPos);
        if(iColPos == textCharM.length){
            //System.out.print(" Reset iCol\n");
            iColPos = 0;
            if (iSourceLineNr != sourceLineM.size()) {
                //System.out.println(iSourceLineNr+1 + " " + sourceLineM.get(iSourceLineNr));
                textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                iSourceLineNr++;
            } else {
                setNextToEmpty();
            }

        }
    }

    /**
     *<p>skipEmptyLines updates iColPos and iSourceLineNm whenever there are blank lines</p>
     */
    private void skipEmptyLines(){
        for(int i = iSourceLineNr-1; i<sourceLineM.size(); i++){
            if (sourceLineM.get(i).toCharArray().length == 0){
                textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                iSourceLineNr++;
                iColPos = 0;
            } else{
                break;
            }
        }
    }

    /**
     *<p>countDecimals is a helper method used to limit the regex use in the program in order to find the number of decimals within a string</p>
     * @param number is the String number to be analyzed
     * @return the number of decimals within the string
     */
    private int countDecimals(String number){
        int counter=0;
        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '.')
                counter++;
        }
        return counter;
    }

    /**
     *<p>setNextToEmpty is used when we have reached EOF</p>
     */
    private void setNextToEmpty(){
        trigger = false;
        nextToken.tokenStr = "";
        nextToken.primClassif = Classif.EOF;
        nextToken.subClassif = SubClassif.EMPTY;
        nextToken.iColPos = -1;
    }
}
