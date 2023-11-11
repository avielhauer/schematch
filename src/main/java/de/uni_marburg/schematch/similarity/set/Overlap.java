package de.uni_marburg.schematch.similarity.set;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.utils.SetUtils;

import java.util.Set;

public class Overlap<T> implements SimilarityMeasure<Set<T>> {
    @Override
    public float compare(Set<T> source, Set<T> target) {
        float score = 0;
        int minSize = Math.min(source.size(), target.size());
        Set<T> intersection = SetUtils.intersection(source, target);
        if (minSize > 0) {
            score = (float) intersection.size() / minSize;
        }
        return score;
    }
}
