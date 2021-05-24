package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static src.Tokenizer.TYPE;



public class Compilation {
    
    private Tokenizer tokenizer;
    private SymbolTable classTable, subRoutineTable;
    private VMWriter writer;
    private String className;
    private int labelCount;

    public Compilation(File file, String outputFilePath) throws IOException{

        try {
			tokenizer = new Tokenizer(file);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
    
        writer = new VMWriter(new File(outputFilePath));
        classTable = new SymbolTable();
        labelCount = 0;
    }

    //compile the class and close the writer
    public void compile() throws Exception{


        compileClass();

        try {
            writer.close();
		} catch (IOException e) {
			e.printStackTrace();
        }
        
    }




    public void compileClass() throws Exception{

        
        eat("class", "expected a class decleration");

        expect(TYPE.IDENTIFIER, "identifier");
        className = eat();

        eat("{", "expected a '{'");



        while(token().equals("field") ||
        token().equals("static") ){

            //static | field
            var catagory = token();
            eat(token(), "");

            takeVars(catagory, this.classTable);
        }


        
        while(token().equals("constructor") 
        || token().equals("function")
        || token().equals("method") )
            subRoutiene();
        
        
        
        //final token of a class should be a closing parentheses
        expect("expected a '}'", "}");

    }




    private void parameterList() throws Exception{


        eat("(", "expected a '('");
        
        //if no arguments for this subroutine
        if(token().equals(")")){
            eat(")", "");
            return;
        }


        String type, name;

        //taking the type of the argument 
        expect("expected a type decleration", TYPE.IDENTIFIER, "int", "boolean", "char");
        type = token();
        eat(token(), "");

        //take name of the argument
        expect(TYPE.IDENTIFIER, "identifier");
        name = token();
        eat(token(), "");

        subRoutineTable.add_arg(name, type);

        expect("expected a ',' or a ')'", ")", ",");

        while(token().equals(",")){

            eat(",", "");
            
            //taking the type of the argument 
            expect("expected a type decleration", 
            TYPE.IDENTIFIER, "int", "boolean", "char");
            type = eat();
 
            //take name of the argument
            expect(TYPE.IDENTIFIER, "identifier");
            name = token();
            eat(token(), "");

            subRoutineTable.add_arg(name, type);
        }

        eat(")", "expected a ')'");

    }



    private int varDec() throws Exception{

        int local_count = 0;
    
        while(token().equals("var")){

            eat("var", "");
            local_count += takeVars("var", this.subRoutineTable);
        }

        return local_count;
    }



    public void compileWhileStatement(String subRoutineName,
     String subRoutineType, String subRoutineReturnType) 
     throws Exception{

        //eat while statement
        eat();

        String label_1 = mkLabel(subRoutineName);
        String label_2 = mkLabel(subRoutineName);
        
        writer.writeLabel(label_1);
        eat("(", "expected a '('");
        compileExpression(")");
        eat(")", "expected a )");

        writer.writeArithmetic("not");
        
        writer.writeIfGoTo(label_2);

        eat("{", "expected a '{'");
        compileStatements(subRoutineName, subRoutineType, subRoutineReturnType);
        eat("}", "expected a '}'");

        writer.writeGoTo(label_1);
        writer.writeLabel(label_2);

    }

    public void compileIfStatement(String subRoutineName,
    String subRoutineType, String subRoutineReturnType)
     throws Exception{
        
        //eat the if keyword
        eat();

        eat("(", "expected a '('");
        compileExpression(")");
        eat(")", "expected a )");
        
        String label_1 = mkLabel(subRoutineName);
        writer.writeArithmetic("not");
        writer.writeIfGoTo(label_1);
        
        eat("{", "expected a '{'");
        compileStatements(subRoutineName, subRoutineType, subRoutineReturnType);
        eat("}", "expected a '}'");


        if(!token().equals("else")){  
            writer.writeLabel(label_1);
            return;
        }
        

        String label_2 = mkLabel(subRoutineName);
        writer.writeGoTo(label_2);

        //eat the else keyword
        eat("else", "expected else");

        writer.writeLabel(label_1);

        eat("{", "expected a '{'");
        compileStatements(subRoutineName, subRoutineType, subRoutineReturnType);
        eat("}", "expected a '}'");
        writer.writeLabel(label_2);
    }


    public void compileLetStatement(String subRoutineName,
    String subRoutineType, String subRoutineReturnType)
     throws Exception{

        eat("let", "expected a let");
        expect(TYPE.IDENTIFIER, "identifier");
        var varName = eat();

        SymbolTable.Identifier ident;
        if(subRoutineTable.exists(varName))
            ident = subRoutineTable.look_up(varName);
        else if(classTable.exists(varName))
            ident = classTable.look_up(varName);

        else {
            throw new 
            Exception(String.format("identifier %s is undefined", varName));
        }

        if(token().equals("[")){//indexing an array

            //push array base address
            if(ident.catagory.equals("var"))
                writer.writePush("local", ident.idx);

            else if(ident.catagory.equals("arg"))
                writer.writePush("argument",  ident.idx);

            else if(ident.catagory.equals("static"))
                writer.writePush("static", ident.idx);

            else if(ident.catagory.equals("field"))
                writer.writePush("this", ident.idx);
                
            //compile expression
            eat("[", "expected '['");
            compileExpression("]");
            eat("]", "expected a ']'");

            writer.writeArithmetic("add");//arr + [expression]
            writer.writePop("temp", 0);//temp[0] = array + [expression]

            eat("=", "expected a '='");//eat =
            compileExpression(";");// = expression;

            writer.writePush("temp", 0);//stack <- temp[0]
            writer.writePop("pointer", 1);//that = arr + [expression]
            writer.writePop("that", 0);// arr[expression] = expression
        }

        else{//simple variable assignment

            eat("=", "expected a '='");
            compileExpression(";");//compile the expression
            
            //assign the expression to the variable
            if(ident.catagory.equals("var"))
                writer.writePop("local", ident.idx);

            else if(ident.catagory.equals("arg"))
                writer.writePop("argument",  ident.idx);

            else if(ident.catagory.equals("static"))
                writer.writePop("static", ident.idx);

            else if(ident.catagory.equals("field"))
                writer.writePop("this", ident.idx);
        }

        eat(";", "expected a ';'");
    }




    public void compileDoStatement(String subRoutineName,
     String subRoutineReturnType, String subRoutineType) 
     throws Exception{

        eat("do", "expected a do");
        compileExpression(";"); // compile the do expression
        eat(";", "expected a ';'");

        //dispose of the expression result
        writer.writePop("temp", 2);
    }



    public void compileReturnStatement(String subRoutineReturnType)throws Exception{

        eat("return", "expected a return");

        if(subRoutineReturnType.equals("void")){
            //return a numeric 0 for void subroutines as stated in the standered
            writer.writePush("constant", 0);
        }

        else{
            //evaluate expression and return it
            compileExpression(";");
        }

        eat(";", "expected a ';'");
        writer.writeReturn();
    }


    public void compileStatements(String subRoutineName,
    String subRoutineType, String subRoutineReturnType)
     throws Exception{


        while(!token().equals("}")){
            switch(tokenizer.token()){

                case "let":
                    compileLetStatement(subRoutineName, subRoutineType, subRoutineReturnType);;
                    break;
                    
                case "do":
                    compileDoStatement(subRoutineName, subRoutineReturnType, subRoutineType);;
                    break;

                case "while":
                    compileWhileStatement(subRoutineName, subRoutineType, subRoutineReturnType);;
                    break;

                case "if":
                    compileIfStatement(subRoutineName, subRoutineType, subRoutineReturnType);
                    break;

                case "return":
                    compileReturnStatement(subRoutineReturnType);
                    break;

                default:
                    throw new Exception("expected a statement");
                }
        }


    }


    
    
   
    
    private void subRoutiene() throws Exception{

        String routineType, returnType, name;
        subRoutineTable = new SymbolTable();

        //takeing subroutine type
        expect("expected a subroutine type decleration", 
        "function", "constructor", "method");
        routineType = eat();

        //taking return type
        expect("expected a return type decleration", 
        TYPE.IDENTIFIER, "void", "int", "boolean", "char");
        returnType = eat();
        
        expect(TYPE.IDENTIFIER, "identifier");
        name = eat();


        if(routineType.equals("method"))
            subRoutineTable.add_arg("this", className);

        parameterList();
        eat("{", "expected a '{'");
        int local_count = varDec();

        writer.writeFunction(className + "." + name, local_count);
        subRoutineBootstrap(routineType);

        compileStatements(name, routineType, returnType);
        
        eat("}", "expected a '}'");
    }


    public void subRoutineBootstrap(String subRoutineType){

        if(subRoutineType.equals("method")){
            //anchor this to argument 0
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        }

        else if(subRoutineType.equals("constructor")){
            //reserve enough memory for the object on the heap 
            
            writer.writePush("constant", classTable.getField_count());
            writer.writeCall("Memory.alloc", 1);

            //anchor this to the address returned my Memory.alloc
            writer.writePop("pointer", 0);
        }

    }


    private int expressionList() throws Exception{

        int argCount = 0;
        eat("(", "");

        if(token().equals(")")){//empty expression list
            eat();
            return argCount;
        }
        compileExpression(",", ")");
        argCount++;

        while(tokenizer.token().equals(",")){

            eat(",", "");
            compileExpression(",", ")");
            argCount++;
        }

        eat(")", "expected a ')'");

        return argCount;
    }


    //method used to compile expressions
    public void compileExpression(String... terminals) throws Exception{

        
        //term encountered is a flag to know if we encountered a term before the current term
        //to distinguish beteween binray op - and unary op -
        boolean term_encountered = false;

        HashSet<String> operators = Stream.of(
            "+", "-", "*", "/", "&", "|", "<", ">", "="
        ).collect(Collectors.toCollection(HashSet::new));

        while(!Arrays.stream(terminals)
        .anyMatch((el -> el.equals(token())))){

            if(token().equals("~") || (token().equals("-") && ! term_encountered))
                compileTerm();

            else if(operators.contains(token())){

                var operator = eat();
                compileTerm();
                compileArithmetic(operator);

            }

            else{
                compileTerm();
            }

            term_encountered = true;
        }



    }

    public void compileArithmetic(String operator) {

        switch(operator){
            case "+":
                writer.writeArithmetic("add");
                break;

            case "-":
                writer.writeArithmetic("sub");
                break;

            case "*":
                writer.writeCall("Math.multiply", 2);
                break;

            case "/":
                writer.writeCall("Math.divide", 2);
                break;

            case "&":
                writer.writeArithmetic("and");
                break;

            case "|":
                writer.writeArithmetic("or");
                break;

            case "<":
                writer.writeArithmetic("lt");
                break;

            case ">":
                writer.writeArithmetic("gt");
                break;

            case "=":
                writer.writeArithmetic("eq");
        }
    }



    private int takeVars(String catagory, SymbolTable table) throws Exception{

        String name, type;
        int varCount = 0;

        //int | char | boolean | className
        expect("expected a type declaration",
        TYPE.IDENTIFIER, "int", "char", "boolean");
        type = eat();

        expect(TYPE.IDENTIFIER, "identifier");
        name = eat();

        table.add(name, type, catagory);
        varCount++;

        expect("expected a ',' or a ';'", ",", ";");

        while(token().equals(",")){

            eat(",", "");

            expect(TYPE.IDENTIFIER, "identifier");
            name = eat();

            table.add(name, type, catagory);
            varCount++;
        }

        eat(";", "expected a ;");

        return varCount;
    }

    private void compileTerm() throws Exception{

        if(token().equals("true")){
            writer.writePush("constant", 1);//writing a one
            writer.writeArithmetic("neg");//generating true by negating 1
            eat();//eat true
        }

        else if(token().equals("false") || token().equals("null")){
            writer.writePush("constant", 0);//null = false = 0
            eat();//eat false|null
        }

        else if(token().equals("this")){
            writer.writePush("pointer", 0); // pointer should be anchored to this
            eat();//eat this
        }

        else if(tokenizer.type(token()) == TYPE.INT_CONST){
            writer.writePush("constant", 
                Integer.parseInt(eat()));
        }

        else if(tokenizer.type(token()) == TYPE.STRING_CONST){
            HandleStringConstant();
        }
        
        //compile a term perceded by an unary operator
        else if(token().equals("-") || token().equals("~")){

            var aritmetic = eat().equals("-") ? "neg" : "not";//generate the arithmetic operation
            compileTerm();//compiling the term
            writer.writeArithmetic(aritmetic);//applying the required unary op
        }

        //compile (expression)
        else if(token().equals("(")){

            eat("(", "expected a '('");
            compileExpression(")");
            eat(")", "expected a  ')' ");
        }

        else if (tokenizer.type(token()) == TYPE.IDENTIFIER )
            compileVarAccess();

    }

    private void HandleStringConstant(){

        var strConst = eat().replace("\"", "");

            //constructing a new string from literal
        writer.writePush("constant", strConst.length());
        writer.writeCall("String.new", 1);
        writer.writePop("temp", 1);//save the literal string

        //append the characters of a literal
        for(int i = 0; i < strConst.length(); ++i){
            writer.writePush("temp", 1);// push the literal string object
            writer.writePush("constant", (int) strConst.charAt(i));// push the char code number
            writer.writeCall("String.appendChar", 2);// call appendChar
            writer.writePop("temp", 2); // dispose of the return of appendChar
        }
    
        writer.writePush("temp", 1); // pushing the string object onto the stack
    }


    private void compileVarAccess() throws Exception{

        var varName = eat();

        switch(token()){

            //method call
            case "(":
                //push this to stack
                writer.writePush("pointer", 0);
                int argCount = 1 + expressionList();
                writer.writeCall(className + "." + varName, argCount);
                break;

            //array access
            case "[":
                handleArrayAccess(varName);
                break;

            //subroutine call via (className|varName)
            case ".":
                eat(".", "");
                handleSubRoutineCall(varName);
                break;

            default://where varName correspond to a simple variable

                SymbolTable.Identifier ident;
                if(subRoutineTable.exists(varName))
                    ident = subRoutineTable.look_up(varName);
                else if(classTable.exists(varName))
                    ident = classTable.look_up(varName);
                else {
                    throw new 
                    Exception(String.format("identifier %s is undefined", varName));
                }

                accessVar(ident);
                break;

        }
    }


    private void handleArrayAccess(String arrName) throws Exception{


        eat("[", "expected a '['");
        compileExpression("]");
        eat("]", "expected a ']'");

        SymbolTable.Identifier arr;
        if(subRoutineTable.exists(arrName))
            arr = subRoutineTable.look_up(arrName);
        else if(classTable.exists(arrName))
            arr = classTable.look_up(arrName);

        else {
            throw new 
            Exception(String.format("identifier %s is undefined", arrName));
        }

        if(!arr.type.equals("Array")){
            throw new Exception(String.format("array access of none array type"));
        }

        //push the array's base address to the stack
        accessVar(arr);
        writer.writeArithmetic("add");// arr + expression
        writer.writePop("pointer", 1);//that = arr + expression 
        writer.writePush("that", 0);//push arr[expression]
    }

    private void accessVar(SymbolTable.Identifier ident){
        
        if(ident.catagory.equals("var")){
            writer.writePush("local", ident.idx);
        }
        else if(ident.catagory.equals("static")){
            writer.writePush("static", ident.idx);
        }

        else if(ident.catagory.equals("arg")){
            writer.writePush("argument", ident.idx);
        }

        else {
            writer.writePush("this", ident.idx);
        }
    }


    private void handleSubRoutineCall(String name) throws Exception{

        String routineName = eat();
        SymbolTable.Identifier variable;
        if(subRoutineTable.exists(name))
            variable = subRoutineTable.look_up(name);
        else if(classTable.exists(name))
            variable = classTable.look_up(name);

        else { // case where this call correspond to a function

            var argCount = expressionList();//push arguments to the stack
            writer.writeCall(name + "." + routineName, argCount);// call the function
            return;
        }

        //we continoue here where it is the case that this is a method call
        accessVar(variable); // push the object to arg 0
        var argCount = expressionList() + 1; // push rest of the arguments
        writer.writeCall(variable.type + "." + routineName, argCount); // call the method
    }


    public void advance(){
        tokenizer.advance();
    }

    public boolean hasMore(){
        return tokenizer.hasMoreTokens();
    }

    public String token(){
        return tokenizer.token();
    }

    private String eat(String tkn, String msg) throws Exception{
        
        if(!tkn.equals(token()))
            throw new Exception(msg);

        advance();
        return tkn;
    }

    private String eat(){

        String tkn = token();
        advance();
        return tkn;
    }

    public void expect(Tokenizer.TYPE type, String exp) throws Exception{

        if(tokenizer.type(token()) != type)
            throw new Exception("expected an " + exp);

    }

    public void expect(String msg, String... exps) throws Exception{

        for(var exp: exps){
            if(exp.equals(token()))
                return;
        }

        throw new Exception(msg);
    }

    
    public void expect(String msg, TYPE type, String... exps) throws Exception{

        for(var exp: exps){
            if(exp.equals(token()))
                return;
        }

        if(tokenizer.type(token()) == type) 
            return;
        throw new Exception(msg);
    }

    public String mkLabel(String subRoutineName){
        return String.format("%s.%s.lbl.%d", className, subRoutineName, labelCount++);
    }


    public Object debug(String command) throws Exception{

        switch(command){
            
            case "classVarDec":
                debugCompileHeader();
                return classTable.toString();

            case "tokens":
                return this.tokenizer.all_tokens();
            

            default:
                return "";
        }        
    }

    
     
    public void debugCompileHeader() throws Exception{

        
        eat("class", "expected a class decleration");

        expect(TYPE.IDENTIFIER, "identifier");
        className = eat();

        eat("{", "expected a '{'");



        while(token().equals("field") ||
        token().equals("static") ){

            //static | field
            var catagory = token();
            eat(token(), "");

            takeVars(catagory, this.classTable);
        }

        expect("expected a '}'", "}");
    }
}