package de.uni_marburg.schematch.data;

import de.uni_marburg.schematch.preprocessing.tokenization.Tokenizer;
import de.uni_marburg.schematch.data.metadata.Datatype;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Column {
    private Table table;
    private final String label;
    private Datatype datatype;
    /**
     * All values are represented as String.
     * See {@link #datatype} for more specific interpretation.
     */
    private final List<String> values;
    private Map<Tokenizer, Set<String>> tokenizedLabel;
    private Map<Tokenizer, Set<String>> tokenizedValues;

    public Column(String label, List<String> values) {
        this.label = label;
        this.values = values;
        this.datatype = null;
        this.tokenizedLabel = new HashMap<>();
        this.tokenizedValues = new HashMap<>();
    }

    @Override
    public int hashCode() {
        return (this.label + "___" + this.table.getName()).hashCode();
    }

    @Override
    public String toString() {
        return this.label + "___" + this.table.getName();
    }

    public Datatype getDatatype() {
        if (this.datatype == null) {
            this.datatype = Datatype.determineDatatype(this);
        }
        return this.datatype;
    }

    public Set<String> getLabelTokens(Tokenizer tokenizer) {
        if (tokenizedLabel.get(tokenizer) == null) {
            this.addLabelTokens(tokenizer, tokenizer.tokenize(this.label));
        }
        return tokenizedLabel.get(tokenizer);
    }

    public Set<String> getValuesTokens(Tokenizer tokenizer) {
        if (tokenizedValues.get(tokenizer) == null) {
            this.addValuesTokens(tokenizer, tokenizer.tokenize(this.values));
        }
        return tokenizedValues.get(tokenizer);
    }

    public void addLabelTokens(Tokenizer tokenizer, Set<String> tokens) {
        this.tokenizedLabel.put(tokenizer, tokens);
    }

    public void addValuesTokens(Tokenizer tokenizer, Set<String> tokens) {
        this.tokenizedValues.put(tokenizer, tokens);
    }
}
