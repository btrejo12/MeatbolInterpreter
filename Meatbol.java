/*
  This is a simple driver for the first programming assignment.
  Command Arguments:
      java Meatbol arg1
             arg1 is the meatbol source file name.
  Output:
      Prints each token in a table.
  Notes:
      1. This creates a SymbolTable object which doesn't do anything
         for this first programming assignment.
      2. This uses the student's Scanner class to get each token from
         the input file.  It uses the getNext method until it returns
         an empty string.
      3. If the Scanner raises an exception, this driver prints 
         information about the exception and terminates.
      4. The token is printed using the Token::printToken() method.
 */
package meatbol;

public class Meatbol 
{
    public static void main(String[] args) 
    {
        // Create the SymbolTable
        SymbolTable symbolTable = new SymbolTable();
        
        try
        {
            Scanner scan = new Scanner(args[0], symbolTable);
            Parser parser = new Parser(scan);

            // Print a column heading 
            //System.out.printf("%-11s %-12s %s\n"
            //        , "primClassif"
            //        , "subClassif"
            //        , "tokenStr");
            //Scanner scan = new Scanner(args[0], symbolTable);
            //while (! scan.getNext().isEmpty())
            //{
            //    scan.currentToken.printToken();
            //}
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
