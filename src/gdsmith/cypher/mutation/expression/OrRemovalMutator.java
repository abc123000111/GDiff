package gdsmith.cypher.mutation.expression;

import gdsmith.Randomly;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.ast.IExpression;
import gdsmith.cypher.standard_ast.expr.BinaryComparisonExpression;
import gdsmith.cypher.standard_ast.expr.BinaryLogicalExpression;

import java.util.ArrayList;
import java.util.List;

public class OrRemovalMutator extends ExpressionVisitor{

    public List<BinaryLogicalExpression> orExpressions = new ArrayList<>();

    public OrRemovalMutator(IClauseSequence clauseSequence) {
        super(clauseSequence);
    }

    @Override
    public ExpressionVisitingResult visitBinaryLogicalExpression(ExpressionVisitorContext context, BinaryLogicalExpression expression) {
        if(expression.getOperation() == BinaryLogicalExpression.BinaryLogicalOperation.OR){
            orExpressions.add(expression);
        }
        return ExpressionVisitingResult.continueToVisit();
    }

    @Override
    public void postProcessing(ExpressionVisitorContext context) {
        Randomly randomly = new Randomly();
        if(orExpressions.size() == 0){
            return;
        }
        BinaryLogicalExpression expression = orExpressions.get(randomly.getInteger(0, orExpressions.size()));

        if(expression.getParentExpression() != null){
            IExpression parent = expression.getParentExpression();
            if(randomly.getInteger(0, 100) < 50){
                parent.replaceChild(expression, expression.getLeftExpression());
            }
            else {
                parent.replaceChild(expression, expression.getRightExpression());
            }
        }
        else if(expression.getExpressionRootClause() != null){
            //todo: more
        }
    }
}
