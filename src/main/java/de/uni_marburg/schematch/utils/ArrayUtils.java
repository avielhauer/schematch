package de.uni_marburg.schematch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class ArrayUtils {
    private static final Logger log = LogManager.getLogger(ArrayUtils.class);

    public static float[] flattenMatrix(float[][] m) {
        float[] result = new float[m.length * m[0].length];
        for (int i = 0; i < m.length; ++i) {
            System.arraycopy(m[i], 0, result, i * m[0].length, m[i].length);
        }
        return result;
    }

    public static int[] flattenMatrix(int[][] m) {
        return Arrays.stream(m).flatMapToInt(Arrays::stream).toArray();
    }

    public static void insertSubmatrixInMatrix(int[][] submatrix, int[][] matrix, int xOffset, int yOffset) {
        for (int i = 0; i < submatrix.length; i++) {
            for (int j = 0; j < submatrix[i].length; j++) {
                matrix[i+xOffset][j+yOffset] = submatrix[i][j];
            }
        }
    }

    public static void insertSubmatrixInMatrix(float[][] submatrix, float[][] matrix, int xOffset, int yOffset) {
        for (int i = 0; i < submatrix.length; i++) {
            for (int j = 0; j < submatrix[i].length; j++) {
                matrix[i+xOffset][j+yOffset] = submatrix[i][j];
            }
        }
    }

    public static Integer[] sortIndices(float[] input, boolean ascending) {
        Integer[] indices = new Integer[input.length];

        for (int i = 0; i < input.length; i++) {
            indices[i] = i;
        }

        if (ascending) {
            Arrays.sort(indices, Comparator.comparingDouble(i -> input[i]));
        } else {
            Arrays.sort(indices, Comparator.comparingDouble(i -> -input[i]));
        }

        return indices;
    }

    public static int sumOfMatrix(int[][] m) {
        return Arrays.stream(m).mapToInt(ints -> IntStream.of(ints).sum()).sum();
    }
}