package de.uni_marburg.schematch.matching.ensemble;

import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.Logistic;
import weka.classifiers.Evaluation;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

public class MachineLearningModel implements Serializable {
    public MachineLearningModel() {
    }

    public void train(String path) throws IOException {
        try {
            // Load CSV data
            Instances data = loadCSV(path);

            // Set the target class index (assuming it's the last column)
            data.setClassIndex(data.numAttributes() - 1);
            data.setClassIndex(data.numAttributes() - 1);

            // Create and configure the linear regression model
            LinearRegression linearModel = new LinearRegression();

            // You can set additional options for the model if needed
            // For example, linearModel.setOptions(weka.core.Utils.splitOptions("-S 1 -C -1 -I 100"));

            // Train the linear regression model
            linearModel.buildClassifier(data);

            // Print the trained model
            System.out.println(linearModel);

            // Make predictions (you can use new instances for prediction)
            // ...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Instances loadCSV(String filename) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filename));
        return loader.getDataSet();
    }


    public static double[] convertToPrimitive(Double[] doubleArray) {
        // Check for null or empty array
        if (doubleArray == null || doubleArray.length == 0) {
            return new double[0]; // or throw an exception, depending on your requirements
        }

        // Create a new primitive double array
        double[] primitiveArray = new double[doubleArray.length];

        // Copy values from Double array to double array
        for (int i = 0; i < doubleArray.length; i++) {
            primitiveArray[i] = doubleArray[i].doubleValue();
        }

        return primitiveArray;
    }
}