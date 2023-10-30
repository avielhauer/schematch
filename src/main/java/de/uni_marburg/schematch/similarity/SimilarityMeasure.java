package de.uni_marburg.schematch.similarity;

public interface SimilarityMeasure<DataType> {
    float compare(DataType source, DataType target);
}
