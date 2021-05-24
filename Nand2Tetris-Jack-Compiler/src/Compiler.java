package src;

import java.io.File;
import java.io.IOException;

public class Compiler{
 
    public static void main(String[] args) throws IOException {

        if(args.length == 0) {

            System.out.println("missing directory or file name...");
            return;
        }

        else if(args.length > 1){

            System.out.println("too many arguments");
            return;
        }


        File file = new File(args[0]);
        

        if(!file.exists()){

            System.out.println("Error: file or directory doesn't exist!");
            return;
        }


        if(file.isFile())
            compileFile(file);
        
        else if(file.isDirectory()) {

            for(var innerFile: file.listFiles()){

                if(innerFile.isFile() && extention(innerFile.getName()).equals("jack")){

                    compileFile(innerFile);
                }
            }

        }


    }


    static String extention(String file){

        return file.substring(file.lastIndexOf(".") + 1);
    }

    static String outputFilePath(File file){

        return file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('\\') + 1) 
        + file.getName().substring(0, file.getName().lastIndexOf('.'))
        + ".vm";
    }


    static void compileFile(File file) throws IOException{

        var outputPath = outputFilePath(file);
        var comp = new Compilation(file, outputPath);
        try {
			comp.compile();
		} catch (Exception e) {
            e.printStackTrace();
            return;
		}
    }


}
