package gdsmith.tinkerGraph.oracle;

import gdsmith.common.oracle.TestOracle;
import gdsmith.common.query.GDSmithResultSet;
import gdsmith.cypher.CypherQueryAdapter;
import gdsmith.cypher.ast.IClauseSequence;
import gdsmith.cypher.gen.RandomQueryGenerator;
import gdsmith.cypher.schema.CypherSchema;
import gdsmith.cypher.schema.IPropertyInfo;
import gdsmith.tinkerGraph.TinkerGlobalState;
import gdsmith.tinkerGraph.schema.TinkerSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinkerSmithCrashOracle implements TestOracle {

    private final TinkerGlobalState globalState;
    private RandomQueryGenerator<TinkerSchema, TinkerGlobalState> randomQueryGenerator;
    private int[] numOfNonEmptyQueries;
    private int[] numOfTotalQueries;

    public TinkerSmithCrashOracle(TinkerGlobalState globalState){
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.randomQueryGenerator = new RandomQueryGenerator<TinkerSchema, TinkerGlobalState>();
        numOfNonEmptyQueries = new int[]{0,0,0,0,0};
        numOfTotalQueries = new int[]{0,0,0,0,0};
    }

    @Override
    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        IClauseSequence sequence = randomQueryGenerator.generateQuery(globalState);
        if (sequence.getClauseList().size() <= 8) {
            StringBuilder sb = new StringBuilder();
            sequence.toTextRepresentation(sb);
            System.out.println(sb);
            GDSmithResultSet r = null;
            int resultLength = 0;
            try {
                r = globalState.executeStatementAndGet(new CypherQueryAdapter(sb.toString())).get(0);
                resultLength = r.getRowNum();
            } catch (CompletionException e) {
                System.out.println("该Cypher查询不支持转换为Gremlin！");
                System.out.println(e.getMessage());
            }

            boolean isBugDetected = false;
            //todo 上层通过抛出的异常检测是否通过，所以这里可以捕获并检测异常的类型，可以计算一些统计数据，然后重抛异常

            List<CypherSchema.CypherLabelInfo> labels = globalState.getSchema().getLabels();
            List<CypherSchema.CypherRelationTypeInfo> relations = globalState.getSchema().getRelationTypes();
            if (resultLength > 0) {
                randomQueryGenerator.addExecutionRecord(sequence, isBugDetected, resultLength);//添加seed

                List<String> coveredProperty = new ArrayList<>();
                Pattern allProps = Pattern.compile("(\\.)(k\\d+)(\\))");
                Matcher matcher = allProps.matcher(sb);
                while (matcher.find()) {
                    if (!coveredProperty.contains(matcher.group(2))) {
                        coveredProperty.add(matcher.group(2));
                    }
                }

                for (String name : coveredProperty) {
                    found:
                    {
                        for (CypherSchema.CypherLabelInfo label : labels) {
                            List<IPropertyInfo> props = label.getProperties();
                            for (IPropertyInfo prop : props) {
                                if (Objects.equals(prop.getKey(), name)) {
                                    ((CypherSchema.CypherPropertyInfo) prop).addFreq();
                                    break found;
                                }
                            }
                        }
                        for (CypherSchema.CypherRelationTypeInfo relation : relations) {
                            List<IPropertyInfo> props = relation.getProperties();
                            for (IPropertyInfo prop : props) {
                                if (Objects.equals(prop.getKey(), name)) {
                                    ((CypherSchema.CypherPropertyInfo) prop).addFreq();
                                    break found;
                                }
                            }
                        }
                    }
                }
            }

            /*if (sequence.getClauseList().size() >= 3 && sequence.getClauseList().size() <= 7) {
                numOfTotalQueries[sequence.getClauseList().size() - 3]++;
                if (resultLength > 0) {
                    numOfNonEmptyQueries[sequence.getClauseList().size() - 3]++;
                }
                System.out.println(sequence.getClauseList().size() + " rate is: " + numOfNonEmptyQueries[sequence.getClauseList().size() - 3] * 1.0 / numOfTotalQueries[sequence.getClauseList().size() - 3]);
            }

            for (CypherSchema.CypherLabelInfo label: labels) {
                List<IPropertyInfo> props = label.getProperties();
                for (IPropertyInfo prop: props) {
                    System.out.println(label.getName() + ":" + prop.getKey() + ":" + ((CypherSchema.CypherPropertyInfo)prop).getFreq());
                }
            }
            for (CypherSchema.CypherRelationTypeInfo relation: relations) {
                List<IPropertyInfo> props = relation.getProperties();
                for (IPropertyInfo prop: props) {
                    System.out.println(relation.getName() + ":" + prop.getKey() + ":" + ((CypherSchema.CypherPropertyInfo)prop).getFreq());
                }
            }*/
        }
    }
}
