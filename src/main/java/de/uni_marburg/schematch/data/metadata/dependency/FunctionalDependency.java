package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.PdepTuple;
import de.uni_marburg.schematch.utils.MetadataUtils;
import lombok.Data;

import java.util.Collection;
import java.util.Objects;

@Data
public class FunctionalDependency implements Dependency{
    Collection<Column> determinant;
    Column dependant;
    PdepTuple pdepTuple;

    public FunctionalDependency(Collection<Column> left, Column right){
        this.determinant = left;
        this.dependant = right;
    }

    public PdepTuple getPdepTuple(){
        if(pdepTuple == null){
            this.setPdepTuple(MetadataUtils.getPdep(this));
        }
        return pdepTuple;
    }

    @Override
    public int hashCode() {
        return Objects.hash(determinant, dependant);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        FunctionalDependency other = (FunctionalDependency) obj;
        return other.getDependant().equals(getDependant()) && other.getDeterminant().equals(getDeterminant());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addColumns(sb, determinant);
        sb.append(" --> ");
        sb.append(dependant.getTable().getName());
        sb.append(".csv.");
        sb.append(dependant.getLabel());
        if(pdepTuple != null){
            sb.append(" (pdep ");
            sb.append(pdepTuple.pdep);
            sb.append(", ");
            sb.append(pdepTuple.gpdep);
            sb.append(")");
        }
        return sb.toString();
    }

}
