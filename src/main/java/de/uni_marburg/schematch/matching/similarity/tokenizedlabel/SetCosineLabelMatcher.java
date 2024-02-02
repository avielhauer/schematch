package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.similarity.set.SetCosine;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class SetCosineLabelMatcher extends TokenizedLabelSimilarityMatcher {
    public SetCosineLabelMatcher() {
        super(new SetCosine<>());
    }
}
