package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.Hamming;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class HammingMatcher extends LabelSimilarityMatcher {
    public HammingMatcher() {
        super(new Hamming());
    }
}
