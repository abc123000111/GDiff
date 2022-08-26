package gdsmith.tinkerGraph;

import gdsmith.cypher.CypherGlobalState;
import gdsmith.tinkerGraph.gen.TinkerSchemaGenerator;
import gdsmith.tinkerGraph.schema.TinkerSchema;

public class TinkerGlobalState extends CypherGlobalState<TinkerOptions, TinkerSchema> {

    private TinkerSchema TinkerSchema = null;

    public TinkerGlobalState(){
        super();
        System.out.println("new global state");
    }

    @Override
    protected TinkerSchema readSchema() throws Exception {
        if(TinkerSchema == null){
            TinkerSchema = new TinkerSchemaGenerator(this).generateSchema();
        }
        return TinkerSchema;
    }
}
