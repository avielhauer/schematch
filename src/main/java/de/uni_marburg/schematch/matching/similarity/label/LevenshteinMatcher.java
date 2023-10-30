package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.similarity.string.Levenshtein;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class LevenshteinMatcher extends LabelSimilarityMatcher {
    public LevenshteinMatcher() {
        super(new Levenshtein());
    }
}
