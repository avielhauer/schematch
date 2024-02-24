package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import de.uni_marburg.schematch.data.metadata.dependency.UniqueColumnCombination;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseMetadata {
    private static final Logger log = LogManager.getLogger(DatabaseMetadata.class);
    Collection<UniqueColumnCombination> uccs;
    Collection<FunctionalDependency> fds;
    Collection<InclusionDependency> inds;
    final Map<Column, Collection<UniqueColumnCombination>> uccMap = new HashMap<>();
    final Map<Column, Collection<FunctionalDependency>> fdMap = new HashMap<>();
    final Map<Column, Collection<InclusionDependency>> indMap = new HashMap<>();

    public Collection<FunctionalDependency> getFunctionalDependencies(Column columnName) {
        return fdMap.get(columnName);
    }

    public Collection<FunctionalDependency> getGpdepFDs(double lowerBound) {
        return fds.stream()
                .filter(fd -> fd.getPdepTuple().gpdep >= lowerBound).toList();
    }

    public Collection<FunctionalDependency> getGpdepFDs(double lowerBound, int size) {
        return fds.stream()
                .filter(fd -> fd.getDeterminant().size() <= size && fd.getPdepTuple().gpdep >= lowerBound).toList();
    }

    public Collection<FunctionalDependency> getGpdepFDs(Column columnName, double lowerBound) {
        return fdMap.get(columnName).stream()
                .filter(fd -> fd.getPdepTuple().gpdep >= lowerBound).toList();
    }

    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(Column columnName) {
        return uccMap.get(columnName);
    }

    public Collection<InclusionDependency> getInclusionDependencies(Column columnName) {
        return indMap.get(columnName);
    }

    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(int size) {
        return uccs.stream().filter(e -> e.getColumnCombination().size() <= size).toList();
    }

    public Collection<UniqueColumnCombination> getUniqueColumnCombinations(Column columnName, int size) {
        return uccMap.get(columnName).stream().filter(e -> e.getColumnCombination().size() <= size).toList();
    }

    public Collection<InclusionDependency> getInclusionDependencies(Column columnName, int size) {
        return indMap.get(columnName).stream().filter(e -> e.getDependant().size() <= size).toList();
    }

    public Collection<FunctionalDependency> getFunctionalDependencies(Column columnName, int size) {
        return fdMap.get(columnName).stream().filter(e -> e.getDeterminant().size() <= size).toList();
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies() {
        return fds.stream()
                .filter(fd -> !getUccs().contains(new UniqueColumnCombination(fd.getDeterminant()))).toList();
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies(int maxDeterminantSize, int maxNumberOfFDs,
                                                                                HashSet<UniqueColumnCombination> uccLookup) {  // TODO change
        Collection<FunctionalDependency> meaningfulFDs = new ArrayList<>();
        int currDeterminantSize = 1;
        while (true) {
            int finalCurrDeterminantSize = currDeterminantSize;
            Collection<FunctionalDependency> newFDs = fds.stream()
                    .filter(fd -> fd.getDeterminant().size() == finalCurrDeterminantSize
                            && !uccLookup.contains(new UniqueColumnCombination(fd.getDeterminant()))
                            && fd.getPdepTuple().pdep >= 0.9
                    ).toList();
            if (meaningfulFDs.size() + newFDs.size() > maxNumberOfFDs) {
                if (currDeterminantSize == 1) {
                    log.info("getMeaningfulFunctionalDependencies found too many FDs with determinant size 1 ("
                            + newFDs.size() + ", when " + maxNumberOfFDs + "where allowed).");
                }
                return meaningfulFDs;
            }
            meaningfulFDs.addAll(newFDs);
            currDeterminantSize += 1;
            if(currDeterminantSize > maxDeterminantSize){
                return meaningfulFDs;
            }
        }
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies(int maxDeterminantSize, HashSet<UniqueColumnCombination> uccLookup) {  // TODO change
        return fds.stream()
                .filter(fd -> fd.getDeterminant().size() <= maxDeterminantSize
                        && !uccLookup.contains(new UniqueColumnCombination(fd.getDeterminant()))
                        && fd.getPdepTuple().pdep >= 0.9
                ).toList();
    }

    public Collection<FunctionalDependency> getMeaningfulFunctionalDependencies(Column column, int size) {
        return fdMap.get(column).stream()
                .filter(fd -> fd.getDeterminant().size() <= size
                        && !getUccs().contains(new UniqueColumnCombination(fd.getDeterminant()))
                        && fd.getPdepTuple().pdep >= 0.9
                ).toList();
    }


    public FunctionalDependency subsumeFunctionalDependencyViaInclusionDependency(FunctionalDependency fd) {
        FunctionalDependency outputFd = new FunctionalDependency(fd.getDeterminant(), fd.getDependant());
        boolean changed = false;
        // TODO: do recursively
        for (InclusionDependency id : inds) {
            if (id.getDependant().size() == fd.getDeterminant().size() && id.getDependant().containsAll(fd.getDeterminant())) {
                outputFd.setDeterminant(id.getReferenced());
                changed = true;
            }
            if (id.getDependant().size() == 1 && id.getDependant().contains(fd.getDependant())) {
                if (id.getReferenced().size() > 1) {
                    throw new RuntimeException("Inclusion dependencies with multiple references columns are not yet supported by ID rewriting.");
                }

                outputFd.setDependant(id.getReferenced().stream().toList().get(0));
                changed = true;
            }
        }

        if (changed) {
            return subsumeFunctionalDependencyViaInclusionDependency(outputFd);
        }

        return outputFd;
    }

    public void setUCCs(Collection<UniqueColumnCombination> uccs) {
        this.uccs = uccs;
        uccMap.clear();
        for (UniqueColumnCombination ucc : uccs) {
            for (Column left : ucc.getColumnCombination()) {
                uccMap.computeIfAbsent(left, k -> new ArrayList<>()).add(ucc);
            }
        }
    }

    public void setFDs(Collection<FunctionalDependency> fds) {
        this.fds = fds;
        fdMap.clear();
        for (FunctionalDependency fd : fds) {
            fdMap.computeIfAbsent(fd.getDependant(), k -> new ArrayList<>()).add(fd);
            for (Column left : fd.getDeterminant()) {
                fdMap.computeIfAbsent(left, k -> new ArrayList<>()).add(fd);
            }
        }
    }

    public void setINDs(Collection<InclusionDependency> inds) {
        this.inds = inds;
        indMap.clear();
        for (InclusionDependency ind : inds) {
            for (Column left : ind.getSuperset()) {
                indMap.computeIfAbsent(left, k -> new ArrayList<>()).add(ind);
            }
            for (Column left : ind.getSubset()) {
                indMap.computeIfAbsent(left, k -> new ArrayList<>()).add(ind);
            }
        }
    }
}
