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

    public boolean bPrintLines = false;
    public boolean bShowToken = false;

    public boolean trigger = true;
    private final static String delimiters = "\t;:()\'\"=!<>+-*/[]#,^\n";
    private final static String operators = "+-*/<>=!#^";
    private final static String digits = "0123456789";
    private final static String separators = "(),:;[]";
    public StorageManager sManager;
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
        //this.sManager = new StorageManager();
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
        iSourceLineNr = 0;

        printNextLine();
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
            //System.out.println("Current Token: " + this.currentToken.tokenStr);
            skipEmptyLines();

            for (int i = 0; i <= textCharM.length; i++) {
                int index = iColPos + i;

                if (index >= textCharM.length) {
                    String variableName = sourceLineM.get(iSourceLineNr - 1).substring(iColPos, (iColPos + i));
                    attemptNewSymbolSave(variableName);
                    //System.out.println(variableName);
                    //assignNextToken(variableName, Classif.OPERAND, SubClassif.IDENTIFIER);
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
                            try {
                                incrementColumnPosition(i);
                            } catch (ParserException pe) {
                                return currentToken.tokenStr;
                            }
                            i = i - 1;
                            continue;
                        } //This character and the next are the start of a comment, go to the next line
                        else if (currentChar == '/' && textCharM.length > index && textCharM[index + 1] == '/') {
                            incrementColumnPosition(textCharM.length);
                            i = -1;
                            continue;
                        }//This is a operator token, assign it and update iCol
                        else if (operators.indexOf(currentChar) >= 0) {
                            checkOperator(index);
                            break;
                        } //This is a separator token, assign it and update iCol
                        else if (separators.indexOf(currentChar) >= 0) {
                            assignNextToken(Character.toString(currentChar), Classif.SEPARATOR, SubClassif.EMPTY);
                            try {
                                incrementColumnPosition(i);
                            } catch (Exception e) {
                                nextToken.tokenStr = "";
                                assignNextToken("", Classif.EOF, SubClassif.EMPTY);
                                return currentToken.tokenStr;
                            }
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
                        attemptNewSymbolSave(variableName);
                        incrementColumnPosition(i - 1);
                        break;
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

        if(bShowToken){
            nextToken.printToken();
        }
    }

    /**
     *<p>shiftTokens is a helper method user to move the nextToken into the currentToken position.</p>
     */
    private void shiftTokens(){
        //System.out.println("Shift tokens from " + currentToken.tokenStr + " to " + nextToken.tokenStr);
        currentToken = new Token();
        currentToken.primClassif = nextToken.primClassif;
        currentToken.subClassif = nextToken.subClassif;
        currentToken.tokenStr = nextToken.tokenStr;
        currentToken.iColPos = nextToken.iColPos;
        currentToken.iSourceLineNr = nextToken.iSourceLineNr;
        nextToken = new Token();
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
            if(index < variableArr.length) {
                char[] newArr = new char[index];
                for(int i=0;i<index;i++){
                    newArr[i] = escapedArr[i];
                }
                return String.valueOf(newArr);
            }
        return String.valueOf(escapedArr);

    }

    /**
     *<p>IncrementColumnPosition updates the iColPos whenever we are moving between tokens.</p>
     * @param relativeIndex is the relative index within the for loop which must be added to iColPos
     */
    private void incrementColumnPosition(int relativeIndex) throws ParserException{
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
                throw new ParserException(iSourceLineNr,"End of File",sourceFileNm);
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
        //System.out.println("********TRIGGER**********");
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
        if (bPrintLines) {
            System.out.println(iSourceLineNr + " " + sourceLineM.get(iSourceLineNr - 1));
        }
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
            if(op == '-' && currentToken.primClassif != Classif.OPERAND) {
                assignNextToken(Character.toString(op), Classif.OPERATOR, SubClassif.UNARY);
            } else {
                assignNextToken(Character.toString(op), Classif.OPERATOR, SubClassif.EMPTY);
            }
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

    /**
     * <p>setPosition is used to go back to a specific position when looping.
     * @param lineNumber is the named line number of the start of the loop.
     *            The method will use it to derive the actual line needed for use
     * @param columnNumber is the first position of the first token to be read.
     *                     A position with whitespace is acceptable. The whitespace
     *                     will be ignored and read to the first token when getNext
     *                     is called</p>
     */
    public void setPosition(int lineNumber, int columnNumber) throws Exception{
        textCharM = sourceLineM.get(lineNumber-1).toCharArray();
        iSourceLineNr = lineNumber;
        iColPos = columnNumber;
        nextToken = new Token();
        currentToken = new Token();
        getNext();
        getNext();
    }

    /**
     * <p> getType is used to find the SubClassifier through the given string for use in creating
     * a resultValue
     * @param type is a string that is used to find the data type of the result value being tested
     * @return SubClassif
     * </p>
     */
    public SubClassif getType(String type) throws Exception {
        if(type.equals("Int"))
            return SubClassif.INTEGER;
        if(type.equals("Float"))
            return SubClassif.FLOAT;
        if(type.equals("Bool"))
            return SubClassif.BOOLEAN;
        if(type.equals("String"))
            return SubClassif.STRING;
        if(type.equals("Date"))
            return SubClassif.DATE;
        if(type.equals("for"))
            return SubClassif.FLOW;
        throw new Exception();

    }

    /**
     * <p>Attempts to save a new symbol into the Scanner's current SymbolTable. Will throw errors
     * if the symbol has already been instantiated, or if there are additional errors with the
     * symbol being saved </p>
     * @param varName - name of the variable being saved.
     * @throws Exception
     */
    private void attemptNewSymbolSave(String varName) throws Exception {
        STIdentifier newEntry = new STIdentifier(varName, Classif.OPERAND, SubClassif.IDENTIFIER);
        STEntry sEntry = symbolTable.getSymbol(varName);
        Classif primary = null;
        SubClassif secondary = null;

        //Variable doesn't exist in SymbolTable
        if (sEntry == null){
            if(currentToken.primClassif == Classif.DEBUG){
                if(varName.equals("on") || varName.equals("off")){
                    primary = Classif.DEBUG;
                    secondary = SubClassif.EMPTY;
                }
            } else if (currentToken.subClassif != SubClassif.DECLARE && currentToken.subClassif != SubClassif.FLOW){
                throw new Exception("Variable '" + varName + "' has not been initialized.");
            } else {
                // add symbol to the symbol table
                symbolTable.putSymbol(varName, newEntry);
                primary = Classif.OPERAND;
                secondary = SubClassif.IDENTIFIER;
                ResultValue rv = new ResultValue();

                //TODO: Change this for prg 4 ---------
                rv.structure = "primitive";
                //-------------------------------------

                rv.type = getType(currentToken.tokenStr);
                rv.value = null;

                try {
                    sManager.addVariable(varName, rv);
                } catch (Exception e) {
                    // Variable already exists
                    throw new Exception("Variable already exists in Storage Manager.");
                    //System.out.println(e.getMessage());
                }
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
                //System.err.println("Woah woah woah whats going on here, variable: " + varName);
                secondary = SubClassif.EMPTY;
            }
        }
        assignNextToken(varName, primary, secondary);
    }
}



