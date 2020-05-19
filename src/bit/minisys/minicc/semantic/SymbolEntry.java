package bit.minisys.minicc.semantic;

public class SymbolEntry {
    public String name;
    public String type;
    public int Value;

    public SymbolEntry(String name, String type) {
        this.type = type;
        this.name = name;
    }
    public SymbolEntry(String name, String type,int value) {
        this.type = type;
        this.name = name;
        this.Value=value;
    }
}
