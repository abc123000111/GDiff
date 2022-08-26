package gdsmith.tinkerGraph;

import com.google.gson.JsonObject;
import gdsmith.AbstractAction;
import gdsmith.MainOptions;
import gdsmith.agensGraph.gen.AgensGraphGraphGenerator;
import gdsmith.common.log.LoggableFactory;
import gdsmith.cypher.*;
import gdsmith.cypher.mutation.GraphMutator;
import gdsmith.cypher.standard_ast.ClauseSequence;
import gdsmith.tinkerGraph.gen.TinkerGraphGenerator;
import gdsmith.tinkerGraph.gen.TinkerNodeGenerator;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;

import java.util.List;

public class TinkerProvider extends CypherProviderAdapter<TinkerGlobalState, TinkerOptions> {
    public TinkerProvider() {
        super(TinkerGlobalState.class, TinkerOptions.class);
    }

    @Override
    public TinkerOptions generateOptionsFromConfig(JsonObject config) {
        return TinkerOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, TinkerOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = TinkerOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = TinkerOptions.DEFAULT_PORT;
        }

        String url = String.format("bolt://%s:%d", host, port);
        TinkerConnection con = new TinkerConnection(Cluster.open());
        con.executeStatement("MATCH (n) DETACH DELETE n");
        return con;
    }

    enum Action implements AbstractAction<TinkerGlobalState> {
        CREATE(TinkerNodeGenerator::createNode);

        private final CypherQueryProvider<TinkerGlobalState> cypherQueryProvider;

        //SQLQueryProvider是一个接口，返回SQLQueryAdapter
        Action(CypherQueryProvider<TinkerGlobalState> cypherQueryProvider) {
            this.cypherQueryProvider = cypherQueryProvider;
        }

        @Override
        public CypherQueryAdapter getQuery(TinkerGlobalState globalState) throws Exception {
            return cypherQueryProvider.getQuery(globalState);
        }
    }

    @Override
    public CypherConnection createDatabase(TinkerGlobalState globalState) throws Exception {
        return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "tinkergraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(TinkerGlobalState globalState) {

    }

    @Override
    public void generateDatabase(TinkerGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = TinkerGraphGenerator.createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }
    }
}
