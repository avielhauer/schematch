package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.PdepTuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
public class FunctionalDependency implements Dependency{
    Collection<Column> determinant;
    Column dependant;
    PdepTuple pdepTuple;

    public FunctionalDependency(Collection<Column> left, Column right){
        this.determinant = left;
        this.dependant = right;
    }

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
}
