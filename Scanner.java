package meatbol;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Scanner{

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
    private final static String operators = "+-*/<>=!#^";
    private final static String digits = "0123456789";
    private final static String separators = "(),:;[]";
    private StorageManager sManager;
    private Numeric numeric;

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
        this.numeric = new Numeric();
        this.sManager = new StorageManager();
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

        System.out.println("1 " + sourceLineM.get(0));
        try{
            getNext();
        }catch(Exception e){
            e.printStackTrace();
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

                if(index >= textCharM.length){
                    String variableName = sourceLineM.get(iSourceLineNr - 1).substring(iColPos, (iColPos + i));
                    assignNextToken(variableName, Classif.OPERAND, SubClassif.IDENTIFIER);
                    incrementColumnPosition(i - 1);
                    break;
                }
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
                        } //This character and the next are the start of a comment, go to the next line
                        else if (currentChar == '/' && textCharM.length > index && textCharM[index+1] == '/'){
                            incrementColumnPosition(textCharM.length);
                            i=0;
                            continue;
                        }//This is a operator token, assign it and update iCol
                        else if (operators.indexOf(currentChar) >= 0) {
	                        checkOperator(index);
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
                        // Digits
                        if (digits.indexOf(variableName.charAt(0)) >= 0) {
                            SubClassif sClassif;
                            try {
                                sClassif = numeric.checkNumType(variableName);
                            } catch (Exception e) {
                                throw new Exception("Line " + iSourceLineNr + ": Invalid number format, File: " + sourceFileNm);
                            }
                            assignNextToken(variableName, Classif.OPERAND, sClassif);
                            incrementColumnPosition(i - 1);
                            break;

                        }

                        //variable identifier
                        else {
                            STEntry sEntry = symbolTable.getSymbol(variableName);
                            Classif primary;
                            SubClassif secondary;

                            //Variable doesn't exist in SymbolTable
                            if (sEntry == null){
                                if (currentToken.subClassif != SubClassif.DECLARE){
                                    //TODO: Add an actual error message
                                    throw new Exception("");
                                } else {
                                    STIdentifier newEntry = new STIdentifier(variableName, Classif.OPERAND, SubClassif.IDENTIFIER);
                                    primary = newEntry.primClassif;
                                    secondary = newEntry.dclType;
                                }
                            } else {
                                primary = sEntry.primClassif;
                                //Secondary classification is dependant on type of STEntry
                                if (sEntry instanceof STControl) {
                                    STControl sControl = (STControl) sEntry;
                                    secondary = sControl.subClassif;
                                } else if (sEntry instanceof STFunction) {
                                    STFunction sFunc = (STFunction) sEntry;
                                    secondary = sFunc.definedBy;
                                } else if (sEntry instanceof STIdentifier) {
                                    STIdentifier stIdentifier = (STIdentifier) sEntry;
                                    secondary = stIdentifier.dclType;
                                } else { //Other instance should have been caught
                                    System.err.println("Woah woah woah whats going on here, variable: " + variableName);
                                    secondary = SubClassif.EMPTY;
                                }
                            }
                            assignNextToken(variableName, primary, secondary);
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
        nextToken.iSourceLineNr = iSourceLineNr;
    }

    /**
     *<p>shiftTokens is a helper method user to move the nextToken into the currentToken position.</p>
     */
    private void shiftTokens(){
        currentToken.primClassif = nextToken.primClassif;
        currentToken.subClassif = nextToken.subClassif;
        currentToken.tokenStr = nextToken.tokenStr;
        currentToken.iColPos = nextToken.iColPos;
        currentToken.iSourceLineNr = nextToken.iSourceLineNr;
    }

    /**
     *<p>FSindStringLiteral is a helper method used to find an entire string literal and updates the position in file.</p>
     * @param endingDelimiter is the character that ends the string literal sequence
     * @param startingIndex is the index where the string literal starts
     * @throws Exception is raised when the String Literal is not closed on the same line
     */
    private void findStringLiteral(char endingDelimiter, int startingIndex) throws Exception{
        for(int i=startingIndex+1; i<textCharM.length; i++){
            char currentChar = textCharM[i];

            //Ending delimiter found and it is not escaped
            if(currentChar == endingDelimiter && textCharM[i-1] != '\\'){
                String variableName = sourceLineM.get(iSourceLineNr-1).substring(startingIndex+1, i);
                String escapedName = convertEscaped(variableName);
                if(escapedName == "")
                    throw new Exception("Line " + iSourceLineNr + ": Unrecognized escape character, File: " + sourceFileNm);
                assignNextToken(escapedName, Classif.OPERAND, SubClassif.STRING);
                incrementColumnPosition(i-startingIndex);
                return;
            }
        }
        throw new Exception("Line " + iSourceLineNr + ": String literal must terminate on same line, File: " + sourceFileNm);
    }

    /**
     * <p>This function correctly converts escaped characters within a String literal.</p>
     * @param variableName is the String to be converted
     * @return the converted string
     */
    private String convertEscaped(String variableName){
        char[] escapedArr = new char[variableName.length()];
        char[] variableArr = variableName.toCharArray();
        int index=0;
            for(int c = 0;c<variableArr.length;c++){
                if(variableArr[c] == '\\'){
                    c++;
                    char next = variableArr[c];
                    if(next == 'n')
                        escapedArr[index] = '\n';
                    else if(next == '\\')
                        escapedArr[index] = '\\';
                    else if(next == '\'')
                        escapedArr[index] = '\'';
                    else if(next == '\"')
                        escapedArr[index] = '\"';
                    else if(next == 't')
                        escapedArr[index] = '\t';
                    else if(next == 'a')
                        escapedArr[index] = '\007';
                    else
                        return "";
                }
                else{
                    escapedArr[index] = variableArr[c];
                }
                index++;
            }
        return String.valueOf(escapedArr);

    }

    /**
     *<p>IncrementColumnPosition updates the iColPos whenever we are moving between tokens.</p>
     * @param relativeIndex is the relative index within the for loop which must be added to iColPos
     */
    private void incrementColumnPosition(int relativeIndex){
        iColPos += (relativeIndex+1);
        //System.out.print(" ColPos: " + iColPos);
        if(iColPos >= textCharM.length){
            //System.out.print(" Reset iCol\n");
            iColPos = 0;
            if (iSourceLineNr != sourceLineM.size()) {
                //System.out.println(iSourceLineNr+1 + " " + sourceLineM.get(iSourceLineNr));
                textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                //iSourceLineNr++;
                printNextLine();
            } else {
                setNextToEmpty();
            }

        }
    }

    /**
     *<p>SkipEmptyLines updates iColPos and iSourceLineNm whenever there are blank lines</p>
     */
    private void skipEmptyLines(){
        for(int i = iSourceLineNr-1; i<sourceLineM.size(); i++){
            if (sourceLineM.get(i).toCharArray().length == 0){
                textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                iSourceLineNr++;
                //System.out.println(sourceLineM.get(iSourceLineNr-1));
                iColPos = 0;
            } else{
                break;
            }
        }
    }

    /**
     * @deprecated Use Numeric checkNumType instead.
     *
     *
     *<p>countDecimals is a helper method used to limit the regex use in the program in order to find the number of decimals within a string</p>
     * @param number is the String number to be analyzed
     * @return the number of decimals within the string
     */
    @Deprecated //ok Wang
    private int countDecimals(String number){
        int counter=0;
        for(int i = 0; i < number.length(); i++){
            if(number.charAt(i) == '.')
                counter++;
        }
        return counter;
    }

    /**
     *<p>SetNextToEmpty is used when we have reached EOF</p>
     */
    private void setNextToEmpty(){
        trigger = false;
        nextToken.tokenStr = "";
        nextToken.primClassif = Classif.EOF;
        nextToken.subClassif = SubClassif.EMPTY;
        nextToken.iColPos = -1;
    }

    /**
     * <p>PrintNextLine is responsible for printing the current line when Scanner increments to the next line.</p>
     */
    private void printNextLine(){
        iSourceLineNr++;
        skipEmptyLines();
        System.out.println(iSourceLineNr + " " + sourceLineM.get(iSourceLineNr-1));
    }

    /**
     * <p>CheckOperator checks to see if there are two subsequent operators that can be combined. This function updates iCol
     * accordingly and populates nextToken.
     * @param index the index of the first operator</p>
     */
    private void checkOperator(int index){
        int i = index;
        char op = textCharM[index];
        i++;
        if(i>=textCharM.length){
            textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
            printNextLine();
            i = 0;
        }
        if(op == '+' || op == '-' || op == '*' || op == '/' || op == '#' || op == '^'){
            assignNextToken(Character.toString(op), Classif.OPERATOR, SubClassif.EMPTY);
            iColPos=i;
            return;
        } else{
            while(true){
                char next = textCharM[i];
                if(Character.isWhitespace(next)){
                    i++;
                    if(i>=textCharM.length){
                        textCharM = sourceLineM.get(iSourceLineNr).toCharArray();
                        printNextLine();
                        i=0;
                    }
                } else if(next == '=' || next == '<' || next == '>' || next == '!'){
                    String multi = Character.toString(op) + Character.toString(next);
                    assignNextToken(multi, Classif.OPERATOR, SubClassif.EMPTY);
                    iColPos=i+1;
                    return;
                } else{
                    assignNextToken(Character.toString(op), Classif.OPERATOR, SubClassif.EMPTY);
                    iColPos = i;
                    return;
                }
            }
        }
    }
}



