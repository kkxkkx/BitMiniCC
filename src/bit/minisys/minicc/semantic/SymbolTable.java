package bit.minisys.minicc.semantic;

import java.awt.datatransfer.SystemFlavorMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    public SymbolTable local;
    public List name = new LinkedList<>();
    public Map Type = new LinkedHashMap<>();
    public Map ArraySize = new LinkedHashMap();
    public Map ArrayType = new LinkedHashMap();

    public boolean findByName(String name) {
        if (this.name.contains(name))
            return true;
        return false;
    }

    public void addByName(String name) {
        this.name.add(name);
    }


    public void addNameType(String funcName, String type) {
        this.Type.put(funcName, new SymbolEntry(funcName, type));
    }

    public String getType(String funcName) {
        if (Type.containsKey(funcName)) {
            SymbolEntry se = (SymbolEntry) this.Type.get(funcName);
            return se.getType();
        }
        return null;
    }
}
