package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.Overlap;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class OverlapInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public OverlapInstanceMatcher() {
        super(new Overlap<>());
    }
}
