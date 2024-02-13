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
        StringBuilder sb = new StringBuilder();
        addColumns(sb, determinant);
        sb.append(" --> ");
        sb.append(dependant.getTable().getName());
        sb.append(".csv.");
        sb.append(dependant.getLabel());
        sb.append(" (pdep ");
        sb.append(pdepTuple.pdep);
        sb.append(", ");
        sb.append(pdepTuple.gpdep);
        sb.append(")");
        return sb.toString();
    }

}
