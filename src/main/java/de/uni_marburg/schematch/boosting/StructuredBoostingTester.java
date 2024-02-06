package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.util.concurrent.Future;

public class StructuredBoostingTester implements SimMatrixBoosting{
    private final static Logger log = LogManager.getLogger(StructuredBoostingTester.class);
    static Map<Metric, Map<String, Map<Matcher, Float>>> result = new LinkedHashMap<>();
    static List<Matcher> matcherList = new ArrayList<>();
    static StringBuilder header = new StringBuilder(",,,");
    static String sessionId = UUID.randomUUID().toString();

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix, Matcher matcher) {
        ExecutorService executor = Executors.newFixedThreadPool(32);

        List<Future<FloodingResult>> futureList = new ArrayList<>();
        Future<FloodingResult> original = executor.submit( () -> new FloodingResult("original", "", simMatrix));
        futureList.add(original);

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(1, 5, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:1_num_FD:5_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(1, 10, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:1_num_FD:10_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(5, 5, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:5_num_FD:5_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(5, 10, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:5_num_FD:10_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(100, 10, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:100_num_FD:10_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(100, 5, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:100_num_FD:5_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(1, 5, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:1_num_FD:5_fd_sim:gpdep", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(1, 10, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:1_num_FD:10_fd_sim:1", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(5, 5, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:5_num_FD:5_fd_sim:1", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(5, 10, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:5_num_FD:10_fd_sim:1", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(100, 10, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:100_num_FD:10_fd_sim:1", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test1(100, 5, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test1", "max_it:100_num_FD:5_fd_sim:1", resultMatrix);
        }));


        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test2(1, 5);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test2", "max_it:1_num_FD:5_fd", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test2(1, 10);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test2", "max_it:1_num_FD:10", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test2(5, 5);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test2", "max_it:5_num_FD:5_fd", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test2(5, 10);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test2", "max_it:5_num_FD:10", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test2(100, 10);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test2", "max_it:100_num_FD:10", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test2(100, 5);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test2", "max_it:100_num_FD:5", resultMatrix);
        }));


        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test3(1, true, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test3", "max_it:1_flooder:C_weighting:normal", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test3(5, true, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test3", "max_it:5_flooder:C_weighting:normal", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test3(100, true, false);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test3", "max_it:100_flooder:C_weighting:normal", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test3(1, true, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test3", "max_it:1_flooder:C_weighting:inv", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test3(5, true, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test3", "max_it:5_flooder:C_weighting:inv", resultMatrix);
        }));

        futureList.add(executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new Test3(100, true, true);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("test3", "max_it:100_flooder:C_weighting:inv", resultMatrix);
        }));

        for (Future<FloodingResult> future : futureList) {
            try {
                FloodingResult floodingResult = future.get();
                Map<Metric, Performance> performances = matchTask.getEvaluator().evaluate(floodingResult.getResult());
                String id = floodingResult.getName()+"_"+floodingResult.getConfig();
                for (Metric metric : performances.keySet()){
                    if (!StructuredBoostingTester.result.containsKey(metric)){
                        result.put(metric, new LinkedHashMap<String, Map<Matcher, Float>>());
                    }
                    if(!StructuredBoostingTester.result.get(metric).containsKey(id)){
                        StructuredBoostingTester.result.get(metric).put(id, new LinkedHashMap<Matcher, Float>());
                    }
                    StructuredBoostingTester.result.get(metric).get(id).put(matcher, performances.get(metric).getGlobalScore());
                }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
        }
        executor.shutdown();
        return simMatrix;
    }

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix) {
       throw new RuntimeException("StructuredBoostingTester is not made for implementation into schematch.");
    }

    public static void writeResults(String path, String scenarioName){
        for (Metric metric : result.keySet()){
            boolean editedHeader = false;
            String csvFile = path+sessionId+".csv";
            try (FileWriter writer = new FileWriter(csvFile, true)) {

                StringBuilder content = new StringBuilder("\n"+scenarioName);
                content.append(",");
                content.append(metric);
                content.append(",");
                boolean needComma = false;
                for(String id : result.get(metric).keySet()){
                    if(needComma){
                        content.append(",,");
                    }
                    else {
                        needComma = true;
                    }
                    content.append(id).append(",");
                    for(Matcher matcher : result.get(metric).get(id).keySet()){
                        if(!matcherList.contains(matcher)){
                            matcherList.add(matcher);
                            header.append(matcher).append(",");
                            editedHeader = true;
                        }
                    }
                    for(Matcher matcher : matcherList){
                        content.append(result.get(metric).get(id).get(matcher)).append(",");
                    }
                    content.append("\n");
                }
                if(editedHeader){
                    writer.append(header);
                    writer.append("\n");
                }
                writer.append(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        result = new LinkedHashMap<>();
    }
}

@Data
@AllArgsConstructor
class FloodingResult{
    private String name;
    private String config;
    private float[][] result;
}

