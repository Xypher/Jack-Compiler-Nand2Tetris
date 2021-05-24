package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    private BufferedWriter writer;

    public VMWriter(File outFile) throws IOException{
        
        writer = new BufferedWriter(new FileWriter(outFile));
    }


    public void writePush(String segment, int idx){
        write("\t", "push", segment, Integer.toString(idx));
    }

    public void writePop(String segment, int idx){
        write("\t", "pop", segment, Integer.toString(idx));
    }

    public void writeArithmetic(String command){
        write("\t", command);
    }
    
    public void writeLabel(String label_name){
        write("label", label_name);
    }

    public void writeGoTo(String label_name){
        write("\t", "goto", label_name);
    }

    public void writeIfGoTo(String label_name){
        write("\t", "if-goto", label_name);
    }

    public void writeCall(String func_name, int arg_cnt){
        write("\t", "call", func_name, Integer.toString(arg_cnt));
    }

    public void writeFunction(String func_name, int local_cnt){
        write("function", func_name, Integer.toString(local_cnt));
    }

    public void writeReturn(){
        write("\t", "return");
    }
    
    public void close() throws IOException{
        writer.flush();
        writer.close();
    }


    private void write(String... args){

        int cnt = 0;
        var buffer = new StringBuffer("");
        for(var arg: args){

            if(cnt > 0)
                buffer.append(" ");
            buffer.append(arg);
            cnt++;
        }

        buffer.append('\n');

        try {
			writer.write(buffer.toString());
		} catch (IOException e) {
            System.out.println("Error writing to file");
			e.printStackTrace();
		}

    }

}
