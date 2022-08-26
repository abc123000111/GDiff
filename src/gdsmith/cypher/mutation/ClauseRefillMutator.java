package gdsmith.cypher.mutation;

import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.dsl.*;
import gdsmith.cypher.gen.RandomAliasGenerator;
import gdsmith.cypher.gen.RandomConditionGenerator;
import gdsmith.cypher.gen.RandomListGenerator;
import gdsmith.cypher.gen.RandomPatternGenerator;
import gdsmith.cypher.schema.CypherSchema;

public class ClauseRefillMutator<S extends CypherSchema<?,?>> extends QueryFiller<S> implements IClauseMutator {
    public ClauseRefillMutator(IClauseSequence clauseSequence, S schema) {
        super(clauseSequence,
                new RandomPatternGenerator<>(schema, clauseSequence.getIdentifierBuilder(), true),
                new RandomConditionGenerator<>(schema, true),
                new RandomAliasGenerator<>(schema, clauseSequence.getIdentifierBuilder(), true),
                new RandomListGenerator<>(schema, clauseSequence.getIdentifierBuilder(), true),
                schema, clauseSequence.getIdentifierBuilder());
    }

    @Override
    public void mutate() {
        startVisit();
    }
}
