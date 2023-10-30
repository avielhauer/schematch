package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.SetCosine;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetCosineInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public SetCosineInstanceMatcher() {
        super(new SetCosine<>());
    }
}
