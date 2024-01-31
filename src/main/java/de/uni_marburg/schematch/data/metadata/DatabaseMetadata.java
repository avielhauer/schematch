package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public Collection<FunctionalDependency> getGpdepFDs(double lowerBound){
        return fds.stream()
                .filter(fd -> fd.getPdepTuple().gpdep >= lowerBound).toList();
    }

    public Collection<FunctionalDependency> getGpdepFDs(int size, double lowerBound){
        return fds.stream()
                .filter(fd -> (fd.getDeterminant().size() < size && fd.getPdepTuple().gpdep >= lowerBound)).toList();
    }

    public Collection<FunctionalDependency> getGpdepFDs(Column columnName, double lowerBound){
        return fdMap.get(columnName).stream()
                .filter(fd -> fd.getPdepTuple().gpdep >= lowerBound).toList();
    }

    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(Column columnName){
        return uccMap.get(columnName);
    }
    public Collection<InclusionDependency> getInclusionDependencies(Column columnName){
        return indMap.get(columnName);
    }

    public Collection<FunctionalDependency> getFunctionalDependencies(Column columnName, int size){
        return fdMap.get(columnName).stream().filter(e -> e.getDeterminant().size() <= size).toList();
    }
    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(Column columnName, int size){
        return uccMap.get(columnName).stream().filter(e -> e.getColumnCombination().size() <= size).toList();
    }

    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(int size){
        return uccs.stream().filter(e -> e.getColumnCombination().size() <= size).toList();
    }

    public Collection<InclusionDependency> getInclusionDependencies(Column columnName, int size){
        return indMap.get(columnName).stream().filter(e -> e.getDependant().size() <= size).toList();
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies() {
        return fds.stream()
                .filter(fd -> !getUccs().contains(new UniqueColumnCombination(fd.getDeterminant()))).toList();
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies(int size) {
        return fds.stream()
                .filter(fd -> fd.getDeterminant().size() <= size
                        && !getUccs().contains(new UniqueColumnCombination(fd.getDeterminant()))
                ).toList();
    }

    public void setUCCs(Collection<UniqueColumnCombination> uccs){
        this.uccs = uccs;
        uccMap.clear();
        for(UniqueColumnCombination ucc: uccs){
            for (Column left: ucc.getColumnCombination()){
                uccMap.computeIfAbsent(left, k -> new ArrayList<>()).add(ucc);
            }
        }
    }

    public void setFDs(Collection<FunctionalDependency> fds){
        this.fds = fds;
        fdMap.clear();
        for (FunctionalDependency fd: fds){
            fdMap.computeIfAbsent(fd.getDependant(), k -> new ArrayList<>()).add(fd);
            for (Column left: fd.getDeterminant()){
                fdMap.computeIfAbsent(left, k -> new ArrayList<>()).add(fd);
            }
        }
    }

    public void setINDs(Collection<InclusionDependency> inds){
        this.inds = inds;
        indMap.clear();
        for (InclusionDependency ind : inds){
            for (Column left: ind.getSuperset()){
                indMap.computeIfAbsent(left, k -> new ArrayList<>()).add(ind);
            }
            for (Column left: ind.getSubset()){
                indMap.computeIfAbsent(left, k -> new ArrayList<>()).add(ind);
            }
        }
    }
}
