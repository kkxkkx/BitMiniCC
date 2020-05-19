package bit.minisys.minicc.semantic;

import bit.minisys.minicc.parser.ast.*;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTableVisitor implements ASTVisitor {
    private SymbolTable global;
    private SymbolTable local;
    private SymbolTable func;
    private boolean isFuncLocal;  //是否函数内部局部变量
    private Map localSymbolTableMap = new LinkedHashMap();
    private ErrorDetect error;
    private String funcName;
    private int depth;

    public SymbolTableVisitor(SymbolTable global, ErrorDetect error) {
        this.global = global;
        this.func = new SymbolTable();
        this.local = new SymbolTable();
        isFuncLocal = false;
        this.error = error;
        this.depth = 0;
    }

    @Override
    public void visit(ASTCompilationUnit program) throws Exception {
        program.scope = this.global;
        Iterator iterator = program.items.iterator();

        while (iterator.hasNext()) {
            ASTNode node = (ASTNode) iterator.next();
            if (node instanceof ASTFunctionDefine) {
                //函数内部
                isFuncLocal = true;
                this.visit((ASTFunctionDefine) node);
                isFuncLocal = true;
            } else if (node instanceof ASTDeclaration) {
                isFuncLocal = false;
                this.visit((ASTDeclaration) node);
            } else {
                error.addError("program's items should be Declaration or FunctionDefine");
            }
        }
    }

    //声明
    @Override
    public void visit(ASTDeclaration declaration) throws Exception {
        if (this.isFuncLocal)
            declaration.scope = this.local;
        else
            declaration.scope = this.global;
        Iterator iterator = declaration.initLists.iterator();
        while (iterator.hasNext()) {
            ASTInitList initList = (ASTInitList) iterator.next();
            this.visit(initList);
            String name = initList.declarator.getName();

            boolean reLocal = this.local.findByName(name);
            boolean reGobal = this.global.findByName(name);
            if (reGobal || reLocal) {
                error.addError("Declaration:" + name + " has been declarated.");
                return;
            }
            //exprs
            if (isFuncLocal)
                this.local.addByName(name);
            else
                this.global.addByName(name);


        }
    }

    @Override
    public void visit(ASTArrayDeclarator arrayDeclarator) throws Exception {

    }

    @Override
    public void visit(ASTVariableDeclarator variableDeclarator) throws Exception {
        variableDeclarator.scope = this.local;
    }

    @Override
    public void visit(ASTFunctionDeclarator functionDeclarator) throws Exception {
        if (functionDeclarator == null)
            return;
        if (!(functionDeclarator.declarator instanceof ASTVariableDeclarator)) {
            error.addError("FunctionDeclarator:must be a global declarator.");
        } else {
            this.visit(functionDeclarator.declarator);
            if (functionDeclarator != null && functionDeclarator.params != null) {
                Iterator iterator = functionDeclarator.params.iterator();
                while (iterator.hasNext()) {
                    ASTParamsDeclarator paramsDeclarator = (ASTParamsDeclarator) iterator.next();
                    paramsDeclarator.scope = local;
                    if (paramsDeclarator.declarator != null) {
                        if (paramsDeclarator.declarator instanceof ASTFunctionDeclarator) {
                            error.addError("FunctionDeclarator:function's declarator can not be a functionDeclarator either.");
                            return;
                        }
                        this.visit(paramsDeclarator.declarator);
                        this.local.addByName(paramsDeclarator.declarator.getName());
                    }
                }
            }
        }
    }

    @Override
    public void visit(ASTParamsDeclarator paramsDeclarator) throws Exception {

    }

    @Override
    public void visit(ASTArrayAccess arrayAccess) throws Exception {

    }

    @Override
    public void visit(ASTBinaryExpression binaryExpression) throws Exception {
        if (binaryExpression == null)
            return;
        binaryExpression.scope = this.local;
        ASTExpression exp1 = binaryExpression.expr1;
        ASTExpression exp2 = binaryExpression.expr2;
        this.visit(exp1);
        this.visit(exp2);

    }

    @Override
    public void visit(ASTBreakStatement breakStat) throws Exception {
        if (breakStat == null)
            return;
        breakStat.scope = this.local;
        if (this.depth == 0)
            error.addError("BreakStatement:must be in a LoopStatement.");
    }

    @Override
    public void visit(ASTContinueStatement continueStatement) throws Exception {

    }

    @Override
    public void visit(ASTCastExpression castExpression) throws Exception {

    }

    @Override
    public void visit(ASTCharConstant charConst) throws Exception {

    }

    @Override
    public void visit(ASTCompoundStatement compoundStat) throws Exception {

    }

    @Override
    public void visit(ASTConditionExpression conditionExpression) throws Exception {

    }

    @Override
    public void visit(ASTExpression expression) throws Exception {
        if (expression != null) {
            if (expression instanceof ASTBinaryExpression) {
                this.visit((ASTBinaryExpression) expression);
            }else if (expression instanceof ASTFunctionCall) {
                this.visit((ASTFunctionCall) expression);
            } else if (expression instanceof ASTIdentifier) {
                this.visit((ASTIdentifier) expression);
            }

        }
    }

    @Override
    public void visit(ASTExpressionStatement expressionStat) throws Exception {

    }

    @Override
    public void visit(ASTFloatConstant floatConst) throws Exception {

    }

    @Override
    public void visit(ASTFunctionCall funcCall) throws Exception {
        if (funcCall != null) {
            funcCall.scope = this.local;
            if (!(funcCall.funcname instanceof ASTIdentifier))
                error.addError("The name of Function must be Identifier");
            else {
                String name = ((ASTIdentifier) funcCall.funcname).value;
                if (!this.func.findByName(name))
                    error.addError("Function [" + name + "] hasn't been declared before");
                else {
                    this.visit(funcCall.funcname);
                    Iterator iterator = funcCall.argList.iterator();
                    ASTExpression expression;
                    while (iterator.hasNext()) {
                        expression = (ASTExpression) iterator.next();
                        this.visit(expression);
                    }
                }
            }
        }
    }

    @Override
    public void visit(ASTGotoStatement gotoStat) throws Exception {

    }

    @Override
    public void visit(ASTIdentifier identifier) throws Exception {
        if (identifier == null)
            return;
        if (!this.local.findByName(identifier.value) && !this.global.findByName(identifier.value))
            error.addError("Variable [" + identifier.value + "] has not been declared before");


    }

    @Override
    public void visit(ASTInitList initList) throws Exception {
        if (initList != null) {
            initList.scope = this.local;
            this.visit(initList.declarator);
            if (initList.declarator != null && initList.exprs != null) {
                Iterator iterator = initList.exprs.iterator();
                while (iterator.hasNext()) {
                    ASTExpression expression = (ASTExpression) iterator.next();
                    this.visit(expression);
                }
            }
        }
    }

    @Override
    public void visit(ASTIntegerConstant intConst) throws Exception {

    }

    @Override
    public void visit(ASTIterationDeclaredStatement iterationDeclaredStat) throws Exception {
        if (iterationDeclaredStat == null)
            return;
        iterationDeclaredStat.scope = this.local;
        this.visit(iterationDeclaredStat.init);

        if (iterationDeclaredStat.cond != null) {
            Iterator iterator = iterationDeclaredStat.cond.iterator();
            while (iterator.hasNext()) {
                ASTExpression expression = (ASTExpression) iterator.next();
                this.visit(expression);
            }
        }

        if (iterationDeclaredStat.step != null) {
            Iterator iterator = iterationDeclaredStat.step.iterator();
            while (iterator.hasNext()) {
                ASTExpression expression = (ASTExpression) iterator.next();
                this.visit(expression);
            }
        }

        if (iterationDeclaredStat.stat instanceof ASTCompoundStatement) {
            iterationDeclaredStat.stat.scope = this.local;
            depth++;
            Iterator iterator = ((ASTCompoundStatement) iterationDeclaredStat.stat).blockItems.iterator();
            while (iterator.hasNext()) {
                ASTNode node = (ASTNode) iterator.next();
                if (node instanceof ASTStatement)
                    this.visit((ASTStatement) node);
                else if (node instanceof ASTDeclaration)
                    this.visit((ASTDeclaration) node);
            }
            depth--;
        } else {
            depth++;
            this.visit(iterationDeclaredStat.stat);
            depth--;
        }
        this.local = iterationDeclaredStat.scope;
    }

    @Override
    public void visit(ASTIterationStatement iterationStat) throws Exception {

    }

    @Override
    public void visit(ASTLabeledStatement labeledStat) throws Exception {

    }

    @Override
    public void visit(ASTMemberAccess memberAccess) throws Exception {

    }

    @Override
    public void visit(ASTPostfixExpression postfixExpression) throws Exception {

    }

    @Override
    public void visit(ASTReturnStatement returnStat) throws Exception {

    }

    @Override
    public void visit(ASTSelectionStatement selectionStat) throws Exception {

    }

    @Override
    public void visit(ASTStringConstant stringConst) throws Exception {

    }

    @Override
    public void visit(ASTTypename typename) throws Exception {

    }

    @Override
    public void visit(ASTUnaryExpression unaryExpression) throws Exception {

    }

    @Override
    public void visit(ASTUnaryTypename unaryTypename) throws Exception {

    }

    @Override
    public void visit(ASTFunctionDefine functionDefine) throws Exception {
        if (functionDefine != null) {
            functionDefine.scope = this.global;
            this.local = new SymbolTable();
            this.funcName = functionDefine.declarator.getName();
            String type = ((ASTToken) functionDefine.specifiers.get(0)).value;
            if (!(functionDefine.declarator instanceof ASTFunctionDeclarator))
                error.addError("FunctionDefine:Declarator is illegal.");
            else {
                this.visit(functionDefine.declarator);
                if (this.func.findByName(funcName)) {
                    error.addError("FunctionDefine:" + funcName + " is defined.");
                    return;
                }
                this.func.addByName(this.funcName);
            }

            Iterator iterator = functionDefine.body.blockItems.iterator();
            while (iterator.hasNext()) {
                ASTNode node = (ASTNode) iterator.next();
                if (node instanceof ASTStatement)
                    this.visit((ASTStatement) node);
                else if (node instanceof ASTDeclaration)
                    this.visit((ASTDeclaration) node);
            }

        }
    }

    @Override
    public void visit(ASTDeclarator declarator) throws Exception {
        if (declarator != null) {
            if (declarator instanceof ASTVariableDeclarator) {
                this.visit((ASTVariableDeclarator) declarator);
            } else if (declarator instanceof ASTFunctionDeclarator) {
                this.visit((ASTFunctionDeclarator) declarator);
            } else {
                error.addError("Illegal Declarator");
            }
        }
    }

    @Override
    public void visit(ASTStatement statement) throws Exception {
        if (statement == null)
            return;
        if (statement instanceof ASTIterationStatement) {
            this.visit((ASTIterationStatement) statement);
        } else if (statement instanceof ASTIterationDeclaredStatement) {
            this.visit((ASTIterationDeclaredStatement) statement);
        } else if (statement instanceof ASTBreakStatement) {
            this.visit((ASTBreakStatement) statement);
        } else if (statement instanceof ASTCompoundStatement) {
            this.visit((ASTCompoundStatement) statement);
        }else if (statement instanceof ASTExpressionStatement) {
            this.visit((ASTExpressionStatement) statement);
        }
    }

    @Override
    public void visit(ASTToken token) throws Exception {

    }
}
