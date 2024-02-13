package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniqueColumnCombination implements Dependency{
    Collection<Column> columnCombination;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueColumnCombination that = (UniqueColumnCombination) o;
        return Objects.equals(columnCombination, that.columnCombination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnCombination);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addColumns(sb, columnCombination);
        return sb.toString();
    }

}
