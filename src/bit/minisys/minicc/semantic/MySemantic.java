package bit.minisys.minicc.semantic;

import bit.minisys.minicc.parser.ast.ASTCompilationUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class MySemantic implements IMiniCCSemantic {
    private SymbolTable global;

    @Override
    public String run(String iFile) throws Exception {
        System.out.println("Semantic starting...");
        ErrorDetect error = new ErrorDetect();
        ObjectMapper mapper = new ObjectMapper();
        this.global = new SymbolTable();
        ASTCompilationUnit program = (ASTCompilationUnit) mapper.readValue(new File(iFile), ASTCompilationUnit.class);

        SymbolTableVisitor visitor = new SymbolTableVisitor(this.global, error);

        program.accept(visitor);

        if (error.ErrorList.size() > 0) {
            System.out.println("errors:");
            System.out.println("------------------------------------");
            error.PrintError();
            System.out.println("------------------------------------");
        }

        System.out.println("4. Semantic finished!");
        return iFile;
    }
}
