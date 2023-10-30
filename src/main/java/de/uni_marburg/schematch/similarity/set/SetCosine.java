package de.uni_marburg.schematch.similarity.set;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SetCosine<T> implements SimilarityMeasure<Set<T>> {
    @Override
    public float compare(Set<T> source, Set<T> target) {
        Map<CharSequence, Integer> sourceVector = new HashMap<>();
        Map<CharSequence, Integer> targetVector = new HashMap<>();

        for (T element : source) {
            String elementStr = element.toString(); // TODO: optimize
            sourceVector.put(elementStr, sourceVector.getOrDefault(elementStr, 0) + 1);
        }

        for (T element : target) {
            String elementStr = element.toString(); // TODO: optimize
            targetVector.put(elementStr, targetVector.getOrDefault(elementStr, 0) + 1);
        }

        return new CosineSimilarity().cosineSimilarity(sourceVector, targetVector).floatValue();
    }
}
