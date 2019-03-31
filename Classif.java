package meatbol;
public enum Classif 
{
    EMPTY,      // empty
    OPERAND,    // constants, identifier
    OPERATOR,   // + - * / < > = !
    SEPARATOR,  // ( ) , : ; [ ] 
    FUNCTION,   // TBD
    CONTROL,    // TBD
    DEBUG,      // used for debug commands
    EOF         // EOF encountered
}