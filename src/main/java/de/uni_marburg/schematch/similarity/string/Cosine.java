package de.uni_marburg.schematch.similarity.string;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Cosine implements SimilarityMeasure<String> {
    @Override
    public float compare(String s1, String s2) {
        // https://stackoverflow.com/a/45107875
        Map<CharSequence, Integer> leftVector =
                Arrays.stream(s1.split(""))
                        .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
        Map<CharSequence, Integer> rightVector =
                Arrays.stream(s2.split(""))
                        .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
        return new CosineSimilarity().cosineSimilarity(leftVector, rightVector).floatValue();
    }
}
