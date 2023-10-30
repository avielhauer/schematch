package de.uni_marburg.schematch.similarity.string;

import de.uni_marburg.schematch.similarity.SimilarityMeasure;

public class LongestCommonSubsequence implements SimilarityMeasure<String> {
    @Override
    public float compare(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        int LCS = new org.apache.commons.text.similarity.LongestCommonSubsequence().apply(s1, s2);
        return (float) LCS / maxLength;
    }
}
