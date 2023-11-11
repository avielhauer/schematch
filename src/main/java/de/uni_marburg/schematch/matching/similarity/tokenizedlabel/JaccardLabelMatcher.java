package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.similarity.set.Jaccard;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class JaccardLabelMatcher extends TokenizedLabelSimilarityMatcher {
    public JaccardLabelMatcher() {
        super(new Jaccard<String>());
    }
}
