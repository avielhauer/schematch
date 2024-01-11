package de.uni_marburg.schematch.matching.similarity.instance;

import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matching.Matcher;
import org.apache.commons.lang3.NotImplementedException;

public abstract class InstanceSimilarityMatcher extends TablePairMatcher {
    @Override
    public float[][] match(TablePair tablePair) {
        throw new NotImplementedException("No instance matchers yet, use TokenizedInstanceSimilarityMatcher instead.");
    }
}
