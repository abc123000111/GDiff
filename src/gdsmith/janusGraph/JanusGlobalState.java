package gdsmith.janusGraph;

import gdsmith.cypher.CypherGlobalState;
import gdsmith.janusGraph.gen.JanusSchemaGenerator;
import gdsmith.janusGraph.schema.JanusSchema;

public class JanusGlobalState extends CypherGlobalState<JanusOptions, JanusSchema> {

    private JanusSchema JanusSchema = null;

    public JanusGlobalState(){
        super();
        System.out.println("new global state");
    }

    @Override
    protected JanusSchema readSchema() throws Exception {
        if(JanusSchema == null){
            JanusSchema = new JanusSchemaGenerator(this).generateSchema();
        }
        return JanusSchema;
    }
}
