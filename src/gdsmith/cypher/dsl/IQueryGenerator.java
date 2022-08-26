package gdsmith.cypher.dsl;

import gdsmith.cypher.CypherGlobalState;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.schema.CypherSchema;

public interface IQueryGenerator <S extends CypherSchema<G,?>,G extends CypherGlobalState<?,S>>{
    IClauseSequence generateQuery(G globalState);
    void addExecutionRecord(IClauseSequence clauseSequence, boolean isBugDetected, int resultSize);

}
