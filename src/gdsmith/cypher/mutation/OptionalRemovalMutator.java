package gdsmith.cypher.mutation;

import gdsmith.Randomly;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.ast.IMatch;
import gdsmith.cypher.dsl.ClauseVisitor;
import gdsmith.cypher.dsl.IContext;
import gdsmith.cypher.mutation.OptionalRemovalMutator.OptionalRemovalMutatorContext;

import java.util.ArrayList;
import java.util.List;

public class OptionalRemovalMutator extends ClauseVisitor<OptionalRemovalMutatorContext> implements IClauseMutator  {

    public List<IMatch> matchList = new ArrayList<>();

    public OptionalRemovalMutator(IClauseSequence clauseSequence) {
        super(clauseSequence, new OptionalRemovalMutatorContext());
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class OptionalRemovalMutatorContext implements IContext{

    }

    @Override

    public void visitMatch(IMatch matchClause, OptionalRemovalMutatorContext context) {
        if(matchClause.isOptional()){
            matchList.add(matchClause);
        }
    }

    @Override
    public void postProcessing(OptionalRemovalMutatorContext context) {
        if(matchList.size() == 0){
            return;
        }

        IMatch match = matchList.get(new Randomly().getInteger(0, matchList.size()));
        match.setOptional(true);
    }
}
