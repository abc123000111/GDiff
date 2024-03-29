package gdsmith.arcadeDB;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.JsonObject;
import gdsmith.DBMSSpecificOptions;
import gdsmith.OracleFactory;
import gdsmith.arcadeDB.oracle.ArcadeDBAlwaysTrueOracle;
import gdsmith.common.oracle.TestOracle;
import gdsmith.cypher.dsl.IQueryGenerator;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Parameters(separators = "=", commandDescription = "ArcadeDB (default port: " + ArcadeDBOptions.DEFAULT_PORT
        + ", default host: " + ArcadeDBOptions.DEFAULT_HOST)
public class ArcadeDBOptions implements DBMSSpecificOptions<ArcadeDBOptions.ArcadeDBOracleFactory> {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 2424; //todo 改


    @Parameter(names = "--path")
    public String EMBEDDED_PATH = "databases/mydb";

    @Parameter(names = "--oracle")
    public List<ArcadeDBOracleFactory> oracles = Arrays.asList(ArcadeDBOracleFactory.ALWAYS_TRUE);

    @Parameter(names = "--host")
    public String host = DEFAULT_HOST;

    @Parameter(names = "--port")
    public int port = DEFAULT_PORT;

    @Parameter(names = "--username")
    public String username = "sqlancer";

    @Parameter(names = "--password")
    public String password = "sqlancer";

    public String getEMBEDDED_PATH() {
        return EMBEDDED_PATH;
    }

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

    public static ArcadeDBOptions parseOptionFromFile(JsonObject jsonObject){
        ArcadeDBOptions options = new ArcadeDBOptions();
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
        if(jsonObject.has("path")){
            options.EMBEDDED_PATH = jsonObject.get("path").getAsString();
        }
        return options;
    }

    @Override
    public List<ArcadeDBOracleFactory> getTestOracleFactory() {
        return oracles;
    }

    @Override
    public IQueryGenerator getQueryGenerator() {
        return null;
    }

    public enum ArcadeDBOracleFactory implements OracleFactory<ArcadeDBGlobalState> {

        ALWAYS_TRUE {

            @Override
            public TestOracle create(ArcadeDBGlobalState globalState) throws SQLException {
                return new ArcadeDBAlwaysTrueOracle(globalState);
            }
        }
    }
}
