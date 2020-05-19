package bit.minisys.minicc.semantic;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    public SymbolTable local;
    public List name=new LinkedList<>();
    public Map Type=new LinkedHashMap<>();

    public boolean findByName(String name){
        if(this.name.contains(name))
            return true;
        return false;
    }

    public void addByName(String name){
        this.name.add(name);
    }

    public String getType(String name){
        if(this.Type.containsKey(name)){
            SymbolEntry se= (SymbolEntry) this.Type.get(name);
            return se.type;
        }
        return null;
    }
}
