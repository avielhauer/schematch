package de.uni_marburg.schematch.evaluation.metric;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.utils.Configuration;

import java.util.*;

public class MetricFactory {
    /**
     * Instantiates a metric for the given metric configuration
     * @param metricConfiguration Metric configuration to use for instantiation
     * @return Metric instance for the specified metric configuration
     * @throws Exception when reflection goes wrong
     */
    public Metric createMetricInstance(Configuration.MetricConfiguration metricConfiguration) throws Exception {
        String name = metricConfiguration.getName();
        Class<?> metricClass = Class.forName(Configuration.METRIC_PACKAGE_NAME + "." + name);

        Metric metric = (Metric) metricClass.getConstructor().newInstance();

        return metric;
    }

    /**
     * Instantiates all metrics as specified in the respective config file
     * @return List of metrics
     * @throws Exception when reflection goes wrong
     */
    public List<Metric> createMetricsFromConfig() throws Exception {
        List<Metric> metrics = new ArrayList<>();
        Configuration config = Configuration.getInstance();

        for (Configuration.MetricConfiguration metricConfiguration : config.getMetricConfigurations()) {
            metrics.add(this.createMetricInstance(metricConfiguration));
        }

        return metrics;
    }
}
