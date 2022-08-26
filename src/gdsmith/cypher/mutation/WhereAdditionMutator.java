package gdsmith.cypher.mutation;

import gdsmith.Randomly;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.ast.ICypherClause;
import gdsmith.cypher.ast.IMatch;
import gdsmith.cypher.ast.IWith;
import gdsmith.cypher.ast.analyzer.IMatchAnalyzer;
import gdsmith.cypher.dsl.ClauseVisitor;
import gdsmith.cypher.dsl.IContext;
import gdsmith.cypher.gen.RandomConditionGenerator;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.mutation.WhereAdditionMutator.WhereAdditionMutatorContext;

import java.util.ArrayList;
import java.util.List;

public class WhereAdditionMutator<S extends CypherSchema<?,?>> extends ClauseVisitor<WhereAdditionMutatorContext>  implements IClauseMutator  {

    public List<ICypherClause> matchOrWithList = new ArrayList<>();
    public S schema;

    public WhereAdditionMutator(IClauseSequence clauseSequence, S schema) {
        super(clauseSequence, new WhereAdditionMutatorContext());
        this.schema = schema;
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class WhereAdditionMutatorContext implements IContext {

    }

    @Override
    public void visitMatch(IMatch matchClause, WhereAdditionMutatorContext context) {
        if(matchClause.getCondition() == null){
            matchOrWithList.add(matchClause);
        }
    }

    @Override
    public void visitWith(IWith withClause, WhereAdditionMutatorContext context) {
        if(withClause.getCondition() == null){
            matchOrWithList.add(withClause);
        }
    }

    @Override
    public void postProcessing(WhereAdditionMutatorContext context) {
        if(matchOrWithList.size() == 0){
            return;
        }

        ICypherClause clause = matchOrWithList.get(new Randomly().getInteger(0, matchOrWithList.size()));
        if(clause instanceof IMatch){
            new RandomConditionGenerator<S>(schema, true).generateMatchCondition(((IMatch) clause).toAnalyzer(), schema);
            return;
        }
        else if(clause instanceof IWith){
            new RandomConditionGenerator<S>(schema, true).generateWithCondition(((IWith) clause).toAnalyzer(), schema);
            return;
        }

        throw new RuntimeException();

    }
}
