package gdsmith.composite;

import gdsmith.common.query.GDSmithResultSet;
import gdsmith.cypher.CypherConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class CompositeConnection extends CypherConnection {

    private List<CypherConnection> connections;
    public static final Long TIMEOUT = 60000L;
    public CompositeConnection(List<CypherConnection> connections){
        this.connections = connections;
    }
    public static final int times = 3;

    @Override
    public String getDatabaseVersion() {
        return "composite";
    }

    @Override
    public void close() throws Exception {
        for(CypherConnection connection : connections){
            connection.close();
        }
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        for(CypherConnection connection : connections){
            connection.executeStatement(arg);
        }
    }

    @Override
    public List<GDSmithResultSet> executeStatementAndGet(String arg) throws Exception{
        List<GDSmithResultSet> results = new ArrayList<>();
        for(CypherConnection connection : connections){
            List<GDSmithResultSet> result = connection.executeStatementAndGet(arg);
            if(result == null || result.get(0) == null){
                results.add(null);
                throw new Exception("a specific database failed"); // todo
            }
            results.addAll(result);
        }
        return results;
    }

    @Override
    public List<Long> executeStatementAndGetTime(String arg) throws Exception {
        List<Long> timeList1 = new ArrayList<>();
        List<Long> timeList2 = new ArrayList<>();
        final ExecutorService exec = Executors.newFixedThreadPool(1);

        for (int i = 0; i < times; i++) {
            Long startTime1 = System.currentTimeMillis();
            Callable<Long> call = () -> {
                connections.get(0).executeStatement(arg);
                return System.currentTimeMillis();
            };
            Long endTime1;
            try {
                Future<Long> future = exec.submit(call);
                endTime1 = future.get(TIMEOUT * 3 / 2, TimeUnit.MILLISECONDS) - startTime1;
            } catch (TimeoutException ex) {
                endTime1 = TIMEOUT + 1L;
                System.out.println(ex);
            } catch (Exception e) {
                if (e.getMessage().contains("JedisDataException: Query timed out") || e.getMessage().contains("ClientException: The transaction has been terminated") || e.getMessage().contains("TransientException: Transaction was asked to abort")) {
                    endTime1 = TIMEOUT + 1L;
                    System.out.println(e);
                } else {
                    endTime1 = -1L;
                    System.out.println("Error!");
                    e.printStackTrace();
                }
            }

            Long startTime2 = System.currentTimeMillis();
            call = () -> {
                connections.get(1).executeStatement(arg);
                return System.currentTimeMillis();
            };
            Long endTime2;
            try {
                Future<Long> future = exec.submit(call);
                endTime2 = future.get(TIMEOUT * 3 / 2, TimeUnit.MILLISECONDS) - startTime2;
            } catch (TimeoutException ex) {
                endTime2 = TIMEOUT + 1L;
                System.out.println(ex);
            } catch (Exception e) {
                if (e.getMessage().contains("JedisDataException: Query timed out") || e.getMessage().contains("ClientException: The transaction has been terminated") || e.getMessage().contains("TransientException: Transaction was asked to abort")) {
                    endTime2 = TIMEOUT + 1L;
                    System.out.println(e);
                } else {
                    endTime2 = -1L;
                    System.out.println("Error!");
                    e.printStackTrace();
                }
            }

            timeList1.add(endTime1);
            timeList2.add(endTime2);
            System.out.println(connections.get(0).getDatabaseVersion() + ": " + endTime1 + "; " + connections.get(1).getDatabaseVersion()+": " + endTime2);
        }
        exec.shutdown();
        Collections.sort(timeList1);
        Collections.sort(timeList2);
        return Arrays.asList(timeList1.get(1), timeList2.get(1));
    }
}
