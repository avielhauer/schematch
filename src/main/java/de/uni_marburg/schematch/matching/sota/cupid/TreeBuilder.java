package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TreeBuilder {
    /**
     * Generates a pair of data types as a string set and the schema tree of the table
     * @param table database table
     * @return Pair<data types as a string set, schema tree of the table>
     */
    Pair<Set<String>,SchemaTree> buildTreeFromTable(Table table, boolean use_simple_data_types) {
        HashSet<String> categories = new HashSet<>();

        SchemaElement root = new SchemaElement("DB__" + table.getName(), "DB");
        root.addCategory("Database");
        categories.add("Database");

        SchemaTree tree = new SchemaTree(root, table.hashCode());

        SchemaElement tableElement = new SchemaElement(table.getName(), "Table");
        tableElement.addCategory("Table");
        categories.add("Table");

        tree.addNode(table.getName(), tree.getRoot(), new ArrayList<>(), tableElement);

        for (Column column : table.getColumns()) {
            categories.add(addColumn(tree, column, use_simple_data_types));
        }

        return new Pair<>(categories, tree);
    }

    /**
     * Adds columns as a leaf node to the table
     * @param targetTree Schema tree, to which the node should be added
     * @param column column from which the node should be derived
     * @return target tree with column node as leaf
     */
    private String addColumn(SchemaTree targetTree, Column column, boolean use_simple_data_types) {
        String datatype = CupidDataTypeConversion.convertDatatype(column, use_simple_data_types);
        SchemaElement schemtmp = new SchemaElement(column.getLabel(), datatype);
        schemtmp.addCategory(datatype);
        targetTree.addNode(column.getLabel(), targetTree.getRoot().getChildren().get(0), new ArrayList<>(), schemtmp);
        return datatype;
    }
}
