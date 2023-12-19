package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Setter
public abstract class TokenizedTablePairMatcher extends TablePairMatcher {
    protected Tokenizer tokenizer;

    @Override
    public String toString() {
        return super.toString() + "___" + tokenizer.toString();
    }
}
