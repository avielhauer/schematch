package de.uni_marburg.schematch.similarity.string;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

public class JaroWinkler implements SimilarityMeasure<String> {
    @Override
    public float compare(String s1, String s2) {
        return new JaroWinklerSimilarity().apply(s1, s2).floatValue();
    }
}
