package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.ensemble.features.FeatureInstace;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import weka.core.Instance;


import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import java.util.List;

public class FeatureInstanceNormalDistribution extends FeatureInstace {
    public FeatureInstanceNormalDistribution(String name) {
        super(name);
    }

    @Override
    public double calculateScore(ColumnPair columnPair) {

        return calc(getPValue(columnPair.getSourceColumn()),getPValue(columnPair.getTargetColumn()));
    }
    private double getPValue(Column c){
        if (c.getDatatype() == Datatype.FLOAT||c.getDatatype() == Datatype.INTEGER)
        {
            double[] array=new double[c.getValues().size()];
            List<String> data=c.getValues();
            for (int i=0;i<array.length;i++){
                array[i]=Double.parseDouble(data.get(i));

            }
            KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
            RealDistribution realDistribution=new NormalDistribution();
            // Test for normal distribution with mean and standard deviation
            return ksTest.kolmogorovSmirnovTest(realDistribution,array,false);


        }
        return 0;
    }
}
