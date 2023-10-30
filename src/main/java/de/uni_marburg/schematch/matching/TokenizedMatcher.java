package de.uni_marburg.schematch.matching;

import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class TokenizedMatcher extends Matcher {
    private Tokenizer tokenizer;
}
