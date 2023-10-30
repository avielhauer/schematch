package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.similarity.set.SetCosine;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetCosineLabelMatcher extends TokenizedLabelSimilarityMatcher {
    public SetCosineLabelMatcher() {
        super(new SetCosine<>());
    }
}
