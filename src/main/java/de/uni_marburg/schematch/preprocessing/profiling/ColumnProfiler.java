package de.uni_marburg.schematch.preprocessing.profiling;

import de.uni_marburg.schematch.data.Column;

public interface ColumnProfiler {
    public void profile(Column column);
}
