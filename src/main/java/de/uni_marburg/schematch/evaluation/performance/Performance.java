package de.uni_marburg.schematch.evaluation.performance;

import lombok.Data;

import java.util.SortedMap;
import java.util.TreeMap;

@Data
public class Performance {
    private float globalScore;
    private SortedMap<Integer, Float> sourceAttributeScores;
    private SortedMap<Integer, Float> targetAttributeScores;

    public Performance(float globalScore) {
        this.globalScore = globalScore;
        sourceAttributeScores = new TreeMap<>();
        targetAttributeScores = new TreeMap<>();
    }

    public void addToGlobalScore(float f) {
        this.globalScore += f;
    }

    public void addSourceAttributeScore(int attributeIndex, float score) {
        sourceAttributeScores.put(attributeIndex, score);
    }

    public void addTargetAttributeScore(int attributeIndex, float score) {
        targetAttributeScores.put(attributeIndex, score);
    }
}
