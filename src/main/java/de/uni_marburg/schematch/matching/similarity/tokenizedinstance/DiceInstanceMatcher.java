package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.similarity.set.Dice;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiceInstanceMatcher extends TokenizedInstanceSimilarityMatcher {
    public DiceInstanceMatcher() {
        super(new Dice<>());
    }
}
