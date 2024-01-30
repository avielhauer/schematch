package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.utils.InputReader;
import de.uni_marburg.schematch.utils.MetadataUtils;
import org.hibernate.event.spi.SaveOrUpdateEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MetanomeCache{

    public static List<UniqueColumnCombination> executeUCC(List<Table> tables) {
        List<UniqueColumnCombination> uccs = new ArrayList<>();
        try {
            for (Table table : tables) {
                uccs.addAll(InputReader.readUCCFile(Path.of(table.getPath()), table, new HashMap<>()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return uccs;
    }

    public static List<FunctionalDependency> executeFD(List<Table> tables) {
        List<FunctionalDependency> fds = new ArrayList<>();
        try {
            for (Table table : tables) {
                fds.addAll(InputReader.readFDFile(Path.of(table.getPath()), table, new HashMap<>()));

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fds;
    }

    public static List<InclusionDependency> executeIND(List<Table> tables) {
        List<InclusionDependency> inds;
        try {
            inds = new ArrayList<>(InputReader.readINDFile(Path.of(tables.get(0).getPath()), tables, tables, new HashMap<>()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inds;
    }
}
