package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.similarity.string.Cosine;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CosineMatcher extends LabelSimilarityMatcher {
    public CosineMatcher() {
        super(new Cosine());
    }
}
