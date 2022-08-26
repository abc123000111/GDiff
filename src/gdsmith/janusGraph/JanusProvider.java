package gdsmith.janusGraph;

import com.google.gson.JsonObject;
import gdsmith.AbstractAction;
import gdsmith.MainOptions;
import gdsmith.agensGraph.gen.AgensGraphGraphGenerator;
import gdsmith.common.log.LoggableFactory;
import gdsmith.cypher.*;
import gdsmith.cypher.mutation.GraphMutator;
import gdsmith.cypher.standard_ast.ClauseSequence;
import gdsmith.janusGraph.gen.JanusNodeGenerator;
import gdsmith.janusGraph.schema.JanusSchema;
import gdsmith.janusGraph.gen.JanusGraphGenerator;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;

import java.util.List;

public class JanusProvider extends CypherProviderAdapter<JanusGlobalState, JanusOptions> {
    public JanusProvider() {
        super(JanusGlobalState.class, JanusOptions.class);
    }

    @Override
    public JanusOptions generateOptionsFromConfig(JsonObject config) {
        return JanusOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, JanusOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = JanusOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = JanusOptions.DEFAULT_PORT;
        }

        String url = String.format("bolt://%s:%d", host, port);
        JanusConnection con = new JanusConnection(Cluster.open());
        con.executeStatement("MATCH (n) DETACH DELETE n");
        return con;
    }

    enum Action implements AbstractAction<JanusGlobalState> {
        CREATE(JanusNodeGenerator::createNode);

        private final CypherQueryProvider<JanusGlobalState> cypherQueryProvider;

        //SQLQueryProvider是一个接口，返回SQLQueryAdapter
        Action(CypherQueryProvider<JanusGlobalState> cypherQueryProvider) {
            this.cypherQueryProvider = cypherQueryProvider;
        }

        @Override
        public CypherQueryAdapter getQuery(JanusGlobalState globalState) throws Exception {
            return cypherQueryProvider.getQuery(globalState);
        }
    }

    @Override
    public CypherConnection createDatabase(JanusGlobalState globalState) throws Exception {
       return createDatabaseWithOptions(globalState.getOptions(), globalState.getDbmsSpecificOptions());
    }

    @Override
    public String getDBMSName() {
        return "janusgraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }

    @Override
    protected void checkViewsAreValid(JanusGlobalState globalState) {

    }

    @Override
    public void generateDatabase(JanusGlobalState globalState) throws Exception {
        List<CypherQueryAdapter> queries = JanusGraphGenerator.createGraph(globalState);
        for(CypherQueryAdapter query : queries){
            globalState.executeStatement(query);
        }
    }
}
