package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.Jaccard;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class JaccardInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public JaccardInstanceMatcher() {
        super(new Jaccard<>());
    }
}
