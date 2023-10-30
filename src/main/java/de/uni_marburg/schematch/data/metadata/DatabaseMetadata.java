package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseMetadata {
    Collection<UniqueColumnCombination> uccs;
    Collection<FunctionalDependency> fds;
    Collection<InclusionDependency> inds;
    final Map<Column, Collection<UniqueColumnCombination>> uccMap = new HashMap<>();
    final Map<Column, Collection<FunctionalDependency>> fdMap = new HashMap<>();
    final Map<Column, Collection<InclusionDependency>> indMap = new HashMap<>();

    public Collection<FunctionalDependency> getFunctionalDependencies(Column columnName){
        return fdMap.get(columnName);
    }
    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(Column columnName){
        return uccMap.get(columnName);
    }
    public Collection<InclusionDependency> getInclusionDependencies(Column columnName){
        return indMap.get(columnName);
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies() {
        return fds.stream()
                .filter(fd -> !getUccs().contains(new UniqueColumnCombination(fd.getDeterminant())))
                .collect(Collectors.toList());
    }

}
