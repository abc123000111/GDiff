package gdsmith.cypher.mutation;

import gdsmith.Randomly;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.ast.IMatch;
import gdsmith.cypher.dsl.ClauseVisitor;
import gdsmith.cypher.dsl.IContext;
import gdsmith.cypher.mutation.OptionalAdditionMutator.OptionalAdditionMutatorContext;

import java.util.ArrayList;
import java.util.List;

public class OptionalAdditionMutator extends ClauseVisitor<OptionalAdditionMutatorContext>  implements IClauseMutator  {

    public List<IMatch> matchList = new ArrayList<>();

    public OptionalAdditionMutator(IClauseSequence clauseSequence) {
        super(clauseSequence, new OptionalAdditionMutatorContext());
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class OptionalAdditionMutatorContext implements IContext{

    }

    @Override
    public void visitMatch(IMatch matchClause, OptionalAdditionMutatorContext context) {
        if(!matchClause.isOptional()){
            matchList.add(matchClause);
        }
    }

    @Override
    public void postProcessing(OptionalAdditionMutatorContext context) {
        if(matchList.size() == 0){
            return;
        }

        IMatch match = matchList.get(new Randomly().getInteger(0, matchList.size()));
        match.setOptional(true);
    }
}
