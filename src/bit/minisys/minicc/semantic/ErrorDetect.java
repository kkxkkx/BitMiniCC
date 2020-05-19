package bit.minisys.minicc.semantic;

import java.util.LinkedList;
import java.util.List;

public class ErrorDetect {
    public List ErrorList =new LinkedList<>();

    public void addError(String info){
        ErrorList.add(info);
    }
    public void PrintError() {
        for(int i = 0; i < this.ErrorList.size(); ++i) {
            System.out.println("ES" + i + " >> " + (String)this.ErrorList.get(i));
        }

    }
}
