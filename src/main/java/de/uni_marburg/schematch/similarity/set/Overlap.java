package de.uni_marburg.schematch.similarity.set;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;

import java.util.Set;

public class Overlap<T> implements SimilarityMeasure<Set<T>> {
    @Override
    public float compare(Set<T> source, Set<T> target) {
        float score = 0;
        int minSize = Math.min(source.size(), target.size());
        source.retainAll(target); // source modified to intersection of source and target
        if (minSize > 0) {
            score = (float) source.size() / minSize;
        }
        return score;
    }
}
