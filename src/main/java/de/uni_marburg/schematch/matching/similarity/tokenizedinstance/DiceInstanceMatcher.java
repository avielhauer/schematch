package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.Dice;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DiceInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public DiceInstanceMatcher() {
        super(new Dice<>());
    }
}
