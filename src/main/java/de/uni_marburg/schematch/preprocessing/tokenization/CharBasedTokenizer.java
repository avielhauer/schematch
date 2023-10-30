package de.uni_marburg.schematch.preprocessing.tokenization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharBasedTokenizer extends Tokenizer {
    String splitChar;

    @Override
    public Set<String> tokenize(String str) {
        return new HashSet<>(List.of(str.split(splitChar)));
    }
}
