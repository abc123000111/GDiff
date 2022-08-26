package gdsmith.cypher.gen;

import gdsmith.Randomly;
import gdsmith.cypher.CypherGlobalState;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.dsl.IQueryGenerator;
import gdsmith.cypher.dsl.QueryFiller;
import gdsmith.cypher.mutation.*;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.standard_ast.ClauseSequence;
import gdsmith.cypher.standard_ast.IClauseSequenceBuilder;

import java.util.*;

public class AdvancedQueryGenerator <S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>> implements IQueryGenerator<S, G> {
    private static final int maxSeedClauseLength = 8;
    private static final int mutationProb = 90;
    public static class Seed{
        IClauseSequence sequence;
        boolean bugDetected;
        int resultLength;
        int selectedTimes;
        int nonEmptyTimes;

        public Seed(IClauseSequence sequence, boolean bugDetected, int resultLength){
            this.sequence = sequence;
            this.bugDetected = bugDetected;
            this.resultLength = resultLength;
            this.selectedTimes = 0;
            this.nonEmptyTimes = 0;
        }

        public int getWeight(){
            if(selectedTimes > 0){
                return nonEmptyTimes * 100000 / selectedTimes;
            }
            return 100000;
        }
    }

    private List<Seed> seeds = new ArrayList<>();
    private int numOfQueries = 0;

    private static class MutatorUsageInfo{
        private int selectedTimes;
        private int nonEmptyTimes;

        public MutatorUsageInfo(){
            selectedTimes = 0;
            nonEmptyTimes = 0;
        }

        public void incSelectedTimes(){
            selectedTimes++;
        }

        public void incNonEmptyTimes(){
            nonEmptyTimes++;
        }

        public int getWeight(){
            if(selectedTimes > 0){
                return nonEmptyTimes * 100000 / selectedTimes;
            }
            return 100000;
        }
    }

    private Map<MutatorType, MutatorUsageInfo> mutatorUsedTimes = new HashMap<>();
    private MutatorType lastStrategy = null;
    private Seed lastSelectedSeed = null;

    private IClauseSequenceBuilder generateClauses(IClauseSequenceBuilder seq, int len, List<String> generateClause) {
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


    @Override
    public void addExecutionRecord(IClauseSequence clauseSequence, boolean isBugDetected, int resultSize) {
        if (clauseSequence.getClauseList().size() <= maxSeedClauseLength) {
            seeds.add(new Seed(clauseSequence, isBugDetected, resultSize));
        }
        if(resultSize > 0 && lastStrategy != null){
            //todo: 这样写其实不太好，绑定了变异和执行反馈的顺序
            mutatorUsedTimes.get(lastStrategy).nonEmptyTimes++;
        }
        if(resultSize > 0 && lastSelectedSeed != null){
            //todo: 这样写其实不太好，绑定了变异和执行反馈的顺序
            lastSelectedSeed.nonEmptyTimes++;
        }
    }

//    public void addSeed(Seed seed){
//        //todo 判断是否需要加到seed中，如果需要，则加入seeds
//        if (seed.sequence.getClauseList().size() <= maxSeedClauseLength) {
//            seeds.add(seed);
//        }
//    }

    private IClauseSequence selectSeed(){
        Randomly r = new Randomly();

        //todo: change to seed selection algorithm
        int totalWeight = 0;
        for(Seed seed : seeds){
            totalWeight += seed.getWeight();
        }

        int randomNum = r.getInteger(0, totalWeight);

        Seed selectedSeed = seeds.get(0);

        for(Seed seed : seeds){
            randomNum -= seed.getWeight();
            if(randomNum < 0){
                selectedSeed = seed;
                break;
            }
        }

        selectedSeed.selectedTimes++;
        lastSelectedSeed = selectedSeed;
        return selectedSeed.sequence;
    }



    private IClauseSequence mutate(G globalState, IClauseSequence seedSeq){
        //todo: mutation strategy
        Randomly r = new Randomly();

        for(MutatorType type : MutatorType.values()){
            if(!mutatorUsedTimes.containsKey(type)){
                mutatorUsedTimes.put(type, new MutatorUsageInfo());
            }
        }

        int totalWeight = 0;

        for(MutatorType type : MutatorType.values()){
            totalWeight += mutatorUsedTimes.get(type).getWeight();
        }

        int randomNum = r.getInteger(0, totalWeight);

        MutatorType selectedType = MutatorType.CLAUSE_REFILL;

        for(MutatorType type : MutatorType.values()){
            randomNum -= mutatorUsedTimes.get(type).getWeight();
            if(randomNum < 0){
                selectedType = type;
                break;
            }
        }

        MutatorType.mutate(selectedType, globalState, seedSeq);
        mutatorUsedTimes.get(selectedType).incSelectedTimes();
        lastStrategy = selectedType;

        return seedSeq;


//        S schema = globalState.getSchema();
//        IClauseSequence sequence = null;
//        int kind = r.getInteger(1, 3);
//        if (kind == 1) {
//            IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder(seedSeq);
//            int numOfClauses = Randomly.smallNumber();
//            sequence = generateClauses(builder.WithClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
//            new QueryFiller<S>(sequence,
//                    new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
//                    new RandomConditionGenerator<>(schema, false),
//                    new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
//                    new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
//                    schema, builder.getIdentifierBuilder()).startVisit();
//            mutatorUsedTimes.put(MutatorType.CLAUSE_REFILL, mutatorUsedTimes.get(MutatorType.CLAUSE_REFILL) + 1);
//        } else if (kind == 2) {
//            WhereRemovalMutator mutator = new WhereRemovalMutator<>(seedSeq);
//            mutator.mutate();
//            sequence = mutator.getClauseSequence();
//            mutatorUsedTimes.put(MutatorType.CONDITION_REFILL, mutatorUsedTimes.get(MutatorType.CONDITION_REFILL) + 1);
//        } else if (kind == 3) {
//            ClauseScissorsMutator mutator = new ClauseScissorsMutator(seedSeq);
//            mutator.mutate();
//            sequence = mutator.getClauseSequence();
//            mutatorUsedTimes.put(MutatorType.CLAUSE_SCISSORS, mutatorUsedTimes.get(MutatorType.CLAUSE_SCISSORS) + 1);
//        }

    }

    public IClauseSequence generateQuery(G globalState){
        S schema = globalState.getSchema();
        Randomly r = new Randomly();
        IClauseSequence sequence = null;

        if (r.getInteger(0, 100) > mutationProb || seeds.size() == 0) {
            IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();
            int numOfClauses = r.getInteger(1, 6);
            sequence = generateClauses(builder.MatchClause(), numOfClauses, Arrays.asList("MATCH", "OPTIONAL MATCH", "WITH", "UNWIND")).ReturnClause().build();
            new QueryFiller<S>(sequence,
                    new RandomPatternGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomConditionGenerator<>(schema, false),
                    new RandomAliasGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    new RandomListGenerator<>(schema, builder.getIdentifierBuilder(), false),
                    schema, builder.getIdentifierBuilder()).startVisit();
            lastStrategy = null;
            lastSelectedSeed = null;
        } else {
            IClauseSequence seedSeq = selectSeed();
            sequence = mutate(globalState, seedSeq.getCopy());
        }
        numOfQueries++;
        System.out.println(numOfQueries);
        return sequence;
    }
}
