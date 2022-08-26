package gdsmith.janusGraph;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.JsonObject;
import gdsmith.DBMSSpecificOptions;
import gdsmith.OracleFactory;
import gdsmith.common.oracle.TestOracle;
import gdsmith.cypher.dsl.IQueryGenerator;
import gdsmith.janusGraph.oracle.JanusSmithCrashOracle;
import gdsmith.janusGraph.JanusOptions.JanusOracleFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "Janus (default port: " + JanusOptions.DEFAULT_PORT
        + ", default host: " + JanusOptions.DEFAULT_HOST)
public class JanusOptions implements DBMSSpecificOptions<JanusOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8182; //todo 改

    public static JanusOptions parseOptionFromFile(JsonObject jsonObject){
        JanusOptions options = new JanusOptions();
        if(jsonObject.has("host")){
            options.host = jsonObject.get("host").getAsString();
        }
        if(jsonObject.has("port")){
            options.port = jsonObject.get("port").getAsInt();
        }
        if(jsonObject.has("username")){
            options.username = jsonObject.get("username").getAsString();
        }
        if(jsonObject.has("password")){
            options.password = jsonObject.get("password").getAsString();
        }
        return options;
    }

    @Parameter(names = "--oracle")
    public List<JanusOracleFactory> oracles = Arrays.asList(JanusOracleFactory.RANDOM_CRASH);

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    @Parameter(names = "--host")
    public String host = DEFAULT_HOST;

    @Parameter(names = "--port")
    public int port = DEFAULT_PORT;

    @Parameter(names = "--username")
    public String username = "Janus";

    @Parameter(names = "--password")
    public String password = "sqlancer";

    @Override
    public List<JanusOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator getQueryGenerator() {
        return null;
    }

    public enum JanusOracleFactory implements OracleFactory<JanusGlobalState> {

        RANDOM_CRASH {

            @Override
            public TestOracle create(JanusGlobalState globalState) throws SQLException {
                return new JanusSmithCrashOracle(globalState);
            }
        },
    }
}
