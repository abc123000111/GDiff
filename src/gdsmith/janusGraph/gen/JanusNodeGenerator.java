package gdsmith.janusGraph.gen;

import gdsmith.cypher.CypherQueryAdapter;
import gdsmith.janusGraph.JanusGlobalState;

public class JanusNodeGenerator {

    private final JanusGlobalState globalState;
    public JanusNodeGenerator(JanusGlobalState globalState){
        this.globalState = globalState;
    }

    public static CypherQueryAdapter createNode(JanusGlobalState globalState){
        return new JanusNodeGenerator(globalState).generateCreate();
    }

    public CypherQueryAdapter generateCreate(){
        return new CypherQueryAdapter("CREATE (p:Person{id: 1})");
    }
}
