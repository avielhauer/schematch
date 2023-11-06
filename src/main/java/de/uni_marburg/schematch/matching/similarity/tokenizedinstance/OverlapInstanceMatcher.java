package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.Overlap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OverlapInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public OverlapInstanceMatcher() {
        super(new Overlap<>());
    }
}