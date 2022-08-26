package gdsmith.neo4j.oracle;

import gdsmith.cypher.oracle.NoRecOracle;
import gdsmith.neo4j.Neo4jGlobalState;
import gdsmith.neo4j.schema.Neo4jSchema;

public class Neo4jNoRecOracle extends NoRecOracle<Neo4jGlobalState, Neo4jSchema> {
    public Neo4jNoRecOracle(Neo4jGlobalState globalState) {
        super(globalState);
    }
}
