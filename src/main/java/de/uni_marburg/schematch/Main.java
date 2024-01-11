package de.uni_marburg.schematch;

import de.uni_marburg.schematch.boosting.IdentitySimMatrixBoosting;
import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.matching.ensemble.AverageEnsembleMatcher;
import de.uni_marburg.schematch.matching.ensemble.CrediblityPredictorModel;
import de.uni_marburg.schematch.matching.ensemble.Feature;
import de.uni_marburg.schematch.matching.ensemble.RandomEnsembleMatcher;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.matchstep.*;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.matchtask.tablepair.generators.GroundTruthTablePairsGenerator;
import de.uni_marburg.schematch.matchtask.tablepair.generators.TablePairsGenerator;
import de.uni_marburg.schematch.data.*;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matching.MatcherFactory;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.utils.*;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class Main {
    final static Logger log = LogManager.getLogger(Main.class);
    public final static Date START_TIMESTAMP = new Date();

    public static void main(String[] args) throws Exception {

        CrediblityPredictorModel crediblityPredictorModel=new CrediblityPredictorModel();
        ModelUtils.loadDataToModel(crediblityPredictorModel);

        crediblityPredictorModel.generateColumnPairs();
        crediblityPredictorModel.addFeature(new Feature());
        crediblityPredictorModel.addFeature(new Feature());
        crediblityPredictorModel.addFeature(new Feature());
        crediblityPredictorModel.addFeature(new Feature());
        crediblityPredictorModel.generateScores();
        for (ColumnPair columnPair:crediblityPredictorModel.colomnPairs)
        {
            System.out.println(columnPair);
        }


    }


}