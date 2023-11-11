package de.uni_marburg.schematch.similarity.set;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import de.uni_marburg.schematch.utils.SetUtils;

import java.util.Set;

public class Dice<T> implements SimilarityMeasure<Set<T>> {
    @Override
    public float compare(Set<T> source, Set<T> target) {
        int sizeSource = source.size();
        int sizeTarget = target.size();
        Set<T> intersection = SetUtils.intersection(source, target);
        int sizeIntersection = intersection.size();
        return (float) 2 * sizeIntersection / (sizeSource + sizeTarget);
    }
}
