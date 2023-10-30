package de.uni_marburg.schematch.preprocessing.tokenization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class nGramTokenizer extends Tokenizer {
    int n = 3;

    @Override
    public Set<String> tokenize(String str) {
        Set<String> tokens = new HashSet<>();
        if (str.length() < n) {
            tokens.add(str); // TODO: double check small string corner case
        } else {
            for (int i = 0; i <= str.length() - this.n; i++) {
                tokens.add(str.substring(i, i + this.n));
            }
        }
        return tokens;
    }
}
