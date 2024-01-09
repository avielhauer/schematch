package de.uni_marburg.schematch.matching.ensemble;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;

public class WekaDecisionTreeExample {

    public static void main(String[] args) throws Exception {
        // Create numerical attributes
        Attribute attribute1 = new Attribute("feature1");
        Attribute attribute2 = new Attribute("feature2");
        Attribute attribute3 = new Attribute("feature3");

        // Create a categorical attribute
        Attribute categoryAttribute = new Attribute("category", Arrays.asList("category_A", "category_B"));

        // Create the target attribute
        Attribute targetAttribute = new Attribute("target");
        ArrayList<Attribute> attributes=new ArrayList<>(Arrays.asList(attribute1, attribute2, attribute3, categoryAttribute, targetAttribute));

        // Create the dataset
        Instances dataset = new Instances("MyDataset", attributes, 0);

        // Add instances to the dataset
        // Replace these values with your actual data
        dataset.add(createInstance(0.2, 0.5, 0.8, "category_A", 0.6, dataset));
        dataset.add(createInstance(0.1, 0.3, 0.7, "category_B", 0.3,dataset));
        dataset.add(createInstance(0.8, 0.2, 0.5, "category_A", 0.9,dataset));

        // Build and train the J48 decision tree
        J48 tree = new J48();
        tree.buildClassifier(dataset);

        // Make predictions for new instances
        // Replace these values with your actual data
        double[] values = {0.3, 0.6, 0.7, dataset.attribute("category").indexOfValue("category_B")};
        double prediction = tree.classifyInstance(createInstance(values));

        System.out.println("Predicted value: " + prediction);
    }

    private static Instance createInstance(double[] values) {
        DenseInstance instance = new DenseInstance(4);
        for (int i = 0; i < values.length; i++) {
            instance.setValue(i,values[i]);
        }

        return instance;
    }

    private static Instance createInstance(double v, double v1, double v2, String categoryA, double v3,Instances dt) {
        DenseInstance instance = new DenseInstance(5);

        instance.setDataset(dt);
        instance.setValue(0, v);
        instance.setValue(1, v1);
        instance.setValue(2, v2);
        instance.setValue(3, categoryA);
        instance.setValue(4, v3);

        return instance;
    }


}