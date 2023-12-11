package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.LongestCommonSubsequence;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class LongestCommonSubsequenceMatcher extends LabelSimilarityMatcher {
    public LongestCommonSubsequenceMatcher() {
        super(new LongestCommonSubsequence());
    }
}
