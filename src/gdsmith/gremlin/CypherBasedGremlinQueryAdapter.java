package gdsmith.gremlin;

import gdsmith.GlobalState;
import gdsmith.common.query.ExpectedErrors;
import gdsmith.common.query.GDSmithResultSet;
import gdsmith.common.query.Query;
import gdsmith.cypher.CypherConnection;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.opencypher.gremlin.client.CypherGremlinClient;

import java.util.List;
import java.util.Map;

public class CypherBasedGremlinQueryAdapter extends Query<CypherConnection> {

    private final String gremlinQuery;

    public CypherBasedGremlinQueryAdapter(String cypherQuery) {
        this.gremlinQuery = CypherGremlinTranslater.translate(cypherQuery);
    }

    private String canonicalizeString(String s) {
        if (s.endsWith(";")) {
            return s;
        } else if (!s.contains("--")) {
            return s + ";";
        } else {
            // query contains a comment
            return s;
        }
    }

    @Override
    public String getLogString() {
        return getQueryString();
    }

    @Override
    public String getQueryString() {
        return gremlinQuery;
    }

    @Override
    public String getUnterminatedQueryString() {
        return canonicalizeString(gremlinQuery);
    }

    @Override
    public boolean couldAffectSchema() {
        return false;
    }

    @Override
    public <G extends GlobalState<?, ?, CypherConnection>> boolean execute(G globalState, String... fills) throws Exception {
        System.out.println(gremlinQuery);
        globalState.getConnection().executeStatement(gremlinQuery);
        return true;
    }

    @Override
    public <G extends GlobalState<?, ?, CypherConnection>> List<GDSmithResultSet> executeAndGet(G globalState, String... fills) throws Exception {
        return globalState.getConnection().executeStatementAndGet(gremlinQuery);
    }

    @Override
    public ExpectedErrors getExpectedErrors() {
        //todo complete
        return null;
    }
}
