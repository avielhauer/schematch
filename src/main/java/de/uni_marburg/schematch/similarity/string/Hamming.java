package de.uni_marburg.schematch.similarity.string;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import org.apache.commons.text.similarity.HammingDistance;

public class Hamming implements SimilarityMeasure<String> {
    @Override
    public float compare(String s1, String s2) {
        int minLength = Math.min(s1.length(), s2.length());
        s1 = s1.substring(0, minLength);
        s2 = s2.substring(0, minLength);
        // TODO: gives a perfect score if s1 is substring of s2 (e.g., "hell" and "hello")
        // TODO: divide by maxLength instead?
        return 1 - (new HammingDistance().apply(s1, s2).floatValue())/minLength;
    }
}
