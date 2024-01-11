package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.Levenshtein;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class LevenshteinMatcher extends LabelSimilarityMatcher {
    public LevenshteinMatcher() {
        super(new Levenshtein());
    }
}
