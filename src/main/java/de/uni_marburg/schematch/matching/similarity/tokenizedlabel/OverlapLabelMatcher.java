package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.similarity.set.Overlap;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OverlapLabelMatcher extends TokenizedLabelSimilarityMatcher {
    public OverlapLabelMatcher() {
        super(new Overlap<>());
    }
}
