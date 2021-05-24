package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class Tokenizer{


    public static enum TYPE {

        IDENTIFIER,
        KEYWORD,
        SYMBOL,
        INT_CONST,
        STRING_CONST,
        NONE,
        COMMIT,
        ERROR
    }

    //a set of all of jack's keywords to be used to identify the  type of a current token is a keyword
    private static HashSet<String> keywords = Stream.of(
        "class"
        ,"constructor"
        ,"function"
        ,"method"
        ,"field"
        ,"static"
        ,"var"
        ,"int",
        "char",
        "boolean",
        "void",
        "true",
        "false",
        "null",
        "this",
        "let",
        "do",
        "if",
        "else",
        "while",
        "return").collect(Collectors.toCollection(HashSet::new));

    
    //a set of all of jack's symbols to be used to identify the  type of a current token is a symbol
    private static HashSet<String> symbols = Stream.of(
        "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|",
        "<", ">", "=", "~" 
    ).collect(Collectors.toCollection(HashSet::new));


    //regular expressions to be used to tokenize the input file
    //and to be used in identifing the type of the token
    private static final String 
    MULTI_LINE_COMMENT = "((?s)/\\*.*?\\*/)",
    SINGLE_LINE_COMMENT = "(//([^\n]*))",
    STRING_CONST = "(\"[^\n]*?\")",
    IDENTIFIER = "([a-zA-Z_]([a-zA-Z0-9_]*))",
    INT_CONST = "([0-9]+)",
    SYMBOL = "([^a-zA-Z0-9_\\s])";

    
    //private helper method to be used for tokenizing
    //the input file
    //@PARAM buffer: a StringBuffer with all of the input file's content
    private void tokenize(String content){

        //constructing regular expression to tokenize the file
        String regex =  STRING_CONST + "|"
                        + IDENTIFIER + "|"
                        + INT_CONST + "|"
                        + SYMBOL;

        //compile the regex and initializing matcher
        this.matcher = Pattern.compile(regex).matcher(content);
    }

    
    private String currentToken;
    private Matcher matcher;

    public Tokenizer(File file) throws FileNotFoundException{
        
        //initialize a BufferedReader as a fast input method
        //to read all of the input file content and put them in a buffer
        var reader = new BufferedReader(new FileReader(file));
        var content = new StringBuffer();
        
        //read all of the input file content
        reader
        .lines()
        .parallel()
        .forEach(line -> content.append(line + '\n'));

        
        tokenize(
            content
                .toString() // turn to string
                .replaceAll(MULTI_LINE_COMMENT + "|" + SINGLE_LINE_COMMENT, " ") // remove comments
                .trim() // remove white spaces at the start and at end
            );

        matcher.find();
        if(matcher.group() != null)
            currentToken = matcher.group();


        try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    public String[] all_tokens(){

        var local_matcher = matcher.reset();


        ArrayList<String> tokens = new ArrayList<>();
        tokens.add("<tokens>");
        while(local_matcher.find())
            tokens.add(token_xml(local_matcher.group()));

        tokens.add("</tokens>");

        return tokens.toArray(String[]::new);
    }

    //debuging method
    private String token_xml(String token){

        
        var type = tokenType(token);
        
        //remove " from a string constant
        if(type.equals("stringConstant"))
            token = token.replaceAll("\"", "");

        else if(token.equals("<"))
            token = "&lt;";

        else if(token.equals(">"))
            token = "&gt;";


        else if(token.equals("&"))
            token = "&amp;";
        

        
        return String.format("<%s> %s </%s>", type, token, type);
    }


    
    
    public void advance(){

        matcher.find();
        currentToken = matcher.group();
    }

    //make sure that idx + 1 still withen the tokens range
    public boolean hasMoreTokens(){
        return !matcher.hitEnd();
    }

    //return current token
    public String token(){return this.currentToken;}

    //return type of the current token by matching against regex
    //and matching against sets of keywords and symbols
    public String tokenType(){

        if(currentToken == null || currentToken == "") return "NONE"; 

        if(currentToken.matches(INT_CONST)) return "integerConstant";
        if(currentToken.matches(STRING_CONST)) return "stringConstant";
      

        if(keywords.contains(currentToken)) return "keyword";

        if(symbols.contains(currentToken)) return "symbol";
        
        if(currentToken.matches(IDENTIFIER)) return "identifier";

        //if it doesn't match any of the above then it is an error type
        return "error";
    }

    public String tokenType(String token){

        if(token == null || token == "") return "NONE"; 

        if(token.matches(INT_CONST)) return "integerConstant";
        if(token.matches(STRING_CONST)) return "stringConstant";
      

        if(keywords.contains(token)) return "keyword";

        if(symbols.contains(token)) return "symbol";
        
        if(token.matches(IDENTIFIER)) return "identifier";

        //if it doesn't match any of the above then it is an error type
        return "error";
    }

    public Tokenizer.TYPE type(String token){

        if(token == null || token == "") return TYPE.NONE; 

        if(token.matches(INT_CONST)) return TYPE.INT_CONST;
        if(token.matches(STRING_CONST)) return TYPE.STRING_CONST;
      

        if(keywords.contains(token)) return TYPE.KEYWORD;

        if(symbols.contains(token)) return TYPE.SYMBOL;
        
        if(token.matches(IDENTIFIER)) return TYPE.IDENTIFIER;

        //if it doesn't match any of the above then it is an error type
        return TYPE.ERROR;
    }

}