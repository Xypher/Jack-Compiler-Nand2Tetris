package src;

import java.util.HashMap;

public class SymbolTable{

    private int static_count, var_count, field_count, arg_count;
    private HashMap<String, Identifier> table;

    public SymbolTable(){

        table = new HashMap<>();
        static_count = var_count = field_count = arg_count = 0;
    }

    
    public static class Identifier{

        public final String name, type, catagory;
        public final int idx;
		public Identifier(String name, String type, String catagory, int idx) {
			this.name = name;
			this.type = type;
			this.catagory = catagory;
			this.idx = idx;
        }
        
		@Override
		public String toString() {
			return "Identifier [catagory=" + catagory + ", idx=" + idx + ", name=" + name + ", type=" + type + "]";
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Identifier other = (Identifier) obj;
			if (catagory == null) {
				if (other.catagory != null)
					return false;
			} else if (!catagory.equals(other.catagory))
				return false;
			if (idx != other.idx)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}        

    }


    public Identifier look_up(String name){
        return table.get(name);
    }


    public void add(String name, String type, String catagory){

        switch(catagory){

            case "static":
                add_static(name, type);
                break;
            
            case "var":
                add_var(name, type);
                break;

            case "field":
                add_field(name, type);
                break;

            case "arg":
                add_arg(name, type);
                break;
        }

    }

    public void add_static(String name, String type){

        table.put(name, 
        new Identifier(name, type, "static", static_count++)
        );
    }

    public void add_var(String name, String type){

        table.put(name, 
        new Identifier(name, type, "var", var_count++)
        );
    }

    public void add_field(String name, String type){

        table.put(name, 
        new Identifier(name, type, "field", field_count++)
        );
    }

    public void add_arg(String name, String type){

        table.put(name, 
        new Identifier(name, type, "arg", arg_count++)
        );
    }

    public boolean exists(String name){

        return table.containsKey(name);
    }


    public String type(String name){
        return table.get(name).type;
    }

    public String catagory(String name){
        return table.get(name).catagory;
    }

    public int index(String name){
        return table.get(name).idx;
    }

    public void clear(){

        table.clear();
        static_count = var_count = field_count = arg_count = 0;
    }

	public int getStatic_count() {
		return static_count;
	}

	public int getVar_count() {
		return var_count;
	}

	public int getField_count() {
		return field_count;
	}

	public int getArg_count() {
		return arg_count;
    }
    
    public int size(){
        return table.size();
    }

    @Override
    public String toString() {
        var output = String.format("{\nstatic=%d, field=%d, arg=%d, var=%d\n", 
        static_count, field_count, arg_count, var_count);

        int cnt = 0;
        for(var ident: table.entrySet()){
            if (cnt > 0) {
                output += ",\n";
            }
            output += ident.getValue() + " " + ident.getKey();   
            cnt++;
        }

        output += "\n}";

        return output;
    }

}