package gdsmith.composite.gen;

import gdsmith.Randomly;
import gdsmith.cypher.CypherQueryAdapter;
import gdsmith.cypher.ast.Direction;
import gdsmith.cypher.ast.INodeIdentifier;
import gdsmith.cypher.ast.IPattern;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.composite.CompositeGlobalState;
import gdsmith.cypher.standard_ast.*;
import gdsmith.cypher.standard_ast.expr.ConstExpression;
import gdsmith.cypher.schema.IPropertyInfo;
import gdsmith.composite.CompositeSchema;

import java.util.ArrayList;
import java.util.List;

public class CompositeGraphGenerator {
    private static int minNumOfNodes = 200;
    private static int maxNumOfNodes = 200;
    private static double percentOfEdges = 0.001;
    private static List<IPattern> INodesPattern;

    private final CompositeGlobalState globalState;

    // todo(rly): handle Exception
    private ConstExpression generatePropertyValue(Randomly r, CypherType type) throws Exception {
        switch (type){
            case NUMBER: return new ConstExpression(r.getInteger());
            case STRING: return new ConstExpression(r.getString());
            case BOOLEAN: return new ConstExpression(r.getInteger(0, 2) == 0);
            default:
                throw new Exception("undefined type in generator!");
        }
    }

    public CompositeGraphGenerator(CompositeGlobalState globalState){
        this.globalState = globalState;
    }

    public static List<CypherQueryAdapter> createGraph(CompositeGlobalState globalState) throws Exception {
        return new CompositeGraphGenerator(globalState).generateGraph(globalState.getSchema());
    }

    public List<CypherQueryAdapter> generateGraph(CompositeSchema schema) throws Exception {
        List<CypherQueryAdapter> queries = new ArrayList<>();
        IClauseSequenceBuilder builder = ClauseSequence.createClauseSequenceBuilder();

        Randomly r = new Randomly();

        // create nodes
        INodesPattern = new ArrayList<>();
        int numOfNodes = r.getInteger(minNumOfNodes, maxNumOfNodes);
        List<CypherSchema.CypherLabelInfo> labels = schema.getLabels();
        for (int i = 0; i < numOfNodes; ++i) {
            Pattern.PatternBuilder.OngoingNode n = new Pattern.PatternBuilder(builder.getIdentifierBuilder()).newNamedNode();
            for (CypherSchema.CypherLabelInfo l : labels) {
                if (r.getBooleanWithRatherLowProbability()) { // choose label
                    n = n.withLabels(new Label(l.getName()));
                    for (IPropertyInfo p : l.getProperties()) {
                        if (r.getBooleanWithRatherLowProbability()) { // choose property
                            n = n.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                        }
                    }
                }
            }
            /*CypherSchema.CypherLabelInfo l = labels.get(r.getInteger(0, labels.size() - 1)); // choose label
            n = n.withLabels(new Label(l.getName()));
            for (IPropertyInfo p : l.getProperties()) {
                if (r.getBooleanWithRatherLowProbability()) { // choose property
                    n = n.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                }
            }*/ //todo
            n = n.withProperties(new Property("id", CypherType.NUMBER, new ConstExpression(i)));
            IPattern pattern = n.build();
            INodesPattern.add(pattern);
            ClauseSequence sequence = (ClauseSequence) ClauseSequence.createClauseSequenceBuilder().CreateClause(pattern).ReturnClause(Ret.createStar()).build();
            StringBuilder sb = new StringBuilder();
            sequence.toTextRepresentation(sb);
            queries.add(new CypherQueryAdapter(sb.toString()));
        }

        // create relations
        List<CypherSchema.CypherRelationTypeInfo> relationTypes = schema.getRelationTypes();
        for (int i = 0; i < numOfNodes; ++i) {
            for (int j = 0; j < numOfNodes; ++j) {
                for (CypherSchema.CypherRelationTypeInfo relationType : relationTypes) {
                    if (r.getInteger(0, 1000000) < percentOfEdges * 1000000) { // choose this type
                        IPattern patternI = INodesPattern.get(i);
                        IPattern patternJ = INodesPattern.get(j);
                        INodeIdentifier nodeI = (INodeIdentifier) patternI.getPatternElements().get(0);
                        INodeIdentifier nodeJ = (INodeIdentifier) patternJ.getPatternElements().get(0);

                        Pattern.PatternBuilder.OngoingRelation rel = new Pattern.PatternBuilder(builder.getIdentifierBuilder())
                                .newRefDefinedNode(nodeI)
                                .newNamedRelation().withType(new RelationType(relationType.getName()));

                        for (IPropertyInfo p : relationType.getProperties()) {
                            if (r.getBooleanWithRatherLowProbability()) { // choose this property
                                rel = rel.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));
                            }
                        }
                        /*IPropertyInfo p = relationType.getProperties().get(r.getInteger(0, relationType.getProperties().size() - 1)); // choose this property
                        rel = rel.withProperties(new Property(p.getKey(), p.getType(), generatePropertyValue(r, p.getType())));*/ //todo

                        int dirChoice = r.getInteger(0, 2); // generate direction
                        Direction dir = (dirChoice == 0) ? Direction.LEFT : Direction.RIGHT; // For generate in Neo4j, ALL relationships should be directed.
                        rel = rel.withDirection(dir);

                        IPattern merge = rel.newNodeRef(nodeJ).build();

                        ClauseSequence sequence = (ClauseSequence) ClauseSequence.createClauseSequenceBuilder()
                                .MatchClause(null, patternI, patternJ).MergeClause(merge).ReturnClause(Ret.createStar()).build();
                        StringBuilder sb = new StringBuilder();
                        sequence.toTextRepresentation(sb);
                        queries.add(new CypherQueryAdapter(sb.toString()));
                    }
                }
            }
        }
        queries.add(new CypherQueryAdapter("MATCH (n) OPTIONAL MATCH (n)-[r]->() RETURN count(n.prop) + count(r.prop)"));
        return queries;
    }
}
