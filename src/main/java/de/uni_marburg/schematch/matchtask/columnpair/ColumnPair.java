package de.uni_marburg.schematch.matchtask.columnpair;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnPair {
    private final Column sourceColumn;
    private final Column targetColumn;

    public String toString() {
        return sourceColumn.getLabel() + Configuration.getInstance().getDefaultTablePairSeparator() + targetColumn.getLabel();
    }
}
