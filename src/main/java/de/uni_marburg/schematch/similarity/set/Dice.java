package de.uni_marburg.schematch.similarity.set;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;

import java.util.Set;

public class Dice<T> implements SimilarityMeasure<Set<T>> {
    @Override
    public float compare(Set<T> source, Set<T> target) {
        int sizeSource = source.size();
        int sizeTarget = target.size();
        source.retainAll(target); // source modified to intersection of source and target
        int sizeIntersection = source.size();
        return (float) 2 * sizeIntersection / (sizeSource + sizeTarget);
    }
}
