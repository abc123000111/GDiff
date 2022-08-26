package gdsmith.tinkerGraph;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.JsonObject;
import gdsmith.DBMSSpecificOptions;
import gdsmith.OracleFactory;
import gdsmith.common.oracle.TestOracle;
import gdsmith.cypher.dsl.IQueryGenerator;
import gdsmith.tinkerGraph.oracle.TinkerSmithCrashOracle;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "Tinker (default port: " + TinkerOptions.DEFAULT_PORT
        + ", default host: " + TinkerOptions.DEFAULT_HOST)
public class TinkerOptions implements DBMSSpecificOptions<TinkerOptions.TinkerOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8182; //todo 改

    public static TinkerOptions parseOptionFromFile(JsonObject jsonObject){
        TinkerOptions options = new TinkerOptions();
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
    public List<TinkerOracleFactory> oracles = Arrays.asList(TinkerOracleFactory.RANDOM_CRASH);

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
    public String username = "Tinker";

    @Parameter(names = "--password")
    public String password = "sqlancer";

    @Override
    public List<TinkerOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator getQueryGenerator() {
        return null;
    }

    public enum TinkerOracleFactory implements OracleFactory<TinkerGlobalState> {

        RANDOM_CRASH {

            @Override
            public TestOracle create(TinkerGlobalState globalState) throws SQLException {
                return new TinkerSmithCrashOracle(globalState);
            }
        },
    }
}
