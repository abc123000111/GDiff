package gdsmith.redisGraph;

import com.redislabs.redisgraph.RedisGraphContext;
import com.redislabs.redisgraph.RedisGraphTransaction;
import com.redislabs.redisgraph.impl.api.RedisGraph;
import gdsmith.common.query.GDSmithResultSet;
import gdsmith.composite.CompositeConnection;
import gdsmith.cypher.CypherConnection;
import org.neo4j.driver.Session;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;

public class RedisGraphConnection extends CypherConnection {

    private final JedisPooled graph;
    private String graphName;

    public RedisGraphConnection(JedisPooled graph, String graphName){
         this.graph = graph;
         this.graphName = graphName;
    }


    @Override
    public String getDatabaseVersion() {
        return "redisgraph";
    }

    @Override
    public void close() throws Exception {
        graph.close();
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        graph.graphQuery(graphName, arg, CompositeConnection.TIMEOUT);
    }

    @Override
    public List<GDSmithResultSet> executeStatementAndGet(String arg) throws Exception{
        return Arrays.asList(new GDSmithResultSet(graph.graphQuery(graphName, arg)));
    }
}
