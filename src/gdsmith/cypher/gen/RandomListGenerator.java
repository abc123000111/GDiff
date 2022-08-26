package gdsmith.cypher.gen;

import gdsmith.cypher.ast.IExpression;
import gdsmith.cypher.ast.IRet;
import gdsmith.cypher.ast.analyzer.IUnwindAnalyzer;
import gdsmith.cypher.dsl.BasicListGenerator;
import gdsmith.cypher.dsl.IIdentifierBuilder;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.standard_ast.CypherType;
import gdsmith.cypher.standard_ast.Ret;

public class RandomListGenerator<S extends CypherSchema<?,?>> extends BasicListGenerator<S> {
    private boolean overrideOld;
    public RandomListGenerator(S schema, IIdentifierBuilder identifierBuilder, boolean overrideOld) {
        super(schema, identifierBuilder);
        this.overrideOld = overrideOld;
    }

    @Override
    public IRet generateList(IUnwindAnalyzer unwindAnalyzer, IIdentifierBuilder identifierBuilder, S schema) {
        //todo
        if(unwindAnalyzer.getListAsAliasRet()!=null && !overrideOld){
            return unwindAnalyzer.getListAsAliasRet();
        }
        IExpression listExpression = new RandomExpressionGenerator<>(unwindAnalyzer, schema).generateListWithBasicType(2, CypherType.NUMBER);
        return Ret.createNewExpressionAlias(identifierBuilder, listExpression);
    }
}
