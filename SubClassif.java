package meatbol;
public enum SubClassif 
{
    EMPTY,      // empty
    // OPERAND's subclassifications
    IDENTIFIER, // identifier 
    INTEGER,    // integer constant
    FLOAT,      // float constant
    BOOLEAN,    // boolean constant
    STRING,     // string constant
    DATE,       // date constant
    VOID,       // void
    // CONTROL's subclassifications
    FLOW,       // flow statement (e.g., if)
    END,        // end statement (e.g., endif)
    DECLARE,    // declare statement (e.g., Int)
    // FUNCTION's subclassfications
    BUILTIN,    // builtin function (e.g., print)
    USER,        // user-defined function
    // DEBUGS' subclassification
    DEBUGTYPE,
    // OPERATOR subclassification
    UNARY
}