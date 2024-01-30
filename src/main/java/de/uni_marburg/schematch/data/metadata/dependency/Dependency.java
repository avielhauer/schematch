package de.uni_marburg.schematch.data.metadata.dependency;

import de.uni_marburg.schematch.data.Column;

import java.util.Collection;

public interface Dependency {
    default void addColumns(StringBuilder sb, Collection<Column> referenced) {
        sb.append("[");
        for (Column column : referenced) {
            sb.append(column.getTable().getName());
            sb.append(".csv.");
            sb.append(column.getLabel());
            sb.append(", ");
        }
        if(!referenced.isEmpty())
            sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
    }
}
