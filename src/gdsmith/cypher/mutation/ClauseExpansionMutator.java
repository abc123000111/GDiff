package gdsmith.cypher.mutation;

import gdsmith.Randomly;
import gdsmith.cypher.CypherGlobalState;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.dsl.QueryFiller;
import gdsmith.cypher.gen.*;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.standard_ast.ClauseSequence;
import gdsmith.cypher.standard_ast.IClauseSequenceBuilder;
import org.neo4j.cypherdsl.core.Cypher;

import java.util.Arrays;

public class ClauseExpansionMutator<S extends CypherSchema<G,?>, G extends CypherGlobalState<?, S>> implements IClauseMutator{

    private IClauseSequence sequence;
    private S schema;

    public ClauseExpansionMutator(IClauseSequence sequence, S schema){
        this.sequence = sequence;
        this.schema = schema;
    }

    @Override
    public void mutate() {
        IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();
        int numOfClauses = Randomly.smallNumber();
        sequence = new RandomQueryGenerator<S, G>().generateClauses(builder.WithClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
        new QueryFiller<S>(sequence,
                new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                new RandomConditionGenerator<>(schema, false),
                new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                schema, builder.getIdentifierBuilder()).startVisit();
    }
}
