package gdsmith;

import gdsmith.cypher.dsl.IQueryGenerator;

import java.util.List;

public interface DBMSSpecificOptions<F extends OracleFactory<? extends GlobalState<?, ?, ?>>> {

    List<F> getTestOracleFactory();

    IQueryGenerator getQueryGenerator();

}
