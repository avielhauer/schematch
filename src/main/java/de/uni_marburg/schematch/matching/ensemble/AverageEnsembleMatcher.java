package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

@Data
@NoArgsConstructor
public class AverageEnsembleMatcher extends Matcher {
    @Override
    public float[][] match(TablePair tablePair) {

        int numSourceColumns = tablePair.getSourceTable().getNumberOfColumns();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        Object[] firstLineMatchers;

        if (Configuration.getInstance().isRunSimMatrixBoostingOnFirstLineMatchers()) {
            firstLineMatchers = tablePair.getBoostedFirstLineMatcherResults().keySet().toArray();

        } else {
            firstLineMatchers = tablePair.getFirstLineMatcherResults().keySet().toArray();
        }
        for (int i = 0; i < numSourceColumns; i++) {
            float [] sum =new float[tablePair.getBoostedResultsForFirstLineMatcher((Matcher) firstLineMatchers[0])[i].length];
            for (int j =0;j<firstLineMatchers.length;j++) {
                if (Configuration.getInstance().isRunSimMatrixBoostingOnFirstLineMatchers()) {
                    sum = addTwo(sum, tablePair.getBoostedResultsForFirstLineMatcher((Matcher) firstLineMatchers[j])[i]);

                } else {
                    sum = addTwo(sum, tablePair.getResultsForFirstLineMatcher((Matcher) firstLineMatchers[j])[i]);
                }
            }
            simMatrix[i]=divideBy(sum,firstLineMatchers.length);
        }
        return simMatrix;
    }

    private float[] divideBy(float[] sum, int length) {
        float[] res=new float[sum.length];
        for (int i = 0; i <sum.length ; i++) {
            res[i]=sum[i]/length;
        }
        return res;
    }


    private float[] addTwo(float[] sum, float[] floats) {
        if(sum.length==floats.length)
        {
            float[] result=new float[sum.length];
            for (int i = 0; i < sum.length ; i++) {
                result[i]=sum[i]+floats[i];
            }
            return result;
        }
        System.out.println("didn't Work");
        return null;
    }
}
