package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.similarity.set.Dice;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiceLabelMatcher extends TokenizedLabelSimilarityMatcher {
    public DiceLabelMatcher() {
        super(new Dice<>());
    }
}
