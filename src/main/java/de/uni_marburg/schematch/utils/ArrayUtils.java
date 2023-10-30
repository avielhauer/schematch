package de.uni_marburg.schematch.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class ArrayUtils {
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