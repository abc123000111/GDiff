package gdsmith.cypher.gen;

import gdsmith.Randomly;
import gdsmith.cypher.CypherGlobalState;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.dsl.IQueryGenerator;
import gdsmith.cypher.dsl.QueryFiller;
import gdsmith.cypher.mutation.ClauseScissorsMutator;
import gdsmith.cypher.mutation.WhereRemovalMutator;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.standard_ast.ClauseSequence;
import gdsmith.cypher.standard_ast.IClauseSequenceBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class RandomQueryGenerator<S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> implements IQueryGenerator<S, G> {

    private static final int maxSeedClauseLength = 8;
    public static class Seed{
        IClauseSequence sequence;
        boolean bugDetected;
        int resultLength;

        public Seed(IClauseSequence sequence, boolean bugDetected, int resultLength){
            this.sequence = sequence;
            this.bugDetected = bugDetected;
            this.resultLength = resultLength;
        }
    }

    private List<Seed> seeds = new ArrayList<>();
    private int numOfQueries = 0;

    public IClauseSequenceBuilder generateClauses(IClauseSequenceBuilder seq, int len, List<String> generateClause) {
        if (len == 0) {
            return seq;
        }
        Randomly r = new Randomly();
        String generate = generateClause.get(r.getInteger(0, generateClause.size()));
        if (generate == "MATCH") {
            return generateClauses(seq.MatchClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
        } else if (generate == "OPTIONAL MATCH") {
            // return generateClauses(seq.OptionalMatchClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
            return generateClauses(seq.OptionalMatchClause(), len - 1, Arrays.asList("OPTIONAL MATCH", "WITH")); //todo
        } else if (generate == "WITH") {
            return generateClauses(seq.WithClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
        } else {
            return generateClauses(seq.UnwindClause(), len - 1, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND"));
        }
    }

//    public void addSeed(Seed seed){
//        //todo 判断是否需要加到seed中，如果需要，则加入seeds
//        if (seed.sequence.getClauseList().size() <= addSeedResultSetSizeThreshold) {
//            seeds.add(seed);
//        }
//    }

    public IClauseSequence generateQuery(G globalState){
        S schema = globalState.getSchema();
        Randomly r = new Randomly();
        IClauseSequence sequence = null;

        if (numOfQueries < globalState.getOptions().getNrQueries()) {
            IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();
            int numOfClauses = r.getInteger(1, 8);
            sequence = generateClauses(builder.MatchClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
            new QueryFiller<S>(sequence,
                    new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomConditionGenerator<>(schema, false),
                    new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    schema, builder.getIdentifierBuilder()).startVisit();
        } else {
            IClauseSequence seedSeq = seeds.get(r.getInteger(0, seeds.size())).sequence;
            int kind = r.getInteger(1, 3);
            if (kind == 1) {
                IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder(seedSeq);
                int numOfClauses = Randomly.smallNumber();
                sequence = generateClauses(builder.WithClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
                new QueryFiller<S>(sequence,
                        new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                        new RandomConditionGenerator<>(schema, false),
                        new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                        new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                        schema, builder.getIdentifierBuilder()).startVisit();
            } else if (kind == 2) {
                WhereRemovalMutator mutator = new WhereRemovalMutator<>(seedSeq);
                mutator.mutate();
                sequence = mutator.getClauseSequence();
            } else if (kind == 3) {
                ClauseScissorsMutator mutator = new ClauseScissorsMutator(seedSeq);
                mutator.mutate();
                sequence = mutator.getClauseSequence();
            }
        }
        numOfQueries++;
        System.out.println(numOfQueries);
        return sequence;
    }

    @Override
    public void addExecutionRecord(IClauseSequence clauseSequence, boolean isBugDetected, int resultSize) {
        if (clauseSequence.getClauseList().size() <= maxSeedClauseLength) {
            seeds.add(new Seed(clauseSequence, isBugDetected, resultSize));
        }
    }
}
