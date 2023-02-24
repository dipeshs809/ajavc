//
// Generated by JTB 1.3.2
//

package visitor;

import syntaxtree.*;
import java.util.*;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order. Your visitors may extend this class.
 */

public class OutputGen implements GJNoArguVisitor<String> {
    // HashMap<Node, Set<String>> resultMap;
    HashMap<Node, HashMap<String, HashSet<String>>> graphMap;
    HashMap<Node, HashMap<String, String>> allocatedRegisters = new HashMap<Node, HashMap<String, String>>();
    HashMap<Node, HashMap<String, String>> metData = new HashMap<Node, HashMap<String, String>>();
    HashMap<Node, HashMap<String, String>> formalParams = new HashMap<Node, HashMap<String, String>>();
    HashMap<Node, HashMap<String, Integer>> spilledVar = new HashMap<Node, HashMap<String, Integer>>();
    ArrayList<String> progOutput;
    
    int numRegisters;
    Node currNode;
    HashMap<Node, HashMap<String, String>> metaData;
    public OutputGen(HashMap<Node, HashMap<String, HashSet<String>>> g, HashMap<Node, HashMap<String, String>> param, int n, HashMap<Node, HashMap<String, String>> metaData, ArrayList<String> progOutput) {
        numRegisters = n;
        graphMap = g;
        this.metaData = metaData;
        this.progOutput = progOutput;
        this.formalParams = param;
        colorGraph();
        // System.out.println(spilledVar);
        // System.out.println(allocatedRegisters);
    }

    public String hasDegLTk(HashMap<String, HashSet<String>> graph) {
        for(String s: graph.keySet()){
            if(graph.get(s).size() < numRegisters) return s;
        }
        return null;
    }

    public int getType(String var){
        // spilled variable
        if(spilledVar.get(currNode).get(var) != null) return 3;
        // register
        if(metaData.get(currNode).get(var)!= null) return 2;
        // method param
        return 1;
    }

    public String getVarStmt(String var){
        int type = getType(var);
        // System.out.println(var + " "+ type);
        if(type == 1) return var;
        else if(type == 2) return "(("+ metaData.get(currNode).get(var) +")"+allocatedRegisters.get(currNode).get(var)+")";
        // ((int) load(0))
        return "(("+ metaData.get(currNode).get(var) +") load(" + spilledVar.get(currNode).get(var) + "))";
    }
// perfect
    public String processassignment(String identifier, String expression){
        String output = null;
        int type = getType(identifier);
        if(type == 1){
            output = identifier+" = "+expression +";";
        }else if(type == 2){
            output = allocatedRegisters.get(currNode).get(identifier)+" = "+expression+ ";";
        }else{
            output = "store("+ spilledVar.get(currNode).get(identifier)+", " + expression + ");";
        }
        return output;
    }

    public HashSet<String> genSet(){
        HashSet<String> temp = new HashSet<>();
        for(int i= 0 ;i<numRegisters; i++){
            temp.add("r"+i);
        }
        return temp;
    }

    public void colorGraph() {
        HashMap<String, HashSet<String>> graph, dipesh;
        for (Node n : graphMap.keySet()) {
            allocatedRegisters.put(n, new HashMap<String, String>());
            spilledVar.put(n, new HashMap<String, Integer>());
            // map of corresponding node
            int cntSpill = 0;
            graph = graphMap.get(n);
            dipesh = new HashMap<String, HashSet<String>>();
            dipesh.putAll(graph);
            Stack<String> regStack = new Stack<>();

            /*repeat
                repeat
                    Remove a node n and all its edges from G, such that the
                    degree of n is less than K
                    Push n onto a stack
                until G has no node with degree less than K
            G is now either empty or all its nodes have degree ≥K
                if G is not empty then
                    Take a node m and all its edges out of G, and mark m for
                    spilling
                endif
            until G is empty
            Take one node at a time from stack and assign a non-conflicting
            color*/

            // repeat
            while (dipesh.keySet().size() > 0) {
                String s = hasDegLTk(dipesh);
                while(s!= null){
                    for(String key : dipesh.keySet()){
                        if(key.equals(s)) continue;
                        else{
                            dipesh.get(key).remove(s);
                        }
                    }
                    dipesh.remove(s);
                    regStack.push(s);
                    s = hasDegLTk(dipesh);
                }
                if(dipesh.keySet().size()>0){
                    for(String key : dipesh.keySet()){
                        // add this variable to spilling
                        if(spilledVar.get(n) == null) spilledVar.put(n, new HashMap<>());
                        for(String variable : dipesh.keySet()){
                            if(variable.equals(key)) continue;
                            else{
                                dipesh.get(variable).remove(key);
                            }
                        }
                        dipesh.remove(key);
                        spilledVar.get(n).put(key, cntSpill);
                        cntSpill++;
                        break;
                    }
                }
            }
            // System.out.println("Register Stack"+regStack);
            HashSet<String> conflictRegisters, nonConflictingReg;
            while(regStack.size()>0){
                String var = regStack.pop();
                nonConflictingReg = genSet();
                conflictRegisters = new HashSet<>();
                for(String key : graph.get(var)){
                    String reg = allocatedRegisters.get(n).get(key);
                    if(reg != null){
                        conflictRegisters.add(reg);
                    }
                }
                nonConflictingReg.removeAll(conflictRegisters);
                for(String reg : nonConflictingReg){
                    allocatedRegisters.get(n).put(var, reg);
                    break;
                }
            }
            // System.out.println("Spilled Variables"+spilledVar.get(n));
            // System.out.println("Allocated registers"+allocatedRegisters.get(n));
        }
    }

    //
    // Auto class visitors--probably don't need to be overridden.
    //
    public String visit(NodeList n) {
        String _ret = null;
        int _count = 0;
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            _count++;
        }
        return _ret;
    }

    public String visit(NodeListOptional n) {
        if (n.present()) {
            String _ret = null;
            int _count = 0;
            for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
                e.nextElement().accept(this);
                _count++;
            }
            return _ret;
        } else
            return null;
    }

    public String visit(NodeOptional n) {
        if (n.present())
            return n.node.accept(this);
        else
            return null;
    }

    public String visit(NodeSequence n) {
        String _ret = null;
        int _count = 0;
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();) {
            e.nextElement().accept(this);
            _count++;
        }
        return _ret;
    }

    public String visit(NodeToken n) {
        return null;
    }

    //
    // User-generated visitor methods below
    //

    /**
     * f0 -> ( <REGLIMIT> )?
     * f1 -> MainClass()
     * f2 -> ( TypeDeclaration() )*
     * f3 -> <EOF>
     */
    public String visit(Goal n) {
        String _ret = null;
        progOutput.add(n.f0.tokenImage);
        progOutput.add("import static a5.Memory.*;");
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public String visit(MainClass n) {
        String _ret = null;
        currNode = n;
        n.f0.accept(this);
        _ret = n.f1.accept(this);
        progOutput.add("class " + _ret + " {");
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        _ret = n.f11.accept(this);
        progOutput.add("public static void main(String[] "+_ret+"){");
        n.f12.accept(this);
        n.f13.accept(this);
        n.f14.accept(this);
        for(int i = 0 ;i< numRegisters; i++){
            progOutput.add("Object r"+i+";");
        }
        progOutput.add("alloca("+ spilledVar.get(n).size() +");");
        n.f15.accept(this);
        n.f16.accept(this);
        n.f17.accept(this);
        progOutput.add("}");
        progOutput.add("}");
        return _ret;
    }

    /**
     * f0 -> ClassDeclaration()
     * | ClassExtendsDeclaration()
     */
    public String visit(TypeDeclaration n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public String visit(ClassDeclaration n) {
        String _ret = null;
        n.f0.accept(this);
        _ret = n.f1.accept(this);
        progOutput.add("class " + _ret + " {");
        n.f2.accept(this);
        n.f3.accept(this);
        for(Node m: n.f3.nodes){
            VarDeclaration v = (VarDeclaration)m;
            String type = v.f0.accept(this);
            String var = v.f1.accept(this);
            progOutput.add(type+" "+var+";");
        }
        n.f4.accept(this);
        n.f5.accept(this);
        progOutput.add("}");
        return _ret;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public String visit(ClassExtendsDeclaration n) {
        String _ret = null, temp= null;
        n.f0.accept(this);
        _ret = n.f1.accept(this);
        n.f2.accept(this);
        temp = n.f3.accept(this);
        progOutput.add("class " + _ret + " extends " + temp + " {");
        n.f4.accept(this);
        n.f5.accept(this);
        for(Node m: n.f5.nodes){
            VarDeclaration v = (VarDeclaration)m;
            String type = v.f0.accept(this);
            String var = v.f1.accept(this);
            progOutput.add(type+" "+var+";");
        }
        n.f6.accept(this);
        n.f7.accept(this);
        progOutput.add("}");
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public String visit(VarDeclaration n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Identifier()
     * f11 -> ";"
     * f12 -> "}"
     */
    public String visit(MethodDeclaration n) {
        currNode = n;
        String _ret = null, temp = null;
        _ret = n.f0.tokenImage + " ";
        _ret += n.f1.accept(this) + " ";
        _ret += n.f2.accept(this) + " (";
        n.f3.accept(this);
        temp = n.f4.accept(this);
        if(temp != null) _ret += temp+"){";
        else _ret+= "){";
        progOutput.add(_ret);
        n.f5.accept(this);
        n.f6.accept(this);
        n.f7.accept(this);
        for(int i=0;i<numRegisters; i++){
            progOutput.add("Object r"+i+";");
        }
        progOutput.add("alloca("+ spilledVar.get(n).size() +");");
        n.f8.accept(this);
        n.f9.accept(this);
        n.f10.accept(this);
        progOutput.add("return "+ getVarStmt(n.f10.accept(this))+";");
        n.f11.accept(this);
        n.f12.accept(this);
        progOutput.add("}");
        return _ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> ( FormalParameterRest() )*
     */
    public String visit(FormalParameterList n) {
        String _ret = null;
        _ret = n.f0.accept(this);
        for(Node m: n.f1.nodes){
            FormalParameterRest f = (FormalParameterRest)m;
            _ret += ", " + f.f1.accept(this);
        }
        return _ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n) {
        String _ret = null;
        _ret = n.f0.accept(this);
        _ret += (" " +  n.f1.accept(this));
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterRest n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
    }

    /**
     * f0 -> ArrayType()
     * | BooleanType()
     * | IntegerType()
     * | FloatType()
     * | Identifier()
     */
    public String visit(Type n) {
        String _ret = null;
        _ret = n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "int"
     * f1 -> "["
     * f2 -> "]"
     */
    public String visit(ArrayType n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return "int[]";
    }

    /**
     * f0 -> "float"
     */
    public String visit(FloatType n) {
        String _ret = null;
        n.f0.accept(this);
        return "float";
    }

    /**
     * f0 -> "boolean"
     */
    public String visit(BooleanType n) {
        String _ret = null;
        n.f0.accept(this);
        return "boolean";
    }

    /**
     * f0 -> "int"
     */
    public String visit(IntegerType n) {
        String _ret = null;
        n.f0.accept(this);
        return "int";
    }

    /**
     * f0 -> Block()
     * | AssignmentStatement()
     * | ArrayAssignmentStatement()
     * | FieldAssignmentStatement()
     * | IfStatement()
     * | WhileStatement()
     * | PrintStatement()
     * | LivenessQueryStatement()
     */
    public String visit(Statement n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n) {
        String _ret = null;
        progOutput.add("{");
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        progOutput.add("}");
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    public String visit(AssignmentStatement n) {
        String _ret = null, expr = null;
        _ret = n.f0.accept(this);
        n.f1.accept(this);
        expr = n.f2.accept(this);
        progOutput.add(processassignment(_ret, expr));
        n.f3.accept(this);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Identifier()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Identifier()
     * f6 -> ";"
     */
    public String visit(ArrayAssignmentStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        n.f6.accept(this);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "="
     * f4 -> Identifier()
     * f5 -> ";"
     */
    public String visit(FieldAssignmentStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        n.f5.accept(this);
        return _ret;
    }

    /**
     * f0 -> IfthenElseStatement()
     * | IfthenStatement()
     */
    public String visit(IfStatement n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Identifier()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(IfthenStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        _ret = n.f2.accept(this);
        n.f3.accept(this);
        progOutput.add("if (" + getVarStmt(_ret) + ")");
        n.f4.accept(this);
        return _ret;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Identifier()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    public String visit(IfthenElseStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        _ret = n.f2.accept(this);
        n.f3.accept(this);
        progOutput.add("if (" + getVarStmt(_ret) + ")");
        n.f4.accept(this);
        n.f5.accept(this);
        progOutput.add("else");
        n.f6.accept(this);
        return _ret;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Identifier()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        _ret = n.f2.accept(this);
        n.f3.accept(this);
        progOutput.add("while (" + getVarStmt(_ret) + ")");
        n.f4.accept(this);
        return _ret;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Identifier()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        _ret = n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        progOutput.add("System.out.println(" + getVarStmt(_ret) + ");");
        return _ret;
    }

    /**
     * f0 -> <SCOMMENT1>
     * f1 -> <LIVENESSQUERY>
     * f2 -> <SCOMMENT2>
     */
    public String visit(LivenessQueryStatement n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> OrExpression()
     * | AndExpression()
     * | CompareExpression()
     * | neqExpression()
     * | PlusExpression()
     * | MinusExpression()
     * | TimesExpression()
     * | DivExpression()
     * | ArrayLookup()
     * | ArrayLength()
     * | MessageSend()
     * | PrimaryExpression()
     */
    public String visit(Expression n) {
        String _ret = null;
        _ret = n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "&&"
     * f2 -> Identifier()
     */
    public String visit(AndExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "||"
     * f2 -> Identifier()
     */
    public String visit(OrExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "<="
     * f2 -> Identifier()
     */
    public String visit(CompareExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "!="
     * f2 -> Identifier()
     */
    public String visit(neqExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "+"
     * f2 -> Identifier()
     */
    public String visit(PlusExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "-"
     * f2 -> Identifier()
     */
    public String visit(MinusExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "*"
     * f2 -> Identifier()
     */
    public String visit(TimesExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "/"
     * f2 -> Identifier()
     */
    public String visit(DivExpression n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret+= getVarStmt(n.f2.accept(this));
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Identifier()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ArgList() )?
     * f5 -> ")"
     */
    public String visit(MessageSend n) {
        String _ret = null, temp = null;
        _ret = getVarStmt(n.f0.accept(this));
        _ret += n.f1.tokenImage;
        _ret += n.f2.accept(this);
        _ret += n.f3.tokenImage;
        temp = n.f4.accept(this);
        if(temp != null) _ret += temp;
        _ret += n.f5.tokenImage;
        return _ret;
    }

    /**
     * f0 -> Identifier()
     * f1 -> ( ArgRest() )*
     */
    public String visit(ArgList n) {
        String _ret = null;
        _ret = getVarStmt(n.f0.accept(this));
        for(Node m: n.f1.nodes){
            _ret += ((ArgRest)m).f0.tokenImage;
            _ret += getVarStmt(((ArgRest)m).f1.accept(this));
            // System.out.println(getVarStmt(((ArgRest)m).f1.accept(this)));
        }
        return _ret;
    }

    /**
     * f0 -> ","
     * f1 -> Identifier()
     */
    public String visit(ArgRest n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        return _ret;
    }

    /**
     * f0 -> IntegerLiteral()
     * | FloatLiteral()
     * | TrueLiteral()
     * | FalseLiteral()
     * | Identifier()
     * | ThisExpression()
     * | ArrayAllocationExpression()
     * | AllocationExpression()
     * | NotExpression()
     */
    public String visit(PrimaryExpression n) {
        String _ret = null;
        _ret = n.f0.accept(this);
        if(allocatedRegisters.get(currNode).get(_ret)!= null) _ret = getVarStmt(_ret);
        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public String visit(IntegerLiteral n) {
        String _ret = null;
        _ret = n.f0.tokenImage;
        return _ret;
    }

    /**
     * f0 -> <FLOAT_LITERAL>
     */
    public String visit(FloatLiteral n) {
        String _ret = null;
        _ret = n.f0.tokenImage;
        return _ret;
    }

    /**
     * f0 -> "true"
     */
    public String visit(TrueLiteral n) {
        String _ret = null;
        _ret = n.f0.tokenImage;
        return _ret;
    }

    /**
     * f0 -> "false"
     */
    public String visit(FalseLiteral n) {
        String _ret = null;
        _ret = n.f0.tokenImage;
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public String visit(Identifier n) {
        String _ret = null;
        _ret = n.f0.tokenImage;
        return _ret;
    }

    /**
     * f0 -> "this"
     */
    public String visit(ThisExpression n) {
        String _ret = null;
        n.f0.accept(this);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Identifier()
     * f4 -> "]"
     */
    public String visit(ArrayAllocationExpression n) {
        String _ret = null;
        n.f0.accept(this);
        n.f1.accept(this);
        n.f2.accept(this);
        n.f3.accept(this);
        n.f4.accept(this);
        return _ret;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n) {
        String _ret = null;
        _ret = n.f0.tokenImage + " ";
        _ret += n.f1.accept(this);
        _ret += n.f2.tokenImage;
        _ret += n.f3.tokenImage;
        return _ret;
    }

    /**
     * f0 -> "!"
     * f1 -> Identifier()
     */
    public String visit(NotExpression n) {
        String _ret = null;
        _ret = n.f0.tokenImage;
        _ret += getVarStmt(n.f1.accept(this));
        return _ret;
    }

}
