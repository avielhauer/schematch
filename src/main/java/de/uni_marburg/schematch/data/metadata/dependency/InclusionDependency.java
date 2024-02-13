package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InclusionDependency implements Dependency{
    Collection<Column> dependant;
    Collection<Column> referenced;

    public Collection<Column> getSubset(){
        return dependant;
    }

    public Collection<Column> getSuperset(){
        return referenced;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addColumns(sb, dependant);
        sb.append(" --> ");
        addColumns(sb, referenced);
        return sb.toString();
    }


}
