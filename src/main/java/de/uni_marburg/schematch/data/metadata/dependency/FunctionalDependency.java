package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalDependency implements Dependency{
    Collection<Column> determinant;
    Column dependant;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Column column : determinant) {
            sb.append(column.getLabel());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length()); // Remove the trailing ", "
        sb.append("]");
        sb.append(" --> ");
        sb.append(dependant.getLabel());
        return sb.toString();
    }

    public static int getSortingCriteria(FunctionalDependency fd) {
        return fd.getDeterminant().size();
    }

    public Collection<Column> getDependentColumns() {
        return List.of(dependant);
    }

    public Collection<Collection<Column>> getDeterminantColumn() {
        return List.of(determinant);
    }
}
