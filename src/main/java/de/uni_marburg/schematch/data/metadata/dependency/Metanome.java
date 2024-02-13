package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.utils.MetadataUtils;

import java.util.List;

public class Metanome {

    public static boolean SAVE = true;

    public static List<UniqueColumnCombination> executeUCC(List<Table> tables) {
        return executeOperation(tables, MetanomeCache::executeUCC, MetanomeImpl::executeUCC, "UCC");
    }

    public static List<FunctionalDependency> executeFD(List<Table> tables) {
        return executeOperation(tables, MetanomeCache::executeFD, MetanomeImpl::executeFD, "FD");
    }

    public static List<FunctionalDependency> executeApproximateFD(List<Table> tables) {
        return executeOperation(tables, MetanomeCache::executeFD, MetanomeImpl::executeApproximateFD, "FD");
    }

    public static List<InclusionDependency> executeIND(List<Table> tables) {
        return executeOperation(tables, MetanomeCache::executeIND, MetanomeImpl::executeIND, "IND");
    }

    private static <T extends Dependency> List<T> executeOperation(List<Table> tables, OperationExecutor<T> cacheExecutor, OperationExecutor<T> defaultExecutor, String dep) {
        if (MetadataUtils.metadataExists(tables.get(0).getPath(), dep)) {
            return cacheExecutor.execute(tables);
        }
        List<T> results = defaultExecutor.execute(tables);
        return results;
    }

    @FunctionalInterface
    private interface OperationExecutor<T> {
        List<T> execute(List<Table> tables);
    }

}
