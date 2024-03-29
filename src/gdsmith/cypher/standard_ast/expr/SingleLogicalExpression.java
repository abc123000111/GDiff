package gdsmith.cypher.standard_ast.expr;

import gdsmith.Randomly;
import gdsmith.cypher.ICypherSchema;
import gdsmith.cypher.ast.IExpression;
import gdsmith.cypher.ast.analyzer.ICypherTypeDescriptor;
import gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import gdsmith.cypher.standard_ast.CypherType;
import gdsmith.cypher.standard_ast.CypherTypeDescriptor;

import java.util.List;

public class SingleLogicalExpression extends CypherExpression {

    @Override
    public ICypherTypeDescriptor analyzeType(ICypherSchema schema, List<IIdentifierAnalyzer> identifiers) {
        return new CypherTypeDescriptor(CypherType.BOOLEAN);
    }

    public static SingleLogicalExpression randomLogical(IExpression expr){
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        //int operationNum = randomly.getInteger(0, 59); //todo
        if(operationNum < 30){
            return new SingleLogicalExpression(expr, SingleLogicalOperation.NOT);
        }
        if(operationNum < 60){
            return new SingleLogicalExpression(expr, SingleLogicalOperation.IS_NULL);
        }
        return new SingleLogicalExpression(expr, SingleLogicalOperation.IS_NOT_NULL);
    }

    @Override
    public IExpression getCopy() {
        IExpression child = null;
        if(this.child != null){
            child = this.child.getCopy();
        }
        return new SingleLogicalExpression(child, this.op);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if(originalExpression == child){
            this.child = newExpression;
            newExpression.setParentExpression(this);
            return;
        }

        throw new RuntimeException();
    }

    public enum SingleLogicalOperation{
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        NOT("NOT");

        SingleLogicalOperation(String textRepresentation){
            this.TextRepresentation = textRepresentation;
        }

        private final String TextRepresentation;

        public String getTextRepresentation(){
            return this.TextRepresentation;
        }
    }

    private IExpression child;
    private final SingleLogicalOperation op;

    public SingleLogicalExpression(IExpression child, SingleLogicalOperation op){
        this.child = child;
        this.op = op;
        child.setParentExpression(this);
    }

    public IExpression getChildExpression(){
        return child;
    }

    public SingleLogicalOperation getOperation(){
        return op;
    }

    @Override
    public void toTextRepresentation(StringBuilder sb) {
        sb.append("(");
        if(op == SingleLogicalOperation.NOT){
            sb.append(op.getTextRepresentation()).append(" ");
        }
        child.toTextRepresentation(sb);
        if(op != SingleLogicalOperation.NOT){
            sb.append(" ").append(op.getTextRepresentation());
        }
        sb.append(")");
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof SingleLogicalExpression)){
            return false;
        }
        if(child.equals(((SingleLogicalExpression) o).child)){
            return op == ((SingleLogicalExpression) o).op;
        }
        return false;
    }

}
