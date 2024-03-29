package gdsmith.cypher.mutation;

import gdsmith.Randomly;
import gdsmith.cypher.ast.*;
import gdsmith.cypher.ast.analyzer.IIdentifierAnalyzer;
import gdsmith.cypher.ast.analyzer.INodeAnalyzer;
import gdsmith.cypher.ast.analyzer.IRelationAnalyzer;
import gdsmith.cypher.dsl.ClauseVisitor;
import gdsmith.cypher.dsl.IContext;
import gdsmith.cypher.gen.RandomPatternGenerator;
import gdsmith.cypher.mutation.PropertyAdditionMutator.PropertyAdditionMutatorContext;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.schema.IPropertyInfo;
import gdsmith.cypher.standard_ast.Label;
import gdsmith.cypher.standard_ast.Pattern;
import gdsmith.cypher.standard_ast.RelationType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public class PropertyAdditionMutator<S extends CypherSchema<?,?>> extends ClauseVisitor<PropertyAdditionMutatorContext> implements IClauseMutator {

    private List<IIdentifierAnalyzer> patternElements = new ArrayList<>();
    private S schema;

    public PropertyAdditionMutator(IClauseSequence clauseSequence, S schema) {
        super(clauseSequence, new PropertyAdditionMutatorContext());
        this.schema = schema;
    }

    @Override
    public void mutate() {
        startVisit();
    }

    public static class PropertyAdditionMutatorContext implements IContext{

    }

    @Override
    public void visitMatch(IMatch matchClause, PropertyAdditionMutatorContext context) {
        matchClause.toAnalyzer().getLocalNodeIdentifiers().stream().filter(node->{
            //排除掉所有在之前已经使用过的property，避免多次使用同一property的不同constraint使得empty result显著增加
            return node.getAllPropertiesAvailable(schema).stream().anyMatch(
                    property->{
                        return node.getAllPropertiesInDefChain().stream().noneMatch(
                                declaredProperty->declaredProperty.getKey().equals(property.getKey())
                        );
                    }
            );
        }).forEach(node-> patternElements.add(node));

        matchClause.toAnalyzer().getLocalRelationIdentifiers().stream().filter(relation->{
            //排除掉所有在之前已经使用过的property，避免多次使用同一property的不同constraint使得empty result显著增加
            return relation.getAllPropertiesAvailable(schema).stream().anyMatch(
                    property->{
                        return relation.getAllPropertiesInDefChain().stream().noneMatch(
                                declaredProperty->declaredProperty.getKey().equals(property.getKey())
                        );
                    }
            );
        }).forEach(relation-> patternElements.add(relation));
    }


    @Override
    public void postProcessing(PropertyAdditionMutatorContext context) {
        Randomly randomly = new Randomly();
        if(patternElements.size() == 0){
            return;
        }

        IIdentifierAnalyzer identifier = patternElements.get(randomly.getInteger(0, patternElements.size()));

        if(identifier instanceof INodeAnalyzer){
            INodeAnalyzer nodeAnalyzer = (INodeAnalyzer) identifier;
            //排除掉所有在之前已经使用过的property，避免多次使用同一property的不同constraint使得empty result显著增加
            List<IPropertyInfo> possiblePropertyInfo = nodeAnalyzer.getAllPropertiesAvailable(schema).stream().filter(
                property->{
                    return nodeAnalyzer.getAllPropertiesInDefChain().stream().noneMatch(
                      declaredProperty->declaredProperty.getKey().equals(property.getKey())
                    );
                }
            ).collect(Collectors.toList());
            if(possiblePropertyInfo.size() == 0){
                throw new RuntimeException();
            }
            IPropertyInfo selectedPropertyInfo = possiblePropertyInfo.get(randomly.getInteger(0, possiblePropertyInfo.size()));
            //todo: more
            return;
        }
        else if(identifier instanceof IRelationAnalyzer){
            IRelationAnalyzer relationAnalyzer = (IRelationAnalyzer) identifier;
            //排除掉所有在之前已经使用过的property，避免多次使用同一property的不同constraint使得empty result显著增加
            List<IPropertyInfo> possiblePropertyInfo = relationAnalyzer.getAllPropertiesAvailable(schema).stream().filter(
                    property->{
                        return relationAnalyzer.getAllPropertiesInDefChain().stream().noneMatch(
                                declaredProperty->declaredProperty.getKey().equals(property.getKey())
                        );
                    }
            ).collect(Collectors.toList());
            if(possiblePropertyInfo.size() == 0){
                throw new RuntimeException();
            }
            IPropertyInfo selectedPropertyInfo = possiblePropertyInfo.get(randomly.getInteger(0, possiblePropertyInfo.size()));
            //todo: more
            return;
        }
        throw new RuntimeException();
    }
}
