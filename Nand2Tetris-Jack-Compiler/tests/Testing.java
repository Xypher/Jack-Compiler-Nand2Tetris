package tests;

import org.junit.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import src.Compilation;
import src.Tokenizer;

public class Testing {
    
    File infile_main, infile_square_game, infile_header;
    File out_file_main, out_file_square_game;
    static final String projectFile = "D:/University/Computer Organization and Design/Computer Sim/nand2tetris/projects/11/project/tests/testing_files/";

    @Before
    public void init(){

        infile_main = new File(projectFile + "Main.jack");
        out_file_main = new File(projectFile + "MainT.xml");
        infile_header = new File(projectFile + "header.jack");

        infile_square_game = new File(projectFile + "SquareGame.jack");
        out_file_square_game = new File(projectFile + "SquareGameT.xml");
    }

    @Test
    public void Tokenizer_Test_Main() throws FileNotFoundException{

        
        var correct_output =  new BufferedReader(new FileReader(out_file_main));
        Tokenizer tokenizer = new Tokenizer(infile_main);


        var correct_results = correct_output.lines().toArray(String[]::new);
        String[] results = tokenizer.all_tokens();
        
        
        
        assertTrue(Arrays.deepEquals(results, correct_results), "tokenizer passed main test");
    }

    @Test
    public void Tokenizer_Test_Square_Game() throws FileNotFoundException{


        var correct_output =  new BufferedReader(new FileReader(out_file_square_game));
        var tokenizer = new Tokenizer(infile_square_game);


        var correct_results = correct_output.lines().toArray(String[]::new);
        var results = tokenizer.all_tokens();

        assertTrue(Arrays.deepEquals(results, correct_results), "tokenizer passed square game test");

    }

    @Test
    public void test_header_Compilation() throws Exception{


        Compilation comp = new Compilation(infile_header, projectFile + "debug.txt");
        var actual = (String) comp.debug("classVarDec");

        final String expected ="{\n"+
        "static=5, field=2, arg=0, var=0\n" +
        "Identifier [catagory=static, idx=0, name=str, type=String] str,\n" +
        "Identifier [catagory=field, idx=0, name=square, type=Square] square,\n" +
        "Identifier [catagory=static, idx=2, name=chara, type=boolean] chara,\n" +
        "Identifier [catagory=static, idx=1, name=time, type=String] time,\n" +
        "Identifier [catagory=static, idx=3, name=boola, type=char] boola,\n" +
        "Identifier [catagory=field, idx=1, name=direction, type=int] direction,\n" +
        "Identifier [catagory=static, idx=4, name=coola, type=char] coola\n" +
        "}";
        
        assertEquals(expected, actual);
    }

    public static void main(String[] args) throws Exception {


        

        

    }
    

}

