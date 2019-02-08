package meatbol;
public enum Classif 
{
    EMPTY,      // empty
    OPERAND,    // constants, identifier
    OPERATOR,   // + - * / < > = !
    SEPARATOR,  // ( ) , : ; [ ] 
    FUNCTION,   // TBD
    CONTROL,    // TBD
    EOF         // EOF encountered
}