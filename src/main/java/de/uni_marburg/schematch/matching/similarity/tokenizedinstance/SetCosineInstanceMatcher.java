package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.SetCosine;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class SetCosineInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public SetCosineInstanceMatcher() {
        super(new SetCosine<>());
    }
}
