package gdsmith.cypher.mutation;

import gdsmith.Randomly;
import gdsmith.cypher.ast.*;
import gdsmith.cypher.dsl.ClauseVisitor;
import gdsmith.cypher.dsl.IContext;
import gdsmith.cypher.gen.RandomPatternGenerator;
import gdsmith.cypher.mutation.LabelAdditionMutator.LabelAdditionMutatorContext;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.standard_ast.Label;
import gdsmith.cypher.standard_ast.Pattern;
import gdsmith.cypher.standard_ast.RelationType;

import java.util.ArrayList;
import java.util.List;

public class LabelAdditionMutator<S extends CypherSchema<?,?>> extends ClauseVisitor<LabelAdditionMutatorContext> implements IClauseMutator {
    
    private List<IPatternElement> patternElements = new ArrayList<>();
    private S schema;

    public LabelAdditionMutator(IClauseSequence clauseSequence, S schema) {
        super(clauseSequence, new LabelAdditionMutatorContext());
        this.schema = schema;
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class LabelAdditionMutatorContext implements IContext{

    }

    @Override
    public void visitMatch(IMatch matchClause, LabelAdditionMutatorContext context) {
        matchClause.getPatternTuple().forEach(pt->{
            patternElements.addAll(pt.getPatternElements());
        });
    }
    

    @Override
    public void postProcessing(LabelAdditionMutatorContext context) {
        Randomly randomly = new Randomly();
        if(patternElements.size() == 0){
            return;
        }

        IPatternElement patternElement = patternElements.get(randomly.getInteger(0, patternElements.size()));

        if(patternElement instanceof INodeIdentifier){
            if(((INodeIdentifier) patternElement).getLabels().size() == 0){
                CypherSchema.CypherLabelInfo labelInfo = schema.getLabels().get(randomly.getInteger(0, schema.getLabels().size()));
                ILabel label = new Label(labelInfo.getName());
                ((INodeIdentifier) patternElement).getLabels().add(label);
            }
            return;
        }
        else if(patternElement instanceof IRelationIdentifier){
            if(((IRelationIdentifier) patternElement).getTypes().size() == 0){
                CypherSchema.CypherRelationTypeInfo relationTypeInfo = schema.getRelationTypes().get(randomly.getInteger(0, schema.getRelationTypes().size()));
                IType type = new RelationType(relationTypeInfo.getName());
                ((IRelationIdentifier) patternElement).getTypes().add(type);
            }
            return;
        }
        throw new RuntimeException();
    }
}
