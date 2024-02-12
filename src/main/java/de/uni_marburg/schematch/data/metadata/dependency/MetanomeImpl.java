package de.uni_marburg.schematch.data.metadata.dependency;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.binder.BINDERFile;
import de.metanome.algorithms.hyfd.HyFD;
import de.metanome.algorithms.hyucc.HyUCC;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import de.metanome.backend.result_receiver.ResultCache;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.utils.MetadataUtils;
import de.uni_marburg.schematch.utils.PythonUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


public class MetanomeImpl{

    public static List<UniqueColumnCombination> executeUCC(List<Table> tables) {
        return executeHyUCC(tables);
    }


    public static List<FunctionalDependency> executeFD(List<Table> tables) {
        return executeHyFD(tables);
    }

    public static List<FunctionalDependency> executeApproximateFD(List<Table> tables) {
        return executePyro(tables);
    }


    public static List<InclusionDependency> executeIND(List<Table> tables) {
        return executeBinder(tables);
    }

    private static List<UniqueColumnCombination> executeHyUCC(List<Table> tables) {
        List<UniqueColumnCombination> allResults = new ArrayList<>();
        try {
            for (Table table : tables) {
                RelationalInputGenerator input = getInputGenerator(table.getPath());
                ResultCache resultReceiver = new ResultCache("MetanomeMock", getAcceptedColumns(input));

                HyUCC hyUCC = createHyUCC(input, resultReceiver);
                
                suppressSysout(() -> {
                    try {
                        hyUCC.execute();
                    } catch (AlgorithmExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });

                List<Result> tempResults = resultReceiver.fetchNewResults();
                Collection<? extends UniqueColumnCombination> ucCs = getUCCs(table, tempResults);
                allResults.addAll(ucCs);
                if(Metanome.SAVE) MetadataUtils.saveUCCs(MetadataUtils.getMetadataPathFromTable(Path.of(table.getPath())), ucCs);
            }
        }
        catch (AlgorithmExecutionException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return allResults;
    }

    private static List<InclusionDependency> executeBinder(List<Table> tables) {
        try {
            BINDERFile binder = new BINDERFile();

            RelationalInputGenerator[] inputs = new RelationalInputGenerator[tables.size()];
            List<ColumnIdentifier> columnIdentifiers = new ArrayList<>();
            for (int i = 0; i < tables.size(); i++) {
                inputs[i] =  getInputGenerator(tables.get(i).getPath());
                columnIdentifiers.addAll(getAcceptedColumns(inputs[i]));
            }

            ResultCache resultReceiver = new ResultCache("MetanomeMock", columnIdentifiers);

            binder.setRelationalInputConfigurationValue(BINDERFile.Identifier.INPUT_FILES.name(), inputs);
            binder.setIntegerConfigurationValue(BINDERFile.Identifier.MAX_NARY_LEVEL.name(), Parameters.MAX_SEARCH_SPACE_LEVEL);
            binder.setIntegerConfigurationValue(BINDERFile.Identifier.INPUT_ROW_LIMIT.name(), Parameters.FILE_MAX_ROWS);
            binder.setBooleanConfigurationValue(BINDERFile.Identifier.DETECT_NARY.name(), Parameters.DETECT_NARY);
            binder.setResultReceiver(resultReceiver);

            suppressSysout(() -> {
                try {
                    binder.execute();
                } catch (AlgorithmExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            List<Result> results = resultReceiver.fetchNewResults();
            ArrayList<InclusionDependency> inclusionDependencies = new ArrayList<>(getINDs(tables, results));
            if(Metanome.SAVE) MetadataUtils.saveINDs(MetadataUtils.getMetadataRootPathFromTable(Path.of(tables.get(0).getPath())), inclusionDependencies);
            return inclusionDependencies;
        }
        catch (AlgorithmExecutionException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static List<FunctionalDependency> executeHyFD(List<Table> tables) {
        List<FunctionalDependency> allResults = new ArrayList<>();
        try {
            for (Table table : tables) {
                RelationalInputGenerator input = getInputGenerator(table.getPath());
                ResultCache resultReceiver = new ResultCache("MetanomeMock", getAcceptedColumns(input));

                HyFD hyFD = createHyFD(input, resultReceiver);

                suppressSysout(() -> {
                    try {
                        hyFD.execute();
                    } catch (AlgorithmExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });

                List<Result> results = resultReceiver.fetchNewResults();
                Collection<? extends FunctionalDependency> fDs = getFDs(table, results);
                allResults.addAll(fDs);
                if(Metanome.SAVE) MetadataUtils.saveFDs(MetadataUtils.getMetadataPathFromTable(Path.of(table.getPath())), fDs);
            }
        } catch (AlgorithmExecutionException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return allResults;
    }

    private static List<FunctionalDependency> executePyro(List<Table> tables) {
        List<FunctionalDependency> allResults = new ArrayList<>();

        for (Table table : tables) {
            List<FunctionalDependency> results = new ArrayList<>();

            suppressSysout(() -> {
                try {
                    HttpResponse<String> response = PythonUtils.sendHttpRequest(6001, List.of(Pair.of("table", table.getPath())));
                    if (response.body().isEmpty()) {
                        return;
                    }

                    Iterator<String> fdIterator = Arrays.stream(response.body().split("\n")).iterator();
                    while (fdIterator.hasNext()) {
                        try {
                            Collection<Column> determinants = Arrays.stream(fdIterator.next().split("\\|")).map(
                                    table::getColumnByName
                            ).collect(Collectors.toSet());
                            Column dependant = table.getColumnByName(fdIterator.next());
                            results.add(new FunctionalDependency(determinants, dependant));
                        } catch (NoSuchElementException e) {
                            throw new RuntimeException(e);
                        }
                    }

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            allResults.addAll(results);
            if(Metanome.SAVE) MetadataUtils.saveFDs(MetadataUtils.getMetadataPathFromTable(Path.of(table.getPath())), results);
        }
        return allResults;
    }

    static class Parameters {
        private static final boolean NULL_EQUALS_NULL = true;
        private static final boolean VALIDATE_PARALLEL = true;
        private static final boolean ENABLE_MEMORY_GUARDIAN = true;
        private static final boolean DETECT_NARY = true;
        private static final int MAX_SEARCH_SPACE_LEVEL = -1;
        private static final int FILE_MAX_ROWS = -1;
    };

    private static HyUCC createHyUCC(RelationalInputGenerator input, ResultCache resultReceiver) throws AlgorithmConfigurationException {
        HyUCC hyUCC = new HyUCC();
        hyUCC.setRelationalInputConfigurationValue(HyUCC.Identifier.INPUT_GENERATOR.name(), input);
        hyUCC.setBooleanConfigurationValue(HyUCC.Identifier.NULL_EQUALS_NULL.name(), Parameters.NULL_EQUALS_NULL);
        hyUCC.setBooleanConfigurationValue(HyUCC.Identifier.VALIDATE_PARALLEL.name(), Parameters.VALIDATE_PARALLEL);
        hyUCC.setBooleanConfigurationValue(HyUCC.Identifier.ENABLE_MEMORY_GUARDIAN.name(), Parameters.ENABLE_MEMORY_GUARDIAN);
        hyUCC.setIntegerConfigurationValue(HyUCC.Identifier.MAX_UCC_SIZE.name(), 2);
        hyUCC.setIntegerConfigurationValue(HyUCC.Identifier.INPUT_ROW_LIMIT.name(), Parameters.FILE_MAX_ROWS);
        hyUCC.setResultReceiver(resultReceiver);
        return hyUCC;
    }

    private static HyFD createHyFD(RelationalInputGenerator input, ResultCache resultReceiver) throws AlgorithmConfigurationException {
        HyFD hyFD = new HyFD();
        hyFD.setRelationalInputConfigurationValue(HyFD.Identifier.INPUT_GENERATOR.name(), input);
        hyFD.setBooleanConfigurationValue(HyFD.Identifier.NULL_EQUALS_NULL.name(), Parameters.NULL_EQUALS_NULL);
        hyFD.setBooleanConfigurationValue(HyFD.Identifier.VALIDATE_PARALLEL.name(), Parameters.VALIDATE_PARALLEL);
        hyFD.setBooleanConfigurationValue(HyFD.Identifier.ENABLE_MEMORY_GUARDIAN.name(), Parameters.ENABLE_MEMORY_GUARDIAN);
        hyFD.setIntegerConfigurationValue(HyFD.Identifier.MAX_DETERMINANT_SIZE.name(), 2);
        hyFD.setResultReceiver(resultReceiver);
        return hyFD;
    }

    private static RelationalInputGenerator getInputGenerator(String path) throws AlgorithmConfigurationException {
        return new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
                path,
                true,
                ',',
                '\"',
                '\\',
                false,
                true,
                0,
                true,
                true,
                ""
        ));
    }

    private static List<ColumnIdentifier> getAcceptedColumns(RelationalInputGenerator relationalInputGenerator) throws InputGenerationException, AlgorithmConfigurationException {
        RelationalInput relationalInput = relationalInputGenerator.generateNewCopy();
        String tableName = relationalInput.relationName();

        return relationalInput.columnNames().stream()
                .map(columnName -> new ColumnIdentifier(tableName, columnName))
                .toList();
    }

    private static Collection<? extends UniqueColumnCombination> getUCCs(Table table, List<Result> results) {
        return results.stream()
                .map(result -> (de.metanome.algorithm_integration.results.UniqueColumnCombination) result)
                .map(resultCast -> createUniqueColumnCombination(table, resultCast))
                .toList();
    }

    private static Collection<? extends FunctionalDependency> getFDs(Table table, List<Result> results) {
        return results.stream()
                .map(result -> (de.metanome.algorithm_integration.results.FunctionalDependency) result)
                .map(resultCast -> createFunctionalDependency(table, resultCast))
                .toList();
    }

    private static Collection<? extends InclusionDependency> getINDs(List<Table> tables, List<Result> results) {
        return results.stream()
                .map(result -> (de.metanome.algorithm_integration.results.InclusionDependency) result)
                .map(resultCast -> createInclusionDependency(tables, resultCast))
                .toList();
    }

    private static InclusionDependency createInclusionDependency(List<Table> tables, de.metanome.algorithm_integration.results.InclusionDependency resultCast) {
        Table depTable = getTableByName(tables, resultCast.getDependant().getColumnIdentifiers().get(0).getTableIdentifier().replace(".csv", ""));
        List<Column> dep = resultCast.getDependant().getColumnIdentifiers().stream()
                .map(ColumnIdentifier::getColumnIdentifier)
                .map(x -> depTable.getColumns().get(depTable.getLabels().indexOf(x)))
                .toList();
        Table refTable = getTableByName(tables, resultCast.getReferenced().getColumnIdentifiers().get(0).getTableIdentifier().replace(".csv", ""));
        List<Column> ref = resultCast.getReferenced().getColumnIdentifiers().stream()
                .map(ColumnIdentifier::getColumnIdentifier)
                .map(x -> refTable.getColumns().get(refTable.getLabels().indexOf(x)))
                .toList();
        return new InclusionDependency(dep, ref);
    }

    public static Table getTableByName(List<Table> tables, String tableName) {
        Optional<Table> matchingTable = tables.stream()
                .filter(table -> table.getName().equals(tableName))
                .findFirst();

        return matchingTable.orElse(null);
    }

    private static UniqueColumnCombination createUniqueColumnCombination(Table table, de.metanome.algorithm_integration.results.UniqueColumnCombination resultCast) {
        List<Column> list = resultCast.getColumnCombination().getColumnIdentifiers().stream()
                .map(ColumnIdentifier::getColumnIdentifier)
                .map(x -> table.getColumns().get(table.getLabels().indexOf(x)))
                .toList();
        return new UniqueColumnCombination(list);
    }

    private static FunctionalDependency createFunctionalDependency(Table table, de.metanome.algorithm_integration.results.FunctionalDependency resultCast) {
        Column dep = table.getColumns().get(table.getLabels().indexOf(resultCast.getDependant().getColumnIdentifier()));
        List<Column> list = resultCast.getDeterminant().getColumnIdentifiers().stream()
                .map(ColumnIdentifier::getColumnIdentifier)
                .map(x -> table.getColumns().get(table.getLabels().indexOf(x)))
                .toList();
        return new FunctionalDependency(list, dep);
    }

    //BLAME THE AUTHORS @METANOME ALGORITHMS
    private static void suppressSysout(Runnable method) throws RuntimeException{
//        PrintStream originalOut = System.out;
//        System.setOut(new PrintStream(new NullOutputStream()));
        method.run();
//        System.setOut(originalOut);
    }
}
