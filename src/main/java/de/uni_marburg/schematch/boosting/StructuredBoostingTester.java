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
    static Map<Metric, Map<String, Map<Matcher, Float>>> result = new HashMap<>();
    static List<Matcher> matcherList = new ArrayList<>();
    static StringBuilder header = new StringBuilder(",,,");
    static String sessionId = UUID.randomUUID().toString();

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix, Matcher matcher) {
        ExecutorService executor = Executors.newCachedThreadPool();

        List<Future<FloodingResult>> futureList = new ArrayList<>();
        Future<FloodingResult> original = executor.submit( () -> new FloodingResult("original", "", simMatrix));
        futureList.add(original);

        Future<FloodingResult> simFloodingAFuture = executor.submit( () -> {
            SimMatrixBoosting simFloodingA = new SimFloodingSimMatrixBoosting(1);
            float[][] resultMatrix = simFloodingA.run(matchTask, matchStep, simMatrix, matcher);
            return new FloodingResult("floodingA", "standard", resultMatrix);
        });
        futureList.add(simFloodingAFuture);


        for (Future<FloodingResult> future : futureList) {
            try {
                FloodingResult floodingResult = future.get();
                Map<Metric, Performance> performances = matchTask.getEvaluator().evaluate(floodingResult.getResult());
                String id = floodingResult.getName()+"_"+floodingResult.getConfig();
                for (Metric metric : performances.keySet()){
                    if (!StructuredBoostingTester.result.containsKey(metric)){
                        result.put(metric, new HashMap<String, Map<Matcher, Float>>());
                    }
                    if(!StructuredBoostingTester.result.get(metric).containsKey(id)){
                        StructuredBoostingTester.result.get(metric).put(id, new HashMap<Matcher, Float>());
                    }
                    StructuredBoostingTester.result.get(metric).get(id).put(matcher, performances.get(metric).getGlobalScore());
                }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
        }
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
        result = new HashMap<>();
    }
}

@Data
@AllArgsConstructor
class FloodingResult{
    private String name;
    private String config;
    private float[][] result;
}

